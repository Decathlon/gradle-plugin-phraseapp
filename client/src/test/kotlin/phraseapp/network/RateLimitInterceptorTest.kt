package phraseapp.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [RateLimitInterceptor].
 *
 * MockWebServer is used to simulate the Phrase API responses.
 * The `sleepFn` is injected as a no-op so tests don't actually wait for backoff delays.
 *
 * Phrase signals two kinds of 429:
 * - Concurrent cap: includes `Ratelimit-Limit` + `Ratelimit-Remaining` headers → ~60s backoff
 * - Global RPM limit: those headers are absent → exponential short backoff
 */
class RateLimitInterceptorTest {

    private val server = MockWebServer()
    private val recordedSleepMs = mutableListOf<Long>()
    private val noOpSleep: (Long) -> Unit = { ms -> recordedSleepMs.add(ms) }

    private fun client(maxAttempts: Int = 4) = OkHttpClient.Builder()
        .addInterceptor(RateLimitInterceptor(maxAttempts = maxAttempts, sleepFn = noOpSleep))
        .build()

    private fun get() = Request.Builder().url(server.url("/")).build()

    @Before
    fun setUp() {
        server.start()
        recordedSleepMs.clear()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Happy path — no 429
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should pass through and not retry when response is 200`() {
        server.enqueue(MockResponse().setResponseCode(200))

        val response = client().newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(1, server.requestCount)
        assertEquals(0, recordedSleepMs.size)
    }

    @Test
    fun `should pass through and not retry on 500`() {
        server.enqueue(MockResponse().setResponseCode(500))

        val response = client().newCall(get()).execute()

        assertEquals(500, response.code)
        assertEquals(1, server.requestCount)
        assertEquals(0, recordedSleepMs.size)
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Global RPM limit (no Ratelimit headers)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should retry once and succeed when global 429 is followed by 200`() {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = client().newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
        assertEquals(1, recordedSleepMs.size)
    }

    @Test
    fun `should retry multiple times and succeed when multiple global 429 precede 200`() {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = client(maxAttempts = 4).newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(3, server.requestCount)
        assertEquals(2, recordedSleepMs.size)
    }

    @Test
    fun `should apply exponential-like backoff for global RPM 429 — each delay strictly positive`() {
        // 3 consecutive 429s then success → 3 sleeps
        repeat(3) { server.enqueue(MockResponse().setResponseCode(429)) }
        server.enqueue(MockResponse().setResponseCode(200))

        client(maxAttempts = 5).newCall(get()).execute()

        assertEquals(3, recordedSleepMs.size)
        recordedSleepMs.forEach { ms ->
            assertTrue("Expected positive backoff, got $ms ms", ms > 0)
        }
    }

    @Test
    fun `should return 429 and stop retrying after maxAttempts exhausted`() {
        // Queue more 429s than maxAttempts (3 total: 1 initial + 2 retries)
        repeat(5) { server.enqueue(MockResponse().setResponseCode(429)) }

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(429, response.code)
        // maxAttempts=3 → 1 initial + 2 retries = 3 requests
        assertEquals(3, server.requestCount)
        assertEquals(2, recordedSleepMs.size)
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Concurrent cap (Ratelimit-Limit + Ratelimit-Remaining headers present)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should retry and succeed when concurrent 429 (with headers) is followed by 200`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Ratelimit-Limit", "50")
                .addHeader("Ratelimit-Remaining", "0")
        )
        server.enqueue(MockResponse().setResponseCode(200))

        val response = client().newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
        assertEquals(1, recordedSleepMs.size)
    }

    @Test
    fun `should apply long backoff (~60s range) for concurrent cap 429`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Ratelimit-Limit", "50")
                .addHeader("Ratelimit-Remaining", "0")
        )
        server.enqueue(MockResponse().setResponseCode(200))

        client().newCall(get()).execute()

        assertEquals(1, recordedSleepMs.size)
        val sleepMs = recordedSleepMs.first()
        // Expected ~60s ±10% jitter → 54_000..66_000 ms
        assertTrue("Expected concurrent backoff ~60s but got ${sleepMs}ms", sleepMs in 54_000L..66_000L)
    }

    @Test
    fun `should apply short backoff (below 10s) for global RPM 429`() {
        server.enqueue(MockResponse().setResponseCode(429)) // no Ratelimit headers
        server.enqueue(MockResponse().setResponseCode(200))

        client().newCall(get()).execute()

        assertEquals(1, recordedSleepMs.size)
        val sleepMs = recordedSleepMs.first()
        // attempt=1 → base=1s ±50% jitter → max 1500ms
        assertTrue("Expected short exponential backoff (<10s) but got ${sleepMs}ms", sleepMs <= 10_000L)
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Retry-After header
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should use Retry-After header value when present`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", "30") // 30 seconds
        )
        server.enqueue(MockResponse().setResponseCode(200))

        client().newCall(get()).execute()

        assertEquals(1, recordedSleepMs.size)
        assertEquals(30_000L, recordedSleepMs.first())
    }

    @Test
    fun `should use Retry-After over concurrent cap backoff when both are present`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", "5")
                .addHeader("Ratelimit-Limit", "50")
                .addHeader("Ratelimit-Remaining", "0")
        )
        server.enqueue(MockResponse().setResponseCode(200))

        client().newCall(get()).execute()

        assertEquals(1, recordedSleepMs.size)
        assertEquals(5_000L, recordedSleepMs.first())
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Header distinction: concurrent cap requires BOTH headers
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should apply global backoff when only Ratelimit-Limit header is present (not concurrent)`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Ratelimit-Limit", "50") // only one of the two headers
        )
        server.enqueue(MockResponse().setResponseCode(200))

        client().newCall(get()).execute()

        // Short backoff expected (global RPM), not the 60s concurrent backoff
        val sleepMs = recordedSleepMs.first()
        assertTrue("Expected global (short) backoff but got ${sleepMs}ms", sleepMs <= 10_000L)
    }

    @Test
    fun `should apply global backoff when only Ratelimit-Remaining header is present (not concurrent)`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Ratelimit-Remaining", "0") // only one of the two headers
        )
        server.enqueue(MockResponse().setResponseCode(200))

        client().newCall(get()).execute()

        val sleepMs = recordedSleepMs.first()
        assert(sleepMs <= 10_000L) {
            "Expected global (short) backoff but got ${sleepMs}ms"
        }
    }
}



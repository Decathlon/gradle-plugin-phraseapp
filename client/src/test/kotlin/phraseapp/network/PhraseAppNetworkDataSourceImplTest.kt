package phraseapp.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import phraseapp.network.mock.MockPhraseAppService
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tests for [PhraseAppNetworkDataSourceImpl] focused on the concurrency Semaphore behaviour and
 * the constructor parameter propagation added to fix Phrase API HTTP 429 errors.
 */
class PhraseAppNetworkDataSourceImplTest {

    // ──────────────────────────────────────────────────────────────────────────────
    // Existing constructor — default concurrency (maxConcurrentDownloads = 1)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should use default concurrency of 1 when no maxConcurrentDownloads is specified`() =
        runBlocking {
            val maxConcurrent = AtomicInteger(0)
            val peakConcurrent = AtomicInteger(0)
            val service = concurrencyTrackingService(maxConcurrent, peakConcurrent, delayMs = 50L)

            val impl = PhraseAppNetworkDataSourceImpl("", "", "", service)
            impl.downloadAllLocales()

            assertEquals(
                "Default concurrency should be 1 — peak concurrent was ${peakConcurrent.get()}",
                1, peakConcurrent.get()
            )
        }

    // ──────────────────────────────────────────────────────────────────────────────
    // Explicit concurrency = 1 (recommended for CI with multiple parallel builds)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should never exceed 1 concurrent download when maxConcurrentDownloads is 1`() =
        runBlocking {
            val maxConcurrent = AtomicInteger(0)
            val peakConcurrent = AtomicInteger(0)
            val service = concurrencyTrackingService(maxConcurrent, peakConcurrent, delayMs = 50L)

            val impl = PhraseAppNetworkDataSourceImpl("", "", "", service, maxConcurrentDownloads = 1)
            impl.downloadAllLocales()

            assertEquals(
                "maxConcurrentDownloads=1 — peak concurrent was ${peakConcurrent.get()}",
                1, peakConcurrent.get()
            )
        }

    // ──────────────────────────────────────────────────────────────────────────────
    // Explicit concurrency = 2 (useful when fewer parallel CI builds)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should allow up to 2 concurrent downloads when maxConcurrentDownloads is 2`() =
        runBlocking {
            val maxConcurrent = AtomicInteger(0)
            val peakConcurrent = AtomicInteger(0)
            val service = concurrencyTrackingService(maxConcurrent, peakConcurrent, delayMs = 100L)

            val impl = PhraseAppNetworkDataSourceImpl("", "", "", service, maxConcurrentDownloads = 2)
            impl.downloadAllLocales()

            assertTrue(
                "maxConcurrentDownloads=2 — peak concurrent should be ≤2 but was ${peakConcurrent.get()}",
                peakConcurrent.get() <= 2
            )
            assertTrue(
                "maxConcurrentDownloads=2 — peak concurrent should be ≥2 (3 locales, enough overlap) but was ${peakConcurrent.get()}",
                peakConcurrent.get() >= 2
            )
        }

    @Test
    fun `should never exceed the configured maxConcurrentDownloads limit`() = runBlocking {
        val maxConcurrent = AtomicInteger(0)
        val peakConcurrent = AtomicInteger(0)
        // Use a 3-locale service to stress the concurrency limit
        val service = concurrencyTrackingService(maxConcurrent, peakConcurrent, delayMs = 50L)

        for (limit in 1..3) {
            maxConcurrent.set(0)
            peakConcurrent.set(0)
            val impl =
                PhraseAppNetworkDataSourceImpl("", "", "", service, maxConcurrentDownloads = limit)
            impl.downloadAllLocales()
            assertTrue(
                "limit=$limit — peak was ${peakConcurrent.get()} which exceeds the configured limit",
                peakConcurrent.get() <= limit
            )
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Edge cases
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    fun `should clamp maxConcurrentDownloads to 1 when 0 is passed`() = runBlocking {
        // Internal coerceAtLeast(1) prevents illegal Semaphore(0) construction
        val service = MockPhraseAppService()
        val impl = PhraseAppNetworkDataSourceImpl("", "", "", service, maxConcurrentDownloads = 0)
        // Should complete without throwing
        val result = impl.downloadAllLocales()
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `should return all locales regardless of concurrency setting`() = runBlocking {
        val service = MockPhraseAppService()

        for (limit in listOf(1, 2, 4)) {
            val result = PhraseAppNetworkDataSourceImpl("", "", "", service, maxConcurrentDownloads = limit)
                .downloadAllLocales()
            assertEquals(
                "All 3 locales must be downloaded regardless of concurrency limit=$limit",
                3, result.size
            )
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // Helper — wraps the mock service with concurrency instrumentation
    // ──────────────────────────────────────────────────────────────────────────────

    /**
     * Returns a [PhraseAppService] that delegates to [MockPhraseAppService] while tracking
     * the number of in-flight `download` calls to measure peak concurrency.
     */
    private fun concurrencyTrackingService(
        current: AtomicInteger,
        peak: AtomicInteger,
        delayMs: Long
    ): PhraseAppService = object : PhraseAppService {
        private val delegate = MockPhraseAppService()

        override suspend fun getLocales(authorization: String, projectId: String) =
            delegate.getLocales(authorization, projectId)

        override suspend fun download(
            authorization: String,
            projectId: String,
            localeId: String,
            fileFormat: String,
            placeHolder: Boolean
        ): ResponseBody {
            val active = current.incrementAndGet()
            // Update peak atomically
            peak.getAndUpdate { prev -> maxOf(prev, active) }
            try {
                delay(delayMs)
                return delegate.download(authorization, projectId, localeId, fileFormat, placeHolder)
            } finally {
                current.decrementAndGet()
            }
        }

        override suspend fun upload(
            authorization: String,
            projectId: String,
            file: MultipartBody.Part,
            localeId: RequestBody,
            fileFormat: RequestBody,
            updateTranslations: RequestBody,
            updateDescriptions: RequestBody,
            skipUploadTags: RequestBody
        ): ResponseBody = "".toResponseBody("text/xml".toMediaTypeOrNull())
    }
}


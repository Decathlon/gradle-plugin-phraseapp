package phraseapp.network

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.math.pow
import kotlin.random.Random

/**
 * OkHttp interceptor that handles HTTP 429 responses from Phrase API.
 *
 * Phrase enforces two kinds of limits and signals them differently:
 * - Concurrent request cap: response includes `Ratelimit-Limit` and
 *   `Ratelimit-Remaining` headers. Recovered with a long ~60s backoff + jitter.
 * - Global per-minute quota (RPM): those headers are absent. Recovered with a
 *   shorter exponential backoff (1s, 2s, 4s) + jitter.
 *
 * If the server provides a `Retry-After` header, it takes precedence.
 *
 * @param maxAttempts total attempts per request (initial call + retries).
 * @param sleepFn injectable sleep function — override in tests to avoid real delays.
 */
class RateLimitInterceptor(
    private val maxAttempts: Int = 4,
    private val sleepFn: (Long) -> Unit = { ms -> Thread.sleep(ms) }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var response: Response = chain.proceed(request)

        while (response.code == HTTP_TOO_MANY_REQUESTS && attempt < maxAttempts - 1) {
            attempt++
            // Only retry idempotent methods to avoid duplicating non-idempotent requests
            // (e.g., POST multipart uploads may be non-repeatable and cause side-effects).
            val method = request.method.uppercase()
            if (method !in RETRYABLE_METHODS) {
                println("[PhraseAppNetwork] HTTP 429 received on non-retryable method $method - not retrying")
                break
            }
            val isConcurrent = response.header(HEADER_RATELIMIT_LIMIT) != null
                    && response.header(HEADER_RATELIMIT_REMAINING) != null
            val retryAfterMs = response.header(HEADER_RETRY_AFTER)
                ?.toLongOrNull()
                ?.let { it * 1000L }
            val backoffMs = (retryAfterMs ?: computeBackoff(isConcurrent, attempt)).coerceAtLeast(0L)

            val limitType = if (isConcurrent) "concurrent cap" else "global RPM"
            println(
                "[PhraseAppNetwork] HTTP 429 received ($limitType) on ${request.method} ${request.url} - " +
                        "retry $attempt/${maxAttempts - 1} in ${backoffMs}ms"
            )

            // Free the connection before sleeping.
            response.close()

            try {
                sleepFn(backoffMs)
            } catch (e: InterruptedException) {
                // Restore interrupt flag and wrap into an IOException so callers of OkHttp
                // interceptors receive an IO-related exception (conventional behavior).
                Thread.currentThread().interrupt()
                val ioe = java.io.InterruptedIOException("Sleep interrupted during rate-limit backoff")
                ioe.initCause(e)
                throw ioe
            }

            response = chain.proceed(request)
        }

        if (response.code == HTTP_TOO_MANY_REQUESTS) {
            println(
                "[PhraseAppNetwork] HTTP 429 still returned after $maxAttempts attempts on " +
                        "${request.method} ${request.url} - giving up"
            )
        }

        return response
    }

    private fun computeBackoff(isConcurrent: Boolean, attempt: Int): Long {
        return if (isConcurrent) {
            // Long fixed backoff (~60s) with ±10% jitter.
            val base = 60_000L
            val jitter = (base * 0.1).toLong()
            base + Random.nextLong(-jitter, jitter + 1)
        } else {
            // Exponential 1s, 2s, 4s, ... with ±50% jitter, capped to avoid overflow and
            // extremely long sleeps when `attempt` is large. Cap to MAX_GLOBAL_BACKOFF_MS.
            val maxGlobalBackoff = MAX_GLOBAL_BACKOFF_MS
            // Compute exponential base safely using Long and cap it.
            val raw = try {
                // Use Double pow but guard against Infinity.
                val v = 1_000.0 * 2.0.pow((attempt - 1).coerceAtLeast(0))
                if (v.isFinite()) v.toLong() else maxGlobalBackoff
            } catch (_: Exception) {
                maxGlobalBackoff
            }
            val base = raw.coerceAtMost(maxGlobalBackoff)
            val jitter = (base * 0.5).toLong().coerceAtLeast(1L)
            base + Random.nextLong(-jitter, jitter + 1)
        }
    }

    companion object {
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private const val HEADER_RATELIMIT_LIMIT = "Ratelimit-Limit"
        private const val HEADER_RATELIMIT_REMAINING = "Ratelimit-Remaining"
        private const val HEADER_RETRY_AFTER = "Retry-After"
        private const val MAX_GLOBAL_BACKOFF_MS = 30_000L
        private val RETRYABLE_METHODS = setOf("GET", "HEAD", "OPTIONS")
    }
}




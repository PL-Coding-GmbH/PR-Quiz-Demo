package com.example.prquizdemo.data

import com.example.prquizdemo.data.remote.QuoteApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface QuoteRepository {
  suspend fun getQuotes(forceRefresh: Boolean = false): List<Quote>

  suspend fun getQuote(id: String): Quote?
}

/**
 * Keeps the last successful API response in memory for [cacheTtlMillis].
 *
 * If a refresh fails after all retries and an older response exists, that stale response is
 * returned instead of the error, so the app keeps working offline after the first load.
 */
class CachingQuoteRepository(
  private val api: QuoteApi,
  private val cacheTtlMillis: Long = 5 * 60 * 1000,
  private val now: () -> Long = System::currentTimeMillis,
) : QuoteRepository {

  private val mutex = Mutex()
  private var cached: List<Quote>? = null
  private var cachedAt = 0L

  override suspend fun getQuotes(forceRefresh: Boolean): List<Quote> =
    // The list and detail screens can request quotes at the same time; the lock makes the
    // second caller reuse the first caller's response instead of firing a second request.
    mutex.withLock {
      val current = cached
      if (!forceRefresh && current != null && now() - cachedAt < cacheTtlMillis) {
        return@withLock current
      }
      try {
        retryOnIoError { api.fetchQuotes() }.also {
          cached = it
          cachedAt = now()
        }
      } catch (e: Exception) {
        current ?: throw e
      }
    }

  override suspend fun getQuote(id: String): Quote? = getQuotes().firstOrNull { it.id == id }
}

package com.example.prquizdemo.data

import com.example.prquizdemo.data.remote.QuoteApi
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CachingQuoteRepositoryTest {

  private val quotes = listOf(Quote("1", "Text", "Author"))
  private var time = 0L

  @Test
  fun returnsCachedQuotesWithinTtl() = runTest {
    val api = FakeApi(responses = mutableListOf({ quotes }))
    val repository = CachingQuoteRepository(api, cacheTtlMillis = 1000, now = { time })

    repository.getQuotes()
    time = 999
    repository.getQuotes()

    assertEquals(1, api.calls)
  }

  @Test
  fun refetchesAfterTtlExpires() = runTest {
    val api = FakeApi(responses = mutableListOf({ quotes }, { quotes }))
    val repository = CachingQuoteRepository(api, cacheTtlMillis = 1000, now = { time })

    repository.getQuotes()
    time = 1000
    repository.getQuotes()

    assertEquals(2, api.calls)
  }

  @Test
  fun retriesIoErrorsBeforeSucceeding() = runTest {
    val api = FakeApi(responses = mutableListOf({ throw IOException() }, { throw IOException() }, { quotes }))
    val repository = CachingQuoteRepository(api, now = { time })

    assertEquals(quotes, repository.getQuotes())
    assertEquals(3, api.calls)
  }

  @Test
  fun doesNotRetryNonIoErrors() = runTest {
    val api = FakeApi(responses = mutableListOf({ throw IllegalStateException() }, { quotes }))
    val repository = CachingQuoteRepository(api, now = { time })

    runCatching { repository.getQuotes() }

    assertEquals(1, api.calls)
  }

  @Test
  fun returnsStaleQuotesWhenRefreshFails() = runTest {
    val failing: suspend () -> List<Quote> = { throw IOException() }
    val api = FakeApi(responses = mutableListOf({ quotes }, failing, failing, failing))
    val repository = CachingQuoteRepository(api, now = { time })

    val first = repository.getQuotes()
    val refreshed = repository.getQuotes(forceRefresh = true)

    assertSame(first, refreshed)
  }

  private class FakeApi(private val responses: MutableList<suspend () -> List<Quote>>) : QuoteApi {
    var calls = 0

    override suspend fun fetchQuotes(): List<Quote> {
      calls++
      return responses.removeAt(0).invoke()
    }
  }
}

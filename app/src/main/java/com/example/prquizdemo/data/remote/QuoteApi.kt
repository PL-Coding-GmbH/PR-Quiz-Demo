package com.example.prquizdemo.data.remote

import com.example.prquizdemo.data.Quote
import java.io.IOException
import kotlin.random.Random
import kotlinx.coroutines.delay

interface QuoteApi {
  suspend fun fetchQuotes(): List<Quote>
}

/** Stand-in for a real backend. Simulates latency and flaky connectivity. */
class FakeQuoteApi(private val random: Random = Random.Default, private val failureRate: Double = 0.3) : QuoteApi {

  override suspend fun fetchQuotes(): List<Quote> {
    delay(800)
    if (random.nextDouble() < failureRate) throw IOException("Simulated network failure")
    return QUOTES
  }

  private companion object {
    val QUOTES =
      listOf(
        Quote("1", "Simplicity is prerequisite for reliability.", "Edsger W. Dijkstra"),
        Quote("2", "Premature optimization is the root of all evil.", "Donald Knuth"),
        Quote("3", "Make it work, make it right, make it fast.", "Kent Beck"),
        Quote("4", "Talk is cheap. Show me the code.", "Linus Torvalds"),
        Quote("5", "Programs must be written for people to read.", "Harold Abelson"),
        Quote("6", "Any fool can write code that a computer can understand.", "Martin Fowler"),
      )
  }
}

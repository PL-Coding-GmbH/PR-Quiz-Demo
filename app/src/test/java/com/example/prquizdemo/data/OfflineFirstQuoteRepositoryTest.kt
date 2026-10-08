package com.example.prquizdemo.data

import com.example.prquizdemo.data.local.FavoriteEntity
import com.example.prquizdemo.data.local.QuoteDao
import com.example.prquizdemo.data.local.QuoteEntity
import com.example.prquizdemo.data.local.QuoteWithFavoriteRow
import com.example.prquizdemo.data.remote.QuoteApi
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class OfflineFirstQuoteRepositoryTest {

  private val kent = Quote("1", "Make it work, make it right, make it fast.", "Kent Beck")
  private val linus = Quote("2", "Talk is cheap. Show me the code.", "Linus Torvalds")

  @Test
  fun refreshStoresFetchedQuotes() = runTest {
    val dao = FakeQuoteDao()
    val repository = OfflineFirstQuoteRepository(FakeApi({ listOf(kent, linus) }), dao)

    repository.refresh()

    assertEquals(listOf(kent, linus), repository.observeQuotes().first().map { it.quote })
  }

  @Test
  fun failedRefreshKeepsStoredQuotesAndThrows() = runTest {
    val dao = FakeQuoteDao()
    val failing: suspend () -> List<Quote> = { throw IOException() }
    val repository = OfflineFirstQuoteRepository(FakeApi({ listOf(kent) }, failing, failing, failing), dao)
    repository.refresh()

    try {
      repository.refresh()
      fail("Expected the refresh to throw")
    } catch (e: IOException) {
      assertEquals(listOf(kent), repository.observeQuotes().first().map { it.quote })
    }
  }

  @Test
  fun refreshRetriesIoErrors() = runTest {
    val api = FakeApi({ throw IOException() }, { listOf(kent) })
    val repository = OfflineFirstQuoteRepository(api, FakeQuoteDao())

    repository.refresh()

    assertEquals(2, api.calls)
  }

  @Test
  fun favoriteIsReflectedInObservedQuotes() = runTest {
    val repository = OfflineFirstQuoteRepository(FakeApi({ listOf(kent, linus) }), FakeQuoteDao())
    repository.refresh()

    repository.setFavorite(linus.id, isFavorite = true)

    val favorites = repository.observeQuotes().first().filter { it.isFavorite }.map { it.quote }
    assertEquals(listOf(linus), favorites)
  }

  @Test
  fun observeQuoteReturnsNullForUnknownId() = runTest {
    val repository = OfflineFirstQuoteRepository(FakeApi({ listOf(kent) }), FakeQuoteDao())
    repository.refresh()

    assertTrue(repository.observeQuote("unknown").first() == null)
  }

  private class FakeApi(vararg responses: suspend () -> List<Quote>) : QuoteApi {
    private val responses = responses.toMutableList()
    var calls = 0

    override suspend fun fetchQuotes(): List<Quote> {
      calls++
      return responses.removeAt(0).invoke()
    }
  }

  private class FakeQuoteDao : QuoteDao() {
    private val quotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    private val favorites = MutableStateFlow<Set<String>>(emptySet())

    override fun observeQuotes(): Flow<List<QuoteWithFavoriteRow>> =
      combine(quotes, favorites) { quotes, favorites ->
        quotes.map { QuoteWithFavoriteRow(it.id, it.text, it.author, it.id in favorites) }
      }

    override fun observeQuote(id: String): Flow<QuoteWithFavoriteRow?> =
      observeQuotes().map { rows -> rows.firstOrNull { it.id == id } }

    override suspend fun upsertQuotes(quotes: List<QuoteEntity>) {
      val byId = this.quotes.value.associateBy { it.id } + quotes.associateBy { it.id }
      this.quotes.value = byId.values.toList()
    }

    override suspend fun deleteQuotesNotIn(ids: List<String>) {
      quotes.value = quotes.value.filter { it.id in ids }
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
      favorites.value += favorite.quoteId
    }

    override suspend fun deleteFavorite(quoteId: String) {
      favorites.value -= quoteId
    }
  }
}

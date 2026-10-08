package com.example.prquizdemo.data

import com.example.prquizdemo.data.local.FavoriteEntity
import com.example.prquizdemo.data.local.QuoteDao
import com.example.prquizdemo.data.local.toQuoteEntity
import com.example.prquizdemo.data.local.toQuoteWithFavorite
import com.example.prquizdemo.data.remote.QuoteApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface QuoteRepository {
  fun observeQuotes(): Flow<List<QuoteWithFavorite>>

  fun observeQuote(id: String): Flow<QuoteWithFavorite?>

  /** Fetches the latest quotes and stores them. Throws if the API can't be reached after retries. */
  suspend fun refresh()

  suspend fun setFavorite(quoteId: String, isFavorite: Boolean)
}

/**
 * Room is the single source of truth. Screens only observe the database, and [refresh] is the
 * only path that talks to the API, writing its result into the database.
 */
class OfflineFirstQuoteRepository(private val api: QuoteApi, private val dao: QuoteDao) : QuoteRepository {

  override fun observeQuotes(): Flow<List<QuoteWithFavorite>> =
    dao.observeQuotes().map { rows -> rows.map { it.toQuoteWithFavorite() } }

  override fun observeQuote(id: String): Flow<QuoteWithFavorite?> =
    dao.observeQuote(id).map { it?.toQuoteWithFavorite() }

  override suspend fun refresh() {
    val quotes = retryOnIoError { api.fetchQuotes() }
    dao.replaceQuotes(quotes.map { it.toQuoteEntity() })
  }

  override suspend fun setFavorite(quoteId: String, isFavorite: Boolean) {
    if (isFavorite) dao.insertFavorite(FavoriteEntity(quoteId)) else dao.deleteFavorite(quoteId)
  }
}

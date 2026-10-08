package com.example.prquizdemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuoteDao {

  @Query(
    """
    SELECT q.id, q.text, q.author, (f.quoteId IS NOT NULL) AS isFavorite
    FROM quotes q LEFT JOIN favorites f ON f.quoteId = q.id
    """
  )
  abstract fun observeQuotes(): Flow<List<QuoteWithFavoriteRow>>

  @Query(
    """
    SELECT q.id, q.text, q.author, (f.quoteId IS NOT NULL) AS isFavorite
    FROM quotes q LEFT JOIN favorites f ON f.quoteId = q.id
    WHERE q.id = :id
    """
  )
  abstract fun observeQuote(id: String): Flow<QuoteWithFavoriteRow?>

  /** Makes the quotes table match [quotes]: upserts all of them and deletes quotes no longer returned. */
  @Transaction
  open suspend fun replaceQuotes(quotes: List<QuoteEntity>) {
    upsertQuotes(quotes)
    deleteQuotesNotIn(quotes.map { it.id })
  }

  @Upsert protected abstract suspend fun upsertQuotes(quotes: List<QuoteEntity>)

  @Query("DELETE FROM quotes WHERE id NOT IN (:ids)")
  protected abstract suspend fun deleteQuotesNotIn(ids: List<String>)

  @Insert(onConflict = OnConflictStrategy.IGNORE) abstract suspend fun insertFavorite(favorite: FavoriteEntity)

  @Query("DELETE FROM favorites WHERE quoteId = :quoteId") abstract suspend fun deleteFavorite(quoteId: String)
}

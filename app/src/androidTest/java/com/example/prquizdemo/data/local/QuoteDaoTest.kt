package com.example.prquizdemo.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuoteDaoTest {

  private lateinit var database: QuoteDatabase
  private lateinit var dao: QuoteDao

  private val kent = QuoteEntity("1", "Make it work, make it right, make it fast.", "Kent Beck")
  private val linus = QuoteEntity("2", "Talk is cheap. Show me the code.", "Linus Torvalds")

  @Before
  fun setUp() {
    database =
      Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), QuoteDatabase::class.java).build()
    dao = database.quoteDao()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun replaceQuotesKeepsFavorites() = runTest {
    dao.replaceQuotes(listOf(kent, linus))
    dao.insertFavorite(FavoriteEntity(kent.id))

    dao.replaceQuotes(listOf(kent.copy(text = "Updated text"), linus))

    val stored = dao.observeQuote(kent.id).first()
    assertEquals(true, stored?.isFavorite)
    assertEquals("Updated text", stored?.text)
  }

  @Test
  fun replaceQuotesRemovesQuotesNoLongerReturned() = runTest {
    dao.replaceQuotes(listOf(kent, linus))

    dao.replaceQuotes(listOf(linus))

    assertEquals(listOf(linus.id), dao.observeQuotes().first().map { it.id })
  }

  @Test
  fun favoriteOfRemovedQuoteComesBackWhenQuoteReturns() = runTest {
    dao.replaceQuotes(listOf(kent, linus))
    dao.insertFavorite(FavoriteEntity(kent.id))
    dao.replaceQuotes(listOf(linus))

    dao.replaceQuotes(listOf(kent, linus))

    assertEquals(true, dao.observeQuote(kent.id).first()?.isFavorite)
  }
}

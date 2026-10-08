package com.example.prquizdemo.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkerFactory
import com.example.prquizdemo.data.OfflineFirstQuoteRepository
import com.example.prquizdemo.data.QuoteRepository
import com.example.prquizdemo.data.local.LegacyFavoritesImport
import com.example.prquizdemo.data.local.QuoteDatabase
import com.example.prquizdemo.data.remote.FakeQuoteApi
import com.example.prquizdemo.data.sync.QuoteSyncScheduler
import com.example.prquizdemo.data.sync.QuoteWorkerFactory
import com.example.prquizdemo.data.sync.WorkManagerQuoteSyncScheduler

/** App-wide singletons. Created once in [com.example.prquizdemo.PRQuizApp]. */
class AppContainer(context: Context) {

  private val database =
    Room.databaseBuilder(context, QuoteDatabase::class.java, "quotes.db")
      .addCallback(LegacyFavoritesImport(context.getSharedPreferences("favorites", Context.MODE_PRIVATE)))
      .build()

  val quoteRepository: QuoteRepository = OfflineFirstQuoteRepository(api = FakeQuoteApi(), dao = database.quoteDao())

  val quoteSyncScheduler: QuoteSyncScheduler = WorkManagerQuoteSyncScheduler(context)

  val workerFactory: WorkerFactory = QuoteWorkerFactory(quoteRepository)
}

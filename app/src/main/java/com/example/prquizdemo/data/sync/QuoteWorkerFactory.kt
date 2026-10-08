package com.example.prquizdemo.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.prquizdemo.data.QuoteRepository

/** Lets WorkManager create workers with dependencies from [com.example.prquizdemo.di.AppContainer]. */
class QuoteWorkerFactory(private val repository: QuoteRepository) : WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters,
  ): ListenableWorker? =
    when (workerClassName) {
      QuoteSyncWorker::class.java.name -> QuoteSyncWorker(appContext, workerParameters, repository)
      else -> null
    }
}

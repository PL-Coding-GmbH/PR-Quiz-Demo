package com.example.prquizdemo.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

interface QuoteSyncScheduler {
  fun schedulePeriodicSync()
}

class WorkManagerQuoteSyncScheduler(private val context: Context) : QuoteSyncScheduler {

  override fun schedulePeriodicSync() {
    val request =
      PeriodicWorkRequestBuilder<QuoteSyncWorker>(6, TimeUnit.HOURS)
        .setConstraints(
          Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresBatteryNotLow(true).build()
        )
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

    // KEEP, because this runs on every app start and must not reset the schedule each time.
    WorkManager.getInstance(context)
      .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
  }

  private companion object {
    const val WORK_NAME = "sync_quotes"
  }
}

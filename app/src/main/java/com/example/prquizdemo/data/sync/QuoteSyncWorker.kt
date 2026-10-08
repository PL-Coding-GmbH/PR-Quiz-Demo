package com.example.prquizdemo.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.prquizdemo.data.QuoteRepository
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

typealias WorkerResult = ListenableWorker.Result

class QuoteSyncWorker(
  context: Context,
  params: WorkerParameters,
  private val repository: QuoteRepository,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): WorkerResult {
    if (runAttemptCount >= MAX_ATTEMPTS) return WorkerResult.failure()
    return try {
      repository.refresh()
      WorkerResult.success()
    } catch (e: CancellationException) {
      throw e
    } catch (e: IOException) {
      WorkerResult.retry()
    } catch (e: Exception) {
      WorkerResult.failure()
    }
  }

  private companion object {
    const val MAX_ATTEMPTS = 3
  }
}

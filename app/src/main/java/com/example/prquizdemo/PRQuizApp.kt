package com.example.prquizdemo

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.example.prquizdemo.di.AppContainer

/**
 * Provides the WorkManager configuration itself, so WorkManager is initialized on demand with
 * [AppContainer.workerFactory]. The default startup initializer is removed in the manifest.
 */
class PRQuizApp : Application(), Configuration.Provider {
  lateinit var container: AppContainer
    private set

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder().setWorkerFactory(container.workerFactory).build()

  override fun onCreate() {
    super.onCreate()
    container = AppContainer(this)
    container.quoteSyncScheduler.schedulePeriodicSync()
  }
}

val Context.appContainer: AppContainer
  get() = (applicationContext as PRQuizApp).container

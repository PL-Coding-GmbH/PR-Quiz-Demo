package com.example.prquizdemo

import android.app.Application
import android.content.Context
import com.example.prquizdemo.di.AppContainer

class PRQuizApp : Application() {
  lateinit var container: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    container = AppContainer(this)
  }
}

val Context.appContainer: AppContainer
  get() = (applicationContext as PRQuizApp).container

package com.example.prquizdemo.di

import android.content.Context
import com.example.prquizdemo.data.CachingQuoteRepository
import com.example.prquizdemo.data.FavoritesStore
import com.example.prquizdemo.data.QuoteRepository
import com.example.prquizdemo.data.SharedPreferencesFavoritesStore
import com.example.prquizdemo.data.remote.FakeQuoteApi

/** App-wide singletons. Created once in [com.example.prquizdemo.PRQuizApp]. */
class AppContainer(context: Context) {
  val quoteRepository: QuoteRepository = CachingQuoteRepository(api = FakeQuoteApi())

  val favoritesStore: FavoritesStore =
    SharedPreferencesFavoritesStore(context.getSharedPreferences("favorites", Context.MODE_PRIVATE))
}

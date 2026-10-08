package com.example.prquizdemo.data

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext

interface FavoritesStore {
  val favoriteIds: StateFlow<Set<String>>

  suspend fun setFavorite(id: String, isFavorite: Boolean)
}

class SharedPreferencesFavoritesStore(private val prefs: SharedPreferences) : FavoritesStore {

  private val _favoriteIds = MutableStateFlow(prefs.getStringSet(KEY, emptySet()).orEmpty().toSet())
  override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

  override suspend fun setFavorite(id: String, isFavorite: Boolean) {
    val updated = _favoriteIds.updateAndGet { if (isFavorite) it + id else it - id }
    withContext(Dispatchers.IO) { prefs.edit().putStringSet(KEY, updated).commit() }
  }

  private companion object {
    const val KEY = "favorite_quote_ids"
  }
}

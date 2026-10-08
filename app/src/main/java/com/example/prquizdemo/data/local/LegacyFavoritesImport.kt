package com.example.prquizdemo.data.local

import android.content.SharedPreferences
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Moves favorites saved by the previous SharedPreferences-based store into the new database.
 *
 * Runs from [onCreate], which Room only calls when the database file is first created, so the
 * import happens exactly once per install and needs no "already migrated" flag.
 */
class LegacyFavoritesImport(private val prefs: SharedPreferences) : RoomDatabase.Callback() {

  override fun onCreate(db: SupportSQLiteDatabase) {
    prefs.getStringSet(LEGACY_KEY, emptySet()).orEmpty().forEach { quoteId ->
      db.execSQL("INSERT OR IGNORE INTO favorites (quoteId) VALUES (?)", arrayOf(quoteId))
    }
    prefs.edit().remove(LEGACY_KEY).apply()
  }

  private companion object {
    const val LEGACY_KEY = "favorite_quote_ids"
  }
}

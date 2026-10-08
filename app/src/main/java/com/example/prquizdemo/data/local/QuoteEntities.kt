package com.example.prquizdemo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(@PrimaryKey val id: String, val text: String, val author: String)

/**
 * Favorites live in their own table instead of a column on [QuoteEntity], so a sync that
 * rewrites the quotes table can never reset them.
 *
 * There's deliberately no foreign key to `quotes`: favorites imported from the old
 * SharedPreferences store exist before the first sync, and a favorite should survive a quote
 * temporarily missing from the API.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val quoteId: String)

data class QuoteWithFavoriteRow(val id: String, val text: String, val author: String, val isFavorite: Boolean)

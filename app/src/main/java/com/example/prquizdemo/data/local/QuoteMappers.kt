package com.example.prquizdemo.data.local

import com.example.prquizdemo.data.Quote
import com.example.prquizdemo.data.QuoteWithFavorite

fun Quote.toQuoteEntity(): QuoteEntity = QuoteEntity(id = id, text = text, author = author)

fun QuoteWithFavoriteRow.toQuoteWithFavorite(): QuoteWithFavorite =
  QuoteWithFavorite(quote = Quote(id = id, text = text, author = author), isFavorite = isFavorite)

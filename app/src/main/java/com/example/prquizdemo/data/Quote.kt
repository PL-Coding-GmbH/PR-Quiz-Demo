package com.example.prquizdemo.data

data class Quote(val id: String, val text: String, val author: String)

data class QuoteWithFavorite(val quote: Quote, val isFavorite: Boolean)

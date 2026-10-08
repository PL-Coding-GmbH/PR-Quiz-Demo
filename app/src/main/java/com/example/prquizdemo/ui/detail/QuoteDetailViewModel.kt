package com.example.prquizdemo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prquizdemo.data.FavoritesStore
import com.example.prquizdemo.data.Quote
import com.example.prquizdemo.data.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface QuoteDetailState {
  data object Loading : QuoteDetailState

  data object NotFound : QuoteDetailState

  data class Loaded(val quote: Quote, val isFavorite: Boolean) : QuoteDetailState
}

class QuoteDetailViewModel(
  private val quoteId: String,
  repository: QuoteRepository,
  private val favoritesStore: FavoritesStore,
) : ViewModel() {

  private val quote = MutableStateFlow<Result<Quote?>?>(null)

  val state: StateFlow<QuoteDetailState> =
    combine(quote, favoritesStore.favoriteIds) { quote, favoriteIds ->
        when {
          quote == null -> QuoteDetailState.Loading
          quote.getOrNull() == null -> QuoteDetailState.NotFound
          else -> QuoteDetailState.Loaded(quote.getOrThrow()!!, quoteId in favoriteIds)
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuoteDetailState.Loading)

  init {
    viewModelScope.launch { quote.value = runCatching { repository.getQuote(quoteId) } }
  }

  fun onToggleFavorite() {
    viewModelScope.launch { favoritesStore.setFavorite(quoteId, quoteId !in favoritesStore.favoriteIds.value) }
  }
}

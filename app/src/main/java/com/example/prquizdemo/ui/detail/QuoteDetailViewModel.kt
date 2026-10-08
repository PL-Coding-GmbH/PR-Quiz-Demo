package com.example.prquizdemo.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prquizdemo.data.Quote
import com.example.prquizdemo.data.QuoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface QuoteDetailState {
  data object Loading : QuoteDetailState

  data object NotFound : QuoteDetailState

  data class Loaded(val quote: Quote, val isFavorite: Boolean) : QuoteDetailState
}

class QuoteDetailViewModel(private val quoteId: String, private val repository: QuoteRepository) : ViewModel() {

  val state: StateFlow<QuoteDetailState> =
    repository
      .observeQuote(quoteId)
      .map { it?.let { QuoteDetailState.Loaded(it.quote, it.isFavorite) } ?: QuoteDetailState.NotFound }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuoteDetailState.Loading)

  fun onToggleFavorite() {
    val loaded = state.value as? QuoteDetailState.Loaded ?: return
    viewModelScope.launch { repository.setFavorite(quoteId, !loaded.isFavorite) }
  }
}

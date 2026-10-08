package com.example.prquizdemo.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prquizdemo.data.QuoteRepository
import com.example.prquizdemo.data.QuoteWithFavorite
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuoteListState(
  val query: String = "",
  val items: List<QuoteWithFavorite> = emptyList(),
  val isRefreshing: Boolean = false,
  val isShowingSavedQuotes: Boolean = false,
  val errorMessage: String? = null,
)

@OptIn(FlowPreview::class)
class QuoteListViewModel(private val repository: QuoteRepository) : ViewModel() {

  private val query = MutableStateFlow("")
  private val refreshState = MutableStateFlow(RefreshState())

  val state: StateFlow<QuoteListState> =
    combine(query, query.debounce(SEARCH_DEBOUNCE_MILLIS), repository.observeQuotes(), refreshState) {
        rawQuery,
        debouncedQuery,
        quotes,
        refreshState ->
        val refreshFailed = refreshState.errorMessage != null
        QuoteListState(
          query = rawQuery,
          items = quotes.filter { it.matches(debouncedQuery) }.sortedForDisplay(),
          isRefreshing = refreshState.isRefreshing,
          isShowingSavedQuotes = refreshFailed && quotes.isNotEmpty(),
          errorMessage = refreshState.errorMessage.takeIf { quotes.isEmpty() },
        )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuoteListState(isRefreshing = true))

  init {
    refresh()
  }

  fun onQueryChange(newQuery: String) {
    query.value = newQuery
  }

  fun onRefresh() {
    refresh()
  }

  fun onToggleFavorite(quoteId: String) {
    val isFavorite = state.value.items.firstOrNull { it.quote.id == quoteId }?.isFavorite ?: return
    viewModelScope.launch { repository.setFavorite(quoteId, !isFavorite) }
  }

  private fun refresh() {
    viewModelScope.launch {
      refreshState.update { it.copy(isRefreshing = true) }
      refreshState.value =
        try {
          repository.refresh()
          RefreshState()
        } catch (e: Exception) {
          RefreshState(errorMessage = e.message ?: "Could not load quotes")
        }
    }
  }

  private fun QuoteWithFavorite.matches(query: String): Boolean =
    query.isBlank() || quote.text.contains(query, ignoreCase = true) || quote.author.contains(query, ignoreCase = true)

  private fun List<QuoteWithFavorite>.sortedForDisplay(): List<QuoteWithFavorite> =
    sortedWith(compareByDescending<QuoteWithFavorite> { it.isFavorite }.thenBy { it.quote.author })

  private data class RefreshState(val isRefreshing: Boolean = false, val errorMessage: String? = null)

  private companion object {
    const val SEARCH_DEBOUNCE_MILLIS = 300L
  }
}

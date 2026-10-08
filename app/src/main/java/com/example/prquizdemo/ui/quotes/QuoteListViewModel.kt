package com.example.prquizdemo.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prquizdemo.data.FavoritesStore
import com.example.prquizdemo.data.Quote
import com.example.prquizdemo.data.QuoteRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuoteItem(val quote: Quote, val isFavorite: Boolean)

data class QuoteListState(
  val query: String = "",
  val items: List<QuoteItem> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
)

@OptIn(FlowPreview::class)
class QuoteListViewModel(private val repository: QuoteRepository, private val favoritesStore: FavoritesStore) :
  ViewModel() {

  private val query = MutableStateFlow("")
  private val quotes = MutableStateFlow<List<Quote>>(emptyList())
  private val loadState = MutableStateFlow(LoadState())

  val state: StateFlow<QuoteListState> =
    combine(query, query.debounce(SEARCH_DEBOUNCE_MILLIS), quotes, favoritesStore.favoriteIds, loadState) {
        rawQuery,
        debouncedQuery,
        quotes,
        favoriteIds,
        loadState ->
        QuoteListState(
          query = rawQuery,
          items = quotes.filter { it.matches(debouncedQuery) }.toItems(favoriteIds),
          isLoading = loadState.isLoading,
          errorMessage = loadState.errorMessage,
        )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuoteListState(isLoading = true))

  init {
    load(forceRefresh = false)
  }

  fun onQueryChange(newQuery: String) {
    query.value = newQuery
  }

  fun onRefresh() {
    load(forceRefresh = true)
  }

  fun onToggleFavorite(quoteId: String) {
    viewModelScope.launch {
      val isFavorite = quoteId in favoritesStore.favoriteIds.value
      favoritesStore.setFavorite(quoteId, !isFavorite)
    }
  }

  private fun load(forceRefresh: Boolean) {
    viewModelScope.launch {
      loadState.update { it.copy(isLoading = true, errorMessage = null) }
      try {
        quotes.value = repository.getQuotes(forceRefresh)
        loadState.value = LoadState()
      } catch (e: Exception) {
        loadState.value = LoadState(errorMessage = e.message ?: "Could not load quotes")
      }
    }
  }

  private fun Quote.matches(query: String): Boolean =
    query.isBlank() || text.contains(query, ignoreCase = true) || author.contains(query, ignoreCase = true)

  private fun List<Quote>.toItems(favoriteIds: Set<String>): List<QuoteItem> =
    map { QuoteItem(it, it.id in favoriteIds) }
      .sortedWith(compareByDescending<QuoteItem> { it.isFavorite }.thenBy { it.quote.author })

  private data class LoadState(val isLoading: Boolean = false, val errorMessage: String? = null)

  private companion object {
    const val SEARCH_DEBOUNCE_MILLIS = 300L
  }
}

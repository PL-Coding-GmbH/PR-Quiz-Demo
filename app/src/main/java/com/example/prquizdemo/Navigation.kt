package com.example.prquizdemo

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.prquizdemo.ui.detail.QuoteDetailRoot
import com.example.prquizdemo.ui.detail.QuoteDetailViewModel
import com.example.prquizdemo.ui.quotes.QuoteListRoot
import com.example.prquizdemo.ui.quotes.QuoteListViewModel

@Composable
fun MainNavigation() {
  val container = LocalContext.current.appContainer
  val backStack = rememberNavBackStack(QuoteList)
  val screenModifier = Modifier.safeDrawingPadding().padding(16.dp)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    // Scopes each ViewModel to its back stack entry, so a detail ViewModel is cleared when that
    // entry is popped instead of living as long as the Activity.
    entryDecorators =
      listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
    entryProvider =
      entryProvider {
        entry<QuoteList> {
          QuoteListRoot(
            viewModel = viewModel { QuoteListViewModel(container.quoteRepository, container.favoritesStore) },
            onQuoteClick = { backStack.add(QuoteDetail(it)) },
            modifier = screenModifier,
          )
        }
        entry<QuoteDetail> { key ->
          QuoteDetailRoot(
            viewModel =
              viewModel { QuoteDetailViewModel(key.quoteId, container.quoteRepository, container.favoritesStore) },
            onBack = { backStack.removeLastOrNull() },
            modifier = screenModifier,
          )
        }
      },
  )
}

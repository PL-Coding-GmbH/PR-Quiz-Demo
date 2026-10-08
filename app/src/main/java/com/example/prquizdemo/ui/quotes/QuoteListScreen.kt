package com.example.prquizdemo.ui.quotes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prquizdemo.data.Quote
import com.example.prquizdemo.data.QuoteWithFavorite
import com.example.prquizdemo.theme.PRQuizDemoTheme

@Composable
fun QuoteListRoot(viewModel: QuoteListViewModel, onQuoteClick: (String) -> Unit, modifier: Modifier = Modifier) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  QuoteListScreen(
    state = state,
    onQueryChange = viewModel::onQueryChange,
    onRefresh = viewModel::onRefresh,
    onToggleFavorite = viewModel::onToggleFavorite,
    onQuoteClick = onQuoteClick,
    modifier = modifier,
  )
}

@Composable
fun QuoteListScreen(
  state: QuoteListState,
  onQueryChange: (String) -> Unit,
  onRefresh: () -> Unit,
  onToggleFavorite: (String) -> Unit,
  onQuoteClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = state.query,
        onValueChange = onQueryChange,
        label = { Text("Search quotes or authors") },
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
      TextButton(onClick = onRefresh, enabled = !state.isRefreshing) { Text("Refresh") }
    }
    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (state.isShowingSavedQuotes) {
      Text("You're offline. Showing saved quotes.", style = MaterialTheme.typography.labelLarge)
    }
    Box(Modifier.fillMaxSize()) {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.items, key = { it.quote.id }) { item ->
          QuoteCard(item, onClick = { onQuoteClick(item.quote.id) }, onToggleFavorite = { onToggleFavorite(item.quote.id) })
        }
      }
      if (state.isRefreshing && state.items.isEmpty()) CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
  }
}

@Composable
private fun QuoteCard(item: QuoteWithFavorite, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
  Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        Text("“${item.quote.text}”", style = MaterialTheme.typography.bodyLarge)
        Text("— ${item.quote.author}", style = MaterialTheme.typography.labelMedium)
      }
      TextButton(onClick = onToggleFavorite) { Text(if (item.isFavorite) "★" else "☆") }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun QuoteListScreenPreview() {
  PRQuizDemoTheme {
    QuoteListScreen(
      state =
        QuoteListState(
          items =
            listOf(
              QuoteWithFavorite(Quote("1", "Make it work, make it right, make it fast.", "Kent Beck"), isFavorite = true),
              QuoteWithFavorite(Quote("2", "Talk is cheap. Show me the code.", "Linus Torvalds"), isFavorite = false),
            )
        ),
      onQueryChange = {},
      onRefresh = {},
      onToggleFavorite = {},
      onQuoteClick = {},
    )
  }
}

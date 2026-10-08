package com.example.prquizdemo.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun QuoteDetailRoot(viewModel: QuoteDetailViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  QuoteDetailScreen(state = state, onToggleFavorite = viewModel::onToggleFavorite, onBack = onBack, modifier = modifier)
}

@Composable
fun QuoteDetailScreen(
  state: QuoteDetailState,
  onToggleFavorite: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier.fillMaxSize()) {
    when (state) {
      QuoteDetailState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
      QuoteDetailState.NotFound -> Text("This quote doesn't exist anymore.", Modifier.align(Alignment.Center))
      is QuoteDetailState.Loaded ->
        Column(Modifier.align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text("“${state.quote.text}”", style = MaterialTheme.typography.headlineSmall)
          Text("— ${state.quote.author}", style = MaterialTheme.typography.titleMedium)
          Button(onClick = onToggleFavorite) { Text(if (state.isFavorite) "Remove from favorites" else "Add to favorites") }
        }
    }
    TextButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) { Text("Back") }
  }
}

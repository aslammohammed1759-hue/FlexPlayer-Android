package com.flexplayer.app.ui.converted

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.flexplayer.app.data.local.ConvertedVideoEntity
import com.flexplayer.app.data.repository.ConversionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ConvertedViewModel @Inject constructor(
    private val repo: ConversionRepository
) : ViewModel() {
    val items: StateFlow<List<ConvertedVideoEntity>> = repo.convertedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(item: ConvertedVideoEntity) = viewModelScope.launch { repo.deleteConversion(item) }
}

@Composable
fun ConvertedScreen(viewModel: ConvertedViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), androidx.compose.ui.Alignment.Center) {
            Text("No converted copies yet")
        }
    } else LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.outputPath }) { item ->
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(item.outputPath.substringAfterLast('/')) },
                    supportingContent = {
                        Text("${item.presetName} · ${item.outputSizeBytes / 1_048_576} MB")
                    },
                    trailingContent = {
                        TextButton(onClick = { viewModel.delete(item) }) { Text("Delete") }
                    }
                )
            }
        }
    }
}

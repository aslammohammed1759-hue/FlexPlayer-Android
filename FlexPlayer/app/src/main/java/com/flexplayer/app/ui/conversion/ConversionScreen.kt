package com.flexplayer.app.ui.conversion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flexplayer.app.model.ConversionPreset

@Composable
fun ConversionScreen(
    sourceUri: String,
    onDone: () -> Unit = {},
    viewModel: ConversionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Convert video", style = MaterialTheme.typography.headlineSmall)
        Text(
            "A separate compatible copy is created. The original file is never modified.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text("Preset", style = MaterialTheme.typography.labelLarge)
        ConversionPreset.entries.forEach { preset ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.preset == preset,
                    onClick = { viewModel.setPreset(preset) }
                )
                Text(preset.label)
            }
        }

        if (state.running) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth()
            )
            Text("${(state.progress * 100).toInt()}%")
            OutlinedButton(onClick = viewModel::cancel) { Text("Cancel") }
        } else {
            Button(onClick = { viewModel.start(sourceUri) }) { Text("Start conversion") }
        }

        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.outputFile?.let { file ->
            Text("Ready: ${file.name}", color = MaterialTheme.colorScheme.primary)
            Button(onClick = onDone) { Text("Continue to Share") }
        }
    }
}

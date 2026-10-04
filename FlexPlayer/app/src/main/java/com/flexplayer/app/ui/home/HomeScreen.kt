package com.flexplayer.app.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flexplayer.app.model.CompatibilityIssue
import com.flexplayer.app.model.CompatibilityResult

private val videoPermission =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO
    else Manifest.permission.READ_EXTERNAL_STORAGE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onPlayVideo: (String) -> Unit = {},
    onConvertVideo: (String) -> Unit = {},
    onOpenConverted: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, videoPermission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.load()
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(videoPermission)
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("FlexPlayer") },
            actions = {
                TextButton(onClick = onOpenConverted) { Text("Converted") }
            }
        )
    }) { padding ->
        when {
            !hasPermission -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FlexPlayer needs access to your videos to scan your library.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { permissionLauncher.launch(videoPermission) }) {
                        Text("Grant permission")
                    }
                }
            }
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.videos.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("No videos found on this device.")
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.videos, key = { it.id }) { video ->
                    val result = state.compat[video.id]
                    Card(
                        Modifier.fillMaxWidth().clickable {
                            when (result) {
                                is CompatibilityResult.NeedsConversion -> onConvertVideo(video.uri)
                                else -> onPlayVideo(video.uri)
                            }
                        }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(video.displayName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${video.resolutionLabel} · ${video.frameRate.toInt()}fps · " +
                                "${video.codec.ifBlank { "…" }}${if (video.isHdr) " · HDR" else ""}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            when (result) {
                                is CompatibilityResult.Compatible -> AssistChip(
                                    onClick = { onPlayVideo(video.uri) },
                                    label = { Text("✓ Compatible — play original") }
                                )
                                is CompatibilityResult.NeedsConversion ->
                                    ConversionBadge(result) { onConvertVideo(video.uri) }
                                null -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversionBadge(
    result: CompatibilityResult.NeedsConversion,
    onConvertClick: () -> Unit
) {
    val label = when {
        result.issues.contains(CompatibilityIssue.UNSUPPORTED_CODEC) ->
            "⚠ Unsupported codec (${result.video.codec}) — convert"
        else -> "⚠ Convert recommended"
    }
    SuggestionChip(onClick = onConvertClick, label = { Text(label) })
    Text(
        result.explanation,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

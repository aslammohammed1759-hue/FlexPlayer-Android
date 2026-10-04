package com.flexplayer.app.ui.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun PlayerScreen(uri: String, viewModel: PlayerViewModel = hiltViewModel()) {
    DisposableEffect(uri) {
        viewModel.play(uri)
        onDispose { viewModel.pause() }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = true
                player = viewModel.player
            }
        },
        update = { view -> view.player = viewModel.player },
        modifier = Modifier.fillMaxSize()
    )
}

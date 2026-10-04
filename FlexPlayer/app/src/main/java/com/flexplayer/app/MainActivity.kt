package com.flexplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.flexplayer.app.ui.conversion.ConversionScreen
import com.flexplayer.app.ui.converted.ConvertedScreen
import com.flexplayer.app.ui.home.HomeScreen
import com.flexplayer.app.ui.player.PlayerScreen
import com.flexplayer.app.ui.share.ShareScreen
import com.flexplayer.app.ui.theme.FlexPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

/** Minimal manual navigation — no navigation-compose dependency required. */
private sealed interface Screen {
    data object Home : Screen
    data class Player(val uri: String) : Screen
    data class Conversion(val uri: String) : Screen
    data class Share(val uri: String) : Screen
    data object Converted : Screen
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlexPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

                    if (screen !is Screen.Home) {
                        BackHandler { screen = Screen.Home }
                    }

                    when (val s = screen) {
                        is Screen.Home -> HomeScreen(
                            onPlayVideo = { uri -> screen = Screen.Player(uri) },
                            onConvertVideo = { uri -> screen = Screen.Conversion(uri) },
                            onOpenConverted = { screen = Screen.Converted }
                        )
                        is Screen.Player -> SubScreen(title = "Player", onBack = { screen = Screen.Home }) {
                            PlayerScreen(uri = s.uri)
                        }
                        is Screen.Conversion -> SubScreen(title = "Convert", onBack = { screen = Screen.Home }) {
                            ConversionScreen(
                                sourceUri = s.uri,
                                onDone = { screen = Screen.Share(s.uri) }
                            )
                        }
                        is Screen.Share -> SubScreen(title = "Share", onBack = { screen = Screen.Home }) {
                            ShareScreen(sourceUri = s.uri)
                        }
                        is Screen.Converted -> SubScreen(title = "Converted copies", onBack = { screen = Screen.Home }) {
                            ConvertedScreen()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = { Button(onClick = onBack) { Text("Back") } }
        )
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) { content() }
    }
}

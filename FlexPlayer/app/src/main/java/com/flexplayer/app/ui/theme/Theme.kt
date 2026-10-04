package com.flexplayer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00696B),
    secondary = Color(0xFF4A6363),
    tertiary = Color(0xFF7A5470)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CDADB),
    secondary = Color(0xFFB0CCCB),
    tertiary = Color(0xFFE8B9D5)
)

@Composable
fun FlexPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

package com.flexplayer.app.ui.share

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flexplayer.app.model.ConversionPreset
import com.flexplayer.app.sharing.SmartShareManager

/** Smart Share picker: Original / Smart Compatible / Smaller File. */
@Composable
fun ShareScreen(
    sourceUri: String,
    smartShare: SmartShareManager = androidx.hilt.navigation.compose.hiltViewModel<ShareViewModelHolder>().smartShare
) {
    val context = LocalContext.current
    var preset by remember { mutableStateOf(ConversionPreset.BALANCED) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Smart Share", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = {
            context.startActivity(smartShare.shareIntent(
                context, SmartShareManager.ShareMode.OriginalQuality, sourceUri))
        }) { Text("Original Quality") }

        Text("Compatibility preset:", style = MaterialTheme.typography.labelLarge)
        ConversionPreset.entries.forEach { p ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(selected = preset == p, onClick = { preset = p })
                Text(p.label)
            }
        }

        Button(onClick = {
            context.startActivity(smartShare.shareIntent(
                context, SmartShareManager.ShareMode.SmartCompatible(preset), sourceUri))
        }) { Text("Smart Compatible") }

        OutlinedButton(onClick = {
            context.startActivity(smartShare.shareIntent(
                context, SmartShareManager.ShareMode.SmallerFile(preset), sourceUri))
        }) { Text("Smaller File") }

        Text(
            "Note: Android sharing can't detect the recipient's hardware, " +
            "so Smart Compatible uses the preset you selected.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

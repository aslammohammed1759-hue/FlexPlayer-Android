package com.flexplayer.app.ui.compatibility

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flexplayer.app.model.CompatibilityResult
import com.flexplayer.app.model.ConversionPreset

/** Explains WHY conversion is suggested, so the choice is transparent. */
@Composable
fun CompatibilityScreen(result: CompatibilityResult.NeedsConversion) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Compatibility report", style = MaterialTheme.typography.headlineSmall)
        Text(result.video.displayName, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        result.issues.forEach { issue ->
            ListItem(
                headlineContent = { Text(issue.name.lowercase().replace("_", " ")) },
                supportingContent = {
                    Text(when (issue) {
                        com.flexplayer.app.model.CompatibilityIssue.UNSUPPORTED_CODEC ->
                            "Device decoders cannot play ${result.video.codec}."
                        com.flexplayer.app.model.CompatibilityIssue.RESOLUTION_EXCEEDS_DECODER ->
                            "${result.video.resolutionLabel} exceeds this device's decoder limit."
                        com.flexplayer.app.model.CompatibilityIssue.FRAME_RATE_TOO_HIGH ->
                            "${result.video.frameRate.toInt()} fps exceeds decoder capability."
                        com.flexplayer.app.model.CompatibilityIssue.HDR_NOT_SUPPORTED ->
                            "HDR video on an SDR-only device may look washed out."
                        com.flexplayer.app.model.CompatibilityIssue.BITRATE_TOO_HIGH ->
                            "Bitrate may exceed reliable hardware decoding limits."
                    })
                }
            )
        }
        Text("Recommended preset: ${result.recommendedPreset.label}",
            style = MaterialTheme.typography.labelLarge)
        Text(result.explanation, style = MaterialTheme.typography.bodyMedium)
    }
}

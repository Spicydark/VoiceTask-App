package com.joshtalk.sampletask.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joshtalk.sampletask.platform.AudioPlayer

/**
 * Reusable audio playback control card for recorded audio preview.
 * Provides play/pause toggle and delete functionality. Visual progress indicator
 * shows simplified playback state (no actual progress tracking yet).
 * 
 * Used across all task screens to allow agents to review their recordings before submission.
 * Automatically releases audio player resources when component is disposed.
 * 
 * @param audioPath Absolute file path to the audio recording
 * @param audioPlayer Platform audio player service
 * @param onDelete Callback invoked when user deletes the recording
 * @param modifier Optional modifier for styling/positioning
 */
@Composable
fun AudioPlaybackCard(
    audioPath: String,
    audioPlayer: AudioPlayer,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    
    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Recording",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = {
                    if (isPlaying) {
                        audioPlayer.pause()
                        isPlaying = false
                    } else {
                        audioPlayer.play(audioPath)
                        isPlaying = true
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            
            IconButton(onClick = {
                audioPlayer.stop()
                isPlaying = false
                onDelete()
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete"
                )
            }
        }
        
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            progress = { if (isPlaying) 0.5f else 0f }
        )
    }
}

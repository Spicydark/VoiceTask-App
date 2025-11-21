package com.joshtalk.sampletask.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshtalk.sampletask.ui.theme.PrimaryBlue

@Composable
fun PressAndHoldMicButton(
    onPressStart: () -> Unit,
    onPressRelease: () -> Unit,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onPressStart()
                            tryAwaitRelease()
                            onPressRelease()
                        }
                    )
                },
            shape = CircleShape,
            color = if (isRecording) Color.Red else PrimaryBlue,
            shadowElevation = if (isRecording) 8.dp else 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }
        
        Text(
            text = if (isRecording) "Recording..." else "Press and hold to record",
            fontSize = 14.sp,
            fontWeight = if (isRecording) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

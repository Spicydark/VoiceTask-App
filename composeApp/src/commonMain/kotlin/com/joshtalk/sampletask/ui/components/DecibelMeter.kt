package com.joshtalk.sampletask.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshtalk.sampletask.ui.theme.GaugeBlue
import com.joshtalk.sampletask.ui.theme.GaugeRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun toRadians(degrees: Double): Double {
    return degrees * PI / 180.0
}

@Composable
fun DecibelMeter(
    currentDb: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = canvasWidth * 0.4f
            val strokeWidth = 40f
            
            val clampedDb = currentDb.coerceIn(0f, 60f)

            val blueSweep = 180f * (40f / 60f)
            val redSweep = 180f - blueSweep

            // Blue arc for safe range (0-40 dB)
            drawArc(
                color = GaugeBlue,
                startAngle = 180f,
                sweepAngle = blueSweep,
                useCenter = false,
                topLeft = Offset(
                    (canvasWidth - radius * 2) / 2,
                    (canvasHeight - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Red arc for noisy range (40-60 dB)
            drawArc(
                color = GaugeRed,
                startAngle = 180f + blueSweep,
                sweepAngle = redSweep,
                useCenter = false,
                topLeft = Offset(
                    (canvasWidth - radius * 2) / 2,
                    (canvasHeight - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Draw needle
            val angle = 180f + (clampedDb / 60f * 180f)
            val angleRad = toRadians(angle.toDouble())
            val needleLength = radius * 0.7f
            val needleStartX = canvasWidth / 2
            val needleStartY = canvasHeight / 2
            val needleEndX = needleStartX + (needleLength * cos(angleRad)).toFloat()
            val needleEndY = needleStartY + (needleLength * sin(angleRad)).toFloat()
            
            drawLine(
                color = Color.Gray,
                start = Offset(needleStartX, needleStartY),
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
        
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 30.dp)
        ) {
            Text(
                text = currentDb.toInt().toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "db",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Scale markers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .offset(y = (-20).dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0", fontSize = 14.sp)
            Text(text = "10", fontSize = 14.sp)
            Text(text = "20", fontSize = 14.sp)
            Text(text = "30", fontSize = 14.sp)
            Text(text = "40", fontSize = 14.sp)
            Text(text = "50", fontSize = 14.sp)
            Text(text = "60", fontSize = 14.sp)
        }
    }
}

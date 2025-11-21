package com.joshtalk.sampletask.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshtalk.sampletask.platform.NoiseDetector
import com.joshtalk.sampletask.ui.components.DecibelMeter
import com.joshtalk.sampletask.ui.theme.PrimaryBlue
import com.joshtalk.sampletask.ui.theme.SuccessGreen
import com.joshtalk.sampletask.ui.theme.ErrorRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoiseTestScreen(
    noiseDetector: NoiseDetector,
    onNavigateBack: () -> Unit,
    onNavigateToTaskSelection: () -> Unit
) {
    var currentDb by remember { mutableStateOf(0f) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var averageDb by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val passThreshold = 40f
    
    DisposableEffect(Unit) {
        onDispose {
            noiseDetector.release()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sample Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Test Ambient Noise Level",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Press start and keep quiet while we check if the noise is under 40 dB.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            DecibelMeter(currentDb = currentDb)
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Test result message
            testResult?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (averageDb < passThreshold) SuccessGreen else ErrorRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Start Test button
            Button(
                onClick = {
                    if (!isTesting) {
                        isTesting = true
                        testResult = null
                        val dbReadings = mutableListOf<Float>()
                        
                        noiseDetector.startDetection { db ->
                            currentDb = db
                            dbReadings.add(db)
                        }
                        
                        scope.launch {
                            delay(3000) // Test for 3 seconds
                            noiseDetector.stopDetection()
                            isTesting = false
                            averageDb = if (dbReadings.isNotEmpty()) {
                                dbReadings.average().toFloat()
                            } else {
                                0f
                            }
                            
                            if (averageDb < passThreshold) {
                                testResult = "Good to proceed"
                                delay(1000)
                                onNavigateToTaskSelection()
                            } else {
                                testResult = "Please move to a quieter place"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                enabled = !isTesting
            ) {
                Text(
                    text = if (isTesting) "Testing..." else "Start Test",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

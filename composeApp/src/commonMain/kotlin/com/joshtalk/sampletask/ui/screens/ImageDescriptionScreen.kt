package com.joshtalk.sampletask.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshtalk.sampletask.data.ApiService
import com.joshtalk.sampletask.data.TaskRepository
import com.joshtalk.sampletask.domain.Product
import com.joshtalk.sampletask.domain.TaskData
import com.joshtalk.sampletask.platform.AudioPlayer
import com.joshtalk.sampletask.platform.AudioRecorder
import com.joshtalk.sampletask.platform.RecordingState
import com.joshtalk.sampletask.ui.components.AudioPlaybackCard
import com.joshtalk.sampletask.ui.components.PlatformAsyncImage
import com.joshtalk.sampletask.ui.components.PressAndHoldMicButton
import com.joshtalk.sampletask.ui.theme.ErrorRed
import com.joshtalk.sampletask.ui.theme.PrimaryBlue
import com.joshtalk.sampletask.ui.theme.TextGray
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDescriptionScreen(
    apiService: ApiService,
    audioRecorder: AudioRecorder,
    audioPlayer: AudioPlayer,
    taskRepository: TaskRepository,
    onNavigateBack: () -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingResult by remember { mutableStateOf<RecordingState?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        apiService.getProducts().fold(
            onSuccess = { response ->
                product = response.products
                    .mapNotNull { prod ->
                        prod.images.firstOrNull { it.isNotBlank() }?.let { prod }
                    }
                    .randomOrNull()
                isLoading = false
            },
            onFailure = {
                errorMessage = "Failed to load image"
                isLoading = false
            }
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.release()
            audioPlayer.release()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recording Tasks") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Describe what you see in your native language",
                fontSize = 16.sp,
                color = TextGray
            )
            
            // Image display
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    PlatformAsyncImage(
                        model = product?.images?.firstOrNull { it.isNotBlank() },
                        contentDescription = "Image to describe",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mic button
            PressAndHoldMicButton(
                onPressStart = {
                    isRecording = true
                    recordingResult = null
                    errorMessage = null
                    audioRecorder.startRecording()
                },
                onPressRelease = {
                    isRecording = false
                    val result = audioRecorder.stopRecording()
                    recordingResult = result
                    
                    if (result is RecordingState.Error) {
                        errorMessage = result.message
                    }
                },
                isRecording = isRecording,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            // Recording playback
            if (recordingResult is RecordingState.Completed) {
                val completed = recordingResult as RecordingState.Completed
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Submitted Recording",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                AudioPlaybackCard(
                    audioPath = completed.filePath,
                    audioPlayer = audioPlayer,
                    onDelete = {
                        recordingResult = null
                        errorMessage = null
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            recordingResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Record again")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                val task = TaskData.ImageDescription(
                                    taskId = UUID.randomUUID().toString(),
                                    imageUrl = product?.images?.firstOrNull() ?: "",
                                    audioPath = completed.filePath,
                                    durationSec = completed.duration,
                                    timestamp = java.time.Instant.now().toString()
                                )
                                taskRepository.insertTask(task)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = recordingResult is RecordingState.Completed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

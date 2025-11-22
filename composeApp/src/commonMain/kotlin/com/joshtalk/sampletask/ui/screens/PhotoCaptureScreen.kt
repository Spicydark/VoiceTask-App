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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshtalk.sampletask.data.TaskRepository
import com.joshtalk.sampletask.domain.TaskData
import com.joshtalk.sampletask.platform.AudioPlayer
import com.joshtalk.sampletask.platform.AudioRecorder
import com.joshtalk.sampletask.platform.CameraProvider
import com.joshtalk.sampletask.platform.RecordingState
import com.joshtalk.sampletask.ui.components.AudioPlaybackCard
import com.joshtalk.sampletask.ui.components.PlatformAsyncImage
import com.joshtalk.sampletask.ui.components.PressAndHoldMicButton
import com.joshtalk.sampletask.ui.theme.ErrorRed
import com.joshtalk.sampletask.ui.theme.PrimaryBlue
import com.joshtalk.sampletask.ui.theme.TextGray
import kotlinx.coroutines.launch
import java.util.UUID

private const val MIC_PERMISSION_MESSAGE = "Microphone permission not granted. Please enable it in Settings to record."
private const val CAMERA_PERMISSION_MESSAGE = "Camera permission not granted. Please enable it in Settings to capture images."

/**
 * Screen for Photo Capture task where agent takes a photo and provides description.
 * Integrates CameraX for photo capture, allows text and optional audio description.
 * Requires text description before submission; audio description is optional but encouraged.
 * 
 * This task type tests both visual capture and descriptive skills, combining photography
 * with documentation capabilities essential for field work.
 * 
 * @param cameraProvider Platform camera service for photo capture
 * @param audioRecorder Platform audio recorder for optional voice description
 * @param audioPlayer Platform audio player for recording playback
 * @param taskRepository Database repository for persisting completed tasks
 * @param onNavigateBack Callback to return to task selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(
    cameraProvider: CameraProvider,
    audioRecorder: AudioRecorder,
    audioPlayer: AudioPlayer,
    taskRepository: TaskRepository,
    onNavigateBack: () -> Unit
) {
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var textDescription by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingResult by remember { mutableStateOf<RecordingState?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasMicPermission by remember { mutableStateOf(audioRecorder.hasPermission()) }
    var hasCameraPermission by remember { mutableStateOf(cameraProvider.hasPermission()) }
    
    val scope = rememberCoroutineScope()
    
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.release()
            audioPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        hasMicPermission = audioRecorder.hasPermission()
        hasCameraPermission = cameraProvider.hasPermission()
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Capture a photo and describe it in your language",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (capturedPhotoPath == null) {
                if (!hasCameraPermission) {
                    Text(
                        text = CAMERA_PERMISSION_MESSAGE,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            val permissionGranted = cameraProvider.hasPermission()
                            hasCameraPermission = permissionGranted
                            if (!permissionGranted) {
                                errorMessage = CAMERA_PERMISSION_MESSAGE
                                return@launch
                            }
                            cameraProvider.capturePhoto().fold(
                                onSuccess = { path ->
                                    capturedPhotoPath = path
                                    errorMessage = null
                                },
                                onFailure = { error ->
                                    errorMessage = "Failed to capture photo: ${error.message}"
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    enabled = hasCameraPermission
                ) {
                    Text("Capture Image", fontSize = 16.sp)
                }

                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    PlatformAsyncImage(
                        model = capturedPhotoPath,
                        contentDescription = "Captured photo preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = textDescription,
                    onValueChange = { textDescription = it },
                    label = { Text("Describe the photo in your language") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Optional: Record audio description",
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!hasMicPermission) {
                    Text(
                        text = MIC_PERMISSION_MESSAGE,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                PressAndHoldMicButton(
                    onPressStart = {
                        val permissionGranted = audioRecorder.hasPermission()
                        hasMicPermission = permissionGranted
                        if (!permissionGranted) {
                            errorMessage = MIC_PERMISSION_MESSAGE
                            recordingResult = null
                            isRecording = false
                            return@PressAndHoldMicButton
                        }
                        
                        isRecording = true
                        recordingResult = null
                        errorMessage = null
                        audioRecorder.startRecording()
                    },
                    onPressRelease = {
                        if (!isRecording) {
                            return@PressAndHoldMicButton
                        }
                        isRecording = false
                        val result = audioRecorder.stopRecording()
                        recordingResult = result
                        
                        if (result is RecordingState.Error) {
                            errorMessage = result.message
                        }
                    },
                    isRecording = isRecording
                )
                
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (recordingResult is RecordingState.Completed) {
                    val completed = recordingResult as RecordingState.Completed
                    AudioPlaybackCard(
                        audioPath = completed.filePath,
                        audioPlayer = audioPlayer,
                        onDelete = {
                            recordingResult = null
                            errorMessage = null
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            hasCameraPermission = cameraProvider.hasPermission()
                            capturedPhotoPath = null
                            textDescription = ""
                            recordingResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Retake Photo")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                val audioPath = (recordingResult as? RecordingState.Completed)?.filePath ?: ""
                                val duration = (recordingResult as? RecordingState.Completed)?.duration ?: 0
                                val trimmedDescription = textDescription.trim()
                                
                                val task = TaskData.PhotoCapture(
                                    taskId = UUID.randomUUID().toString(),
                                    description = trimmedDescription,
                                    imagePath = capturedPhotoPath ?: "",
                                    audioPath = audioPath,
                                    durationSec = duration,
                                    timestamp = java.time.Instant.now().toString()
                                )
                                taskRepository.insertTask(task)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = capturedPhotoPath != null && (textDescription.trim().isNotEmpty() || recordingResult is RecordingState.Completed),
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

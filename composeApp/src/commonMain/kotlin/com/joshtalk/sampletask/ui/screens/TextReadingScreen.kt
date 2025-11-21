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
import com.benasher44.uuid.Uuid
import com.joshtalk.sampletask.data.ApiService
import com.joshtalk.sampletask.data.TaskRepository
import com.joshtalk.sampletask.domain.Product
import com.joshtalk.sampletask.domain.TaskData
import com.joshtalk.sampletask.platform.AudioPlayer
import com.joshtalk.sampletask.platform.AudioRecorder
import com.joshtalk.sampletask.platform.RecordingState
import com.joshtalk.sampletask.ui.components.AudioPlaybackCard
import com.joshtalk.sampletask.ui.components.PressAndHoldMicButton
import com.joshtalk.sampletask.ui.theme.ErrorRed
import com.joshtalk.sampletask.ui.theme.PrimaryBlue
import com.joshtalk.sampletask.ui.theme.TextGray
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextReadingScreen(
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
    
    // Checkboxes
    var checkNoNoise by remember { mutableStateOf(false) }
    var checkNoMistakes by remember { mutableStateOf(false) }
    var checkHindi by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Load product on first composition
    LaunchedEffect(Unit) {
        apiService.getProducts().fold(
            onSuccess = { response ->
                product = response.products
                    .filter { it.description.isNotBlank() }
                    .randomOrNull()
                isLoading = false
            },
            onFailure = {
                errorMessage = "Failed to load text"
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Read the passage aloud in your native language (10-20 seconds).",
                fontSize = 16.sp,
                color = TextGray
            )
            
            // Text passage card
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = product?.description ?: "No text available",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
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
                    checkNoNoise = false
                    checkNoMistakes = false
                    checkHindi = false
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
            
            // Error message
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
                
                AudioPlaybackCard(
                    audioPath = completed.filePath,
                    audioPlayer = audioPlayer,
                    onDelete = {
                        recordingResult = null
                        errorMessage = null
                        checkNoNoise = false
                        checkNoMistakes = false
                        checkHindi = false
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Before Submitting listen audio and Check these or else Rerecord the passage",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Checkboxes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = checkNoNoise,
                        onCheckedChange = { checkNoNoise = it }
                    )
                    Text(text = "This Audio has no background noise", fontSize = 14.sp)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = checkNoMistakes,
                        onCheckedChange = { checkNoMistakes = it }
                    )
                    Text(text = "Passage has no mistakes in words and reading", fontSize = 14.sp)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = checkHindi,
                        onCheckedChange = { checkHindi = it }
                    )
                    Text(text = "Beech me koi galti nahi hai", fontSize = 14.sp)
                }
                
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
                            checkNoNoise = false
                            checkNoMistakes = false
                            checkHindi = false
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Record again")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                val task = TaskData.TextReading(
                                    taskId = Uuid.randomUUID().toString(),
                                    text = product?.description ?: "",
                                    audioPath = completed.filePath,
                                    durationSec = completed.duration,
                                    timestamp = java.time.Instant.now().toString()
                                )
                                taskRepository.insertTask(task)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = checkNoNoise && checkNoMistakes && checkHindi,
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

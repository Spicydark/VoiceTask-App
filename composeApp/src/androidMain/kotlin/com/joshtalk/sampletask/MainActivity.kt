package com.joshtalk.sampletask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.joshtalk.sampletask.data.TaskRepository
import com.joshtalk.sampletask.db.TaskDatabase
import com.joshtalk.sampletask.platform.AudioPlayer
import com.joshtalk.sampletask.platform.AudioRecorder
import com.joshtalk.sampletask.platform.CameraProvider
import com.joshtalk.sampletask.platform.NoiseDetector
import com.joshtalk.sampletask.platform.PlatformContext

/**
 * Main entry point for the Android application.
 * Initializes platform-specific services (audio recording/playback, camera, noise detection)
 * and sets up the SQLDelight database for task persistence.
 */
class MainActivity : ComponentActivity() {
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var noiseDetector: NoiseDetector
    private lateinit var cameraProvider: CameraProvider
    private lateinit var taskRepository: TaskRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val platformContext = PlatformContext(this)
        audioRecorder = AudioRecorder(platformContext)
        audioPlayer = AudioPlayer(platformContext)
        noiseDetector = NoiseDetector(platformContext)
        cameraProvider = CameraProvider(this, this)
        
        val driver = AndroidSqliteDriver(TaskDatabase.Schema, this, "task.db")
        val database = TaskDatabase(driver)
        taskRepository = TaskRepository(database)
        
        setContent {
            MaterialTheme {
                App(
                    audioRecorder = audioRecorder,
                    audioPlayer = audioPlayer,
                    noiseDetector = noiseDetector,
                    cameraProvider = cameraProvider,
                    taskRepository = taskRepository
                )
            }
        }
    }
    
    /**
     * Ensures proper cleanup of audio and noise detection resources when activity is destroyed.
     * Camera resources are managed by CameraX lifecycle and don't require explicit cleanup here.
     */
    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.release()
        audioPlayer.release()
        noiseDetector.release()
    }
}

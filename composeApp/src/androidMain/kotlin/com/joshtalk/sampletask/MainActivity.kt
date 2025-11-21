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

class MainActivity : ComponentActivity() {
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var noiseDetector: NoiseDetector
    private lateinit var cameraProvider: CameraProvider
    private lateinit var taskRepository: TaskRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize platform-specific components
        val platformContext = PlatformContext(this)
        audioRecorder = AudioRecorder(platformContext)
        audioPlayer = AudioPlayer(platformContext)
        noiseDetector = NoiseDetector(platformContext)
        cameraProvider = CameraProvider(this, this)
        
        // Initialize database
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
    
    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.release()
        audioPlayer.release()
        noiseDetector.release()
    }
}

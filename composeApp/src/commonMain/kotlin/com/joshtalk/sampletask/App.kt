package com.joshtalk.sampletask

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joshtalk.sampletask.data.ApiService
import com.joshtalk.sampletask.data.TaskRepository
import com.joshtalk.sampletask.navigation.Screen
import com.joshtalk.sampletask.platform.*
import com.joshtalk.sampletask.ui.screens.*

@Composable
fun App(
    audioRecorder: AudioRecorder,
    audioPlayer: AudioPlayer,
    noiseDetector: NoiseDetector,
    cameraProvider: CameraProvider,
    taskRepository: TaskRepository
) {
    val navController = rememberNavController()
    val apiService = remember { ApiService() }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        composable(Screen.Start.route) {
            StartScreen(
                onNavigateToNoiseTest = {
                    navController.navigate(Screen.NoiseTest.route)
                }
            )
        }
        
        composable(Screen.NoiseTest.route) {
            NoiseTestScreen(
                noiseDetector = noiseDetector,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskSelection = {
                    navController.navigate(Screen.TaskSelection.route) {
                        popUpTo(Screen.Start.route)
                    }
                }
            )
        }
        
        composable(Screen.TaskSelection.route) {
            TaskSelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTextReading = {
                    navController.navigate(Screen.TextReading.route)
                },
                onNavigateToImageDescription = {
                    navController.navigate(Screen.ImageDescription.route)
                },
                onNavigateToPhotoCapture = {
                    navController.navigate(Screen.PhotoCapture.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.TaskHistory.route)
                }
            )
        }
        
        composable(Screen.TextReading.route) {
            TextReadingScreen(
                apiService = apiService,
                audioRecorder = audioRecorder,
                audioPlayer = audioPlayer,
                taskRepository = taskRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ImageDescription.route) {
            ImageDescriptionScreen(
                apiService = apiService,
                audioRecorder = audioRecorder,
                audioPlayer = audioPlayer,
                taskRepository = taskRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.PhotoCapture.route) {
            PhotoCaptureScreen(
                cameraProvider = cameraProvider,
                audioRecorder = audioRecorder,
                audioPlayer = audioPlayer,
                taskRepository = taskRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TaskHistory.route) {
            TaskHistoryScreen(
                taskRepository = taskRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

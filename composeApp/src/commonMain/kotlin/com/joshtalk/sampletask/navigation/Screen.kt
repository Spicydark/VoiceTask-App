package com.joshtalk.sampletask.navigation

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object NoiseTest : Screen("noise_test")
    object TaskSelection : Screen("task_selection")
    object TextReading : Screen("text_reading")
    object ImageDescription : Screen("image_description")
    object PhotoCapture : Screen("photo_capture")
    object TaskHistory : Screen("task_history")
}

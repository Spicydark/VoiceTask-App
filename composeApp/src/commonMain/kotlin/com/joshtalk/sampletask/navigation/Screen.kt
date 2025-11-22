package com.joshtalk.sampletask.navigation

/**
 * Sealed class hierarchy defining all navigation destinations in the app.
 * Each object represents a unique screen with type-safe route string.
 * Used with Jetpack Compose Navigation for compile-time route verification.
 * 
 * Navigation flow: Start -> NoiseTest -> TaskSelection -> (Task screens | TaskHistory)
 */
sealed class Screen(val route: String) {
    /** Initial onboarding screen */
    object Start : Screen("start")
    
    /** Ambient noise gate check screen */
    object NoiseTest : Screen("noise_test")
    
    /** Task type selection hub */
    object TaskSelection : Screen("task_selection")
    
    /** Text reading task execution screen */
    object TextReading : Screen("text_reading")
    
    /** Image description task execution screen */
    object ImageDescription : Screen("image_description")
    
    /** Photo capture task execution screen */
    object PhotoCapture : Screen("photo_capture")
    
    /** Completed tasks history and statistics screen */
    object TaskHistory : Screen("task_history")
}

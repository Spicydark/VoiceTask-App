package com.joshtalk.sampletask.platform

/**
 * Cross-platform camera service for photo capture functionality.
 * Implementations use platform-native camera APIs (CameraX on Android, AVFoundation on iOS).
 * Handles permission checking and returns captured image file paths.
 */
expect class CameraProvider {
    /**
     * Captures a photo from the device camera and saves to local storage.
     * @return Result containing absolute file path on success, or exception on failure
     */
    suspend fun capturePhoto(): Result<String>
    
    /**
     * Checks if camera permission has been granted by the user.
     * @return true if permission granted, false otherwise
     */
    fun hasPermission(): Boolean
}

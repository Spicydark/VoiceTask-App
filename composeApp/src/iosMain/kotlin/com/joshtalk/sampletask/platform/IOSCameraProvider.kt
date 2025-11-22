package com.joshtalk.sampletask.platform

actual class CameraProvider actual constructor() {
    actual suspend fun capturePhoto(): Result<String> = Result.failure(UnsupportedOperationException("Camera capture not implemented on iOS yet"))

    actual fun hasPermission(): Boolean = false
}

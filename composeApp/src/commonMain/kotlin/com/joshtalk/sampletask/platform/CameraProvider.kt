package com.joshtalk.sampletask.platform

expect class CameraProvider {
    suspend fun capturePhoto(): Result<String>
    fun hasPermission(): Boolean
}

package com.joshtalk.sampletask.platform

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class CameraProvider(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var processCameraProvider: ProcessCameraProvider? = null

    actual suspend fun capturePhoto(): Result<String> {
        if (!hasPermission()) {
            return Result.failure(SecurityException("Camera permission not granted"))
        }

        ensureCameraBound()
        val capture = imageCapture ?: return Result.failure(IllegalStateException("Camera not initialized"))

        return suspendCoroutine { continuation ->
            val timestamp = System.currentTimeMillis()
            val photoFile = File(context.cacheDir, "photo_$timestamp.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            capture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(Result.success(photoFile.absolutePath))
                    }

                    override fun onError(exc: ImageCaptureException) {
                        continuation.resume(Result.failure(exc))
                    }
                }
            )
        }
    }

    private suspend fun ensureCameraBound() {
        if (imageCapture != null) return
        bindCamera()
    }

    private suspend fun bindCamera() = suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                processCameraProvider = cameraProvider

                imageCapture = ImageCapture.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                cameraProvider.unbindAll()
                val capture = imageCapture
                    ?: throw IllegalStateException("Failed to create ImageCapture")
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    capture
                )
                
                continuation.resume(Unit)
            } catch (exc: Exception) {
                continuation.resumeWithException(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    fun shutdown() {
        cameraExecutor.shutdown()
        processCameraProvider?.unbindAll()
        processCameraProvider = null
        imageCapture = null
    }
}

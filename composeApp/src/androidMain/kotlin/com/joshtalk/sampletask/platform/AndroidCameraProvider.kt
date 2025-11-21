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

/**
 * Android implementation of camera functionality using CameraX library.
 * Manages lazy camera initialization and photo capture with lifecycle awareness.
 * Photos are saved to the app's cache directory with timestamp-based filenames.
 */
actual class CameraProvider(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var processCameraProvider: ProcessCameraProvider? = null

    /**
     * Captures a photo from the device's back camera.
     * Lazily initializes CameraX on first call if not already bound.
     * @return Result containing absolute file path to captured image on success, or exception on failure
     */
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

    /**
     * Ensures CameraX is bound to lifecycle before attempting photo capture.
     * Performs lazy initialization - camera is only bound when first needed.
     */
    private suspend fun ensureCameraBound() {
        if (imageCapture != null) return
        bindCamera()
    }

    /**
     * Binds CameraX to the activity lifecycle with back camera and image capture use case.
     * Must be called from a coroutine context as it suspends until camera is ready.
     */
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
    
    /**
     * Checks if camera permission has been granted by the user.
     * @return true if CAMERA permission is granted, false otherwise
     */
    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Releases camera resources and shuts down the executor service.
     * Should be called when camera is no longer needed to free system resources.
     */
    fun shutdown() {
        cameraExecutor.shutdown()
        processCameraProvider?.unbindAll()
        processCameraProvider = null
        imageCapture = null
    }
}

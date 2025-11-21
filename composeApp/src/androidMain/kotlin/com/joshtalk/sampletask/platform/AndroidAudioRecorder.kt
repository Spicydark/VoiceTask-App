package com.joshtalk.sampletask.platform

// Final attempt to force update
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import kotlinx.coroutines.*

actual class AudioRecorder actual constructor(private val context: PlatformContext) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0
    private var currentState: RecordingState = RecordingState.Idle

    actual fun startRecording() {
        try {
            val timestamp = System.currentTimeMillis()
            outputFile = File(context.androidContext.cacheDir, "audio_$timestamp.mp3")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context.androidContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            currentState = RecordingState.Recording(0)
        } catch (e: Exception) {
            currentState = RecordingState.Error("Failed to start recording: ${e.message}")
        }
    }

    actual fun stopRecording(): RecordingState {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            val filePath = outputFile?.absolutePath ?: ""

            when {
                duration < 10 -> {
                    outputFile?.delete()
                    RecordingState.Error("Recording too short (min 10 s).")
                }
                duration > 20 -> {
                    outputFile?.delete()
                    RecordingState.Error("Recording too long (max 20 s).")
                }
                else -> {
                    RecordingState.Completed(filePath, duration)
                }
            }.also { currentState = it }
        } catch (e: Exception) {
            RecordingState.Error("Failed to stop recording: ${e.message}")
                .also { currentState = it }
        }
    }

    actual fun getCurrentState(): RecordingState {
        if (currentState is RecordingState.Recording) {
            val elapsed = System.currentTimeMillis() - startTime
            return RecordingState.Recording(elapsed)
        }
        return currentState
    }

    actual fun release() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}

actual class AudioPlayer actual constructor(private val context: PlatformContext) {
    private var mediaPlayer: android.media.MediaPlayer? = null

    actual fun play(filePath: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    actual fun pause() {
        mediaPlayer?.pause()
    }

    actual fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    actual fun getDuration(filePath: String): Int {
        return try {
            val mp = android.media.MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
            }
            val duration = mp.duration / 1000
            mp.release()
            duration
        } catch (e: Exception) {
            0
        }
    }
}

actual class NoiseDetector actual constructor(private val context: PlatformContext) {
    private var mediaRecorder: MediaRecorder? = null
    private var job: Job? = null

    actual fun startDetection(onNoiseLevel: (Float) -> Unit) {
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context.androidContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(File(context.androidContext.cacheDir, "temp_noise.3gp").absolutePath)
                prepare()
                start()
            }

            job = CoroutineScope(Dispatchers.Default).launch {
                while (isActive) {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    // Convert amplitude to decibels (rough approximation)
                    val db = if (amplitude > 0) {
                        20 * kotlin.math.log10(amplitude.toDouble())
                    } else {
                        0.0
                    }
                    onNoiseLevel(db.toFloat().coerceAtLeast(0f))
                    delay(100)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    actual fun stopDetection() {
        job?.cancel()
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    actual fun release() {
        stopDetection()
    }
}

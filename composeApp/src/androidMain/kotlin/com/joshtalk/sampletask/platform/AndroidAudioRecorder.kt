package com.joshtalk.sampletask.platform

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.File
import kotlinx.coroutines.*

/**
 * Android implementation of audio recording using MediaRecorder.
 * Enforces 10-20 second recording constraint and manages audio file lifecycle.
 * Files are stored in the app's cache directory as MPEG-4 AAC encoded audio.
 */
actual class AudioRecorder actual constructor(private val context: PlatformContext) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0
    private var currentState: RecordingState = RecordingState.Idle

    /**
     * Initiates audio recording from the device microphone.
     * Creates a timestamped audio file in cache directory and begins recording in AAC format.
     * Updates state to Recording on success or Error if initialization fails.
     */
    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context.androidContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

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

    /**
     * Stops the current recording session and validates duration constraints.
     * Enforces 10-20 second duration requirement. Files outside this range are deleted.
     * @return RecordingState indicating completion with file path and duration, or an error state
     */
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

    /**
     * Returns the current recording state with updated elapsed time if recording is active.
     * @return Current RecordingState with real-time elapsed milliseconds
     */
    actual fun getCurrentState(): RecordingState {
        if (currentState is RecordingState.Recording) {
            val elapsed = System.currentTimeMillis() - startTime
            return RecordingState.Recording(elapsed)
        }
        return currentState
    }

    /**
     * Releases MediaRecorder resources. Safe to call multiple times.
     * Should be called when the component is disposed or activity is destroyed.
     */
    actual fun release() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            // Silently ignore release errors as this is cleanup code
        }
    }
}

/**
 * Android implementation of audio playback using MediaPlayer.
 * Manages playback lifecycle of recorded audio files from local storage.
 */
actual class AudioPlayer actual constructor(private val context: PlatformContext) {
    private var mediaPlayer: android.media.MediaPlayer? = null

    /**
     * Plays an audio file from the given file path.
     * Stops any currently playing audio before starting the new file.
     * @param filePath Absolute path to the audio file to play
     */
    actual fun play(filePath: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Silently fail - file may not exist or be corrupted
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

    /**
     * Retrieves the duration of an audio file in seconds without playing it.
     * Creates a temporary MediaPlayer instance to read file metadata.
     * @param filePath Absolute path to the audio file
     * @return Duration in seconds, or 0 if file cannot be read
     */
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

/**
 * Android implementation for ambient noise level detection using MediaRecorder.
 * Continuously measures microphone amplitude and converts to decibel scale for noise testing.
 */
actual class NoiseDetector actual constructor(private val context: PlatformContext) {
    private var mediaRecorder: MediaRecorder? = null
    private var job: Job? = null

    /**
     * Starts continuous noise level monitoring from the device microphone.
     * Samples amplitude every 100ms and converts to decibel scale using logarithmic formula.
     * The conversion formula: dB = 20 * log10(amplitude) provides rough approximation suitable
     * for comparing relative noise levels, not calibrated to absolute SPL measurements.
     * @param onNoiseLevel Callback invoked every 100ms with current decibel reading (0-100+ range)
     */
    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context.androidContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

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
            // Silently fail - permissions may not be granted or mic unavailable
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

package com.joshtalk.sampletask.platform

/**
 * Platform-specific context wrapper. On Android wraps android.content.Context.
 * Used as a parameter to inject platform dependencies into expect/actual implementations.
 */
expect class PlatformContext

/**
 * Represents the current state of an audio recording session.
 * State transitions: Idle -> Recording -> (Completed | Error) -> Idle
 */
sealed class RecordingState {
    /** No recording is active */
    data object Idle : RecordingState()
    
    /** Recording is in progress with elapsed time in milliseconds */
    data class Recording(val elapsedTime: Long) : RecordingState()
    
    /** Recording completed successfully with validated duration (10-20 seconds) */
    data class Completed(val filePath: String, val duration: Int) : RecordingState()
    
    /** Recording failed or was out of bounds (duration constraints) */
    data class Error(val message: String) : RecordingState()
}

/**
 * Cross-platform audio recorder interface for capturing voice recordings.
 * Implementations enforce 10-20 second duration constraints for task compliance.
 */
expect class AudioRecorder(context: PlatformContext) {
    fun hasPermission(): Boolean
    fun startRecording()
    fun stopRecording(): RecordingState
    fun getCurrentState(): RecordingState
    fun release()
}

/**
 * Cross-platform audio player interface for playback of recorded audio files.
 */
expect class AudioPlayer(context: PlatformContext) {
    fun play(filePath: String)
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun getDuration(filePath: String): Int
}

/**
 * Cross-platform noise detector for ambient noise level monitoring.
 * Used during the pre-task noise gate check to ensure quiet environment (< 40 dB threshold).
 */
expect class NoiseDetector(context: PlatformContext) {
    fun hasPermission(): Boolean
    fun startDetection(onNoiseLevel: (Float) -> Unit)
    fun stopDetection()
    fun release()
}

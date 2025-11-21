package com.joshtalk.sampletask.platform

expect class PlatformContext

sealed class RecordingState {
    data object Idle : RecordingState()
    data class Recording(val elapsedTime: Long) : RecordingState()
    data class Completed(val filePath: String, val duration: Int) : RecordingState()
    data class Error(val message: String) : RecordingState()
}

expect class AudioRecorder(context: PlatformContext) {
    fun startRecording()
    fun stopRecording(): RecordingState
    fun getCurrentState(): RecordingState
    fun release()
}

expect class AudioPlayer(context: PlatformContext) {
    fun play(filePath: String)
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun getDuration(filePath: String): Int
}

expect class NoiseDetector(context: PlatformContext) {
    fun startDetection(onNoiseLevel: (Float) -> Unit)
    fun stopDetection()
    fun release()
}

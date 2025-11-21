package com.joshtalk.sampletask.platform

import kotlinx.coroutines.flow.MutableStateFlow

actual class AudioRecorder actual constructor(private val context: PlatformContext) {
    private var currentState: RecordingState = RecordingState.Idle

    actual fun startRecording() {
        // Stub implementation; integrate AVFoundation for real recording.
        currentState = RecordingState.Error("Recording not yet supported on iOS")
    }

    actual fun stopRecording(): RecordingState = currentState

    actual fun getCurrentState(): RecordingState = currentState

    actual fun release() {
        currentState = RecordingState.Idle
    }
}

actual class AudioPlayer actual constructor(private val context: PlatformContext) {
    private val isPlayingFlow = MutableStateFlow(false)

    actual fun play(filePath: String) {
        // Stub playback implementation.
        isPlayingFlow.value = false
    }

    actual fun pause() {
        isPlayingFlow.value = false
    }

    actual fun stop() {
        isPlayingFlow.value = false
    }

    actual fun release() {
        isPlayingFlow.value = false
    }

    actual fun isPlaying(): Boolean = isPlayingFlow.value

    actual fun getDuration(filePath: String): Int = 0
}

actual class NoiseDetector actual constructor(private val context: PlatformContext) {
    private val noiseLevel = MutableStateFlow(0f)

    actual fun startDetection(onNoiseLevel: (Float) -> Unit) {
        // No-op on iOS for now; report silence.
        noiseLevel.value = 0f
        onNoiseLevel(noiseLevel.value)
    }

    actual fun stopDetection() {
        noiseLevel.value = 0f
    }

    actual fun release() {
        stopDetection()
    }
}

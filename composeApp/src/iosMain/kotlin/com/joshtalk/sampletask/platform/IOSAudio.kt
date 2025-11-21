package com.joshtalk.sampletask.platform

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * iOS stub implementation of AudioRecorder.
 * Returns error state as AVFoundation integration is not yet implemented.
 * This allows the shared codebase to compile for iOS target while Android is production-ready.
 */
actual class AudioRecorder actual constructor(private val context: PlatformContext) {
    private var currentState: RecordingState = RecordingState.Idle

    actual fun startRecording() {
        currentState = RecordingState.Error("Recording not yet supported on iOS")
    }

    actual fun stopRecording(): RecordingState = currentState

    actual fun getCurrentState(): RecordingState = currentState

    actual fun release() {
        currentState = RecordingState.Idle
    }
}

/**
 * iOS stub implementation of AudioPlayer.
 * Playback operations are no-ops as AVAudioPlayer integration is pending.
 */
actual class AudioPlayer actual constructor(private val context: PlatformContext) {
    private val isPlayingFlow = MutableStateFlow(false)

    actual fun play(filePath: String) {
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

/**
 * iOS stub implementation of NoiseDetector.
 * Always reports 0 dB (silence) as AVAudioRecorder integration for noise sampling is pending.
 */
actual class NoiseDetector actual constructor(private val context: PlatformContext) {
    private val noiseLevel = MutableStateFlow(0f)

    actual fun startDetection(onNoiseLevel: (Float) -> Unit) {
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

package com.vanikanoon.app.voice

sealed class VoiceInputState {
    object Idle : VoiceInputState()
    object RequestingPermission : VoiceInputState()
    object Initializing : VoiceInputState()
    data class Listening(val amplitudeRms: Float = 0f, val durationSeconds: Int = 0) : VoiceInputState()
    data class Processing(val progressText: String = "Transcribing speech offline...") : VoiceInputState()
    data class Success(val recognizedText: String, val languageCode: String, val audioDurationMs: Long = 0) : VoiceInputState()
    data class Error(val message: String, val canRetry: Boolean = true) : VoiceInputState()
}

enum class VoiceMode {
    OFFLINE,
    ONLINE,
    AUTO
}

enum class MicState {
    IDLE,
    LISTENING,
    PROCESSING,
    SUCCESS,
    ERROR
}


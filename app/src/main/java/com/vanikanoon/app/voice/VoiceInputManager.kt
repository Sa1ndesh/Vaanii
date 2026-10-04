package com.vanikanoon.app.voice

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceInputManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {

    companion object {
        private const val TAG = "VaniKanoonVoice"

        @Volatile private var instance: VoiceInputManager? = null

        fun getInstance(context: Context): VoiceInputManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceInputManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val modelManager = SttModelManager(context)
    val sttEngine = LocalSttEngine(context, modelManager)
    val audioRecorder = AudioRecorder(context)

    var currentVoiceMode: VoiceMode = VoiceMode.OFFLINE

    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    private var activeRecordingJob: Job? = null
    var onAmplitudeUpdated: ((Float) -> Unit)? = null

    init {
        audioRecorder.onAmplitudeChanged = { amp ->
            onAmplitudeUpdated?.invoke(amp)
            val current = _state.value
            if (current is VoiceInputState.Listening) {
                _state.value = current.copy(amplitudeRms = amp)
            }
        }
    }

    fun isMicPermissionGranted(): Boolean {
        return audioRecorder.isPermissionGranted()
    }

    fun startListening(
        languageNameOrCode: String,
        maxDurationSeconds: Int = 15,
        onTranscriptionReady: ((String) -> Unit)? = null
    ) {
        if (!isMicPermissionGranted()) {
            _state.value = VoiceInputState.RequestingPermission
            return
        }

        if (_state.value is VoiceInputState.Listening || _state.value is VoiceInputState.Processing) {
            Log.w(TAG, "Already active in state ${_state.value}")
            return
        }

        _state.value = VoiceInputState.Initializing
        Log.i(TAG, "Initializing on-device voice capture for $languageNameOrCode")

        audioRecorder.startRecording(
            scope = coroutineScope,
            onStarted = {
                _state.value = VoiceInputState.Listening(durationSeconds = 0)
                Log.i(TAG, "Voice input now LISTENING")

                activeRecordingJob = coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val result = audioRecorder.recordUntilStopped(
                            maxDurationSeconds = maxDurationSeconds,
                            saveDebugWav = true
                        )

                        withContext(Dispatchers.Main) {
                            _state.value = VoiceInputState.Processing("Transcribing offline...")
                        }

                        val transcribedText = sttEngine.transcribeAudio(
                            floatSamples = result.floatData,
                            sampleRate = result.sampleRate,
                            languageNameOrCode = languageNameOrCode
                        )

                        withContext(Dispatchers.Main) {
                            if (transcribedText.isNotBlank()) {
                                _state.value = VoiceInputState.Success(
                                    recognizedText = transcribedText,
                                    languageCode = languageNameOrCode,
                                    audioDurationMs = result.durationMs
                                )
                                onTranscriptionReady?.invoke(transcribedText)
                            } else {
                                _state.value = VoiceInputState.Error("No clear speech detected. Please speak closer to microphone.")
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error during audio recording or offline transcription", e)
                        withContext(Dispatchers.Main) {
                            _state.value = VoiceInputState.Error(e.message ?: "Voice recognition failed")
                        }
                    }
                }
            },
            onError = { errMsg ->
                _state.value = VoiceInputState.Error(errMsg)
            }
        )
    }

    fun stopListeningAndTranscribe() {
        if (_state.value is VoiceInputState.Listening) {
            _state.value = VoiceInputState.Processing("Processing audio...")
            audioRecorder.stopRecording()
        }
    }

    fun cancel() {
        audioRecorder.stopRecording()
        activeRecordingJob?.cancel()
        activeRecordingJob = null
        _state.value = VoiceInputState.Idle
    }

    fun resetToIdle() {
        _state.value = VoiceInputState.Idle
    }
}

package com.vanikanoon.app.voice

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

/**
 * On-Device Speech-to-Text Engine powered by Sherpa-ONNX neural runtime.
 *
 * Performs 100% offline, local neural inference using an INT8-quantized multilingual
 * Whisper model on Android. Works completely with Airplane Mode enabled (Wi-Fi & Data OFF).
 */
class LocalSttEngine(
    private val context: Context,
    private val modelManager: SttModelManager
) {

    companion object {
        private const val TAG = "VaniKanoonVoice"

        fun mapLanguageToIso(rawLanguage: String): String {
            val normalized = rawLanguage.lowercase().trim()
            return when {
                normalized.startsWith("kn") || normalized.contains("kannada") || normalized.contains("ಕನ್ನಡ") -> "kn"
                normalized.startsWith("mr") || normalized.contains("marathi") || normalized.contains("मराठी") -> "mr"
                normalized.startsWith("hi") || normalized.contains("hindi") || normalized.contains("हिंदी") -> "hi"
                normalized.startsWith("en") || normalized.contains("english") -> "en"
                normalized.startsWith("te") || normalized.contains("telugu") || normalized.contains("తెలుగు") -> "te"
                normalized.startsWith("ta") || normalized.contains("tamil") || normalized.contains("தமிழ்") -> "ta"
                normalized.startsWith("bn") || normalized.contains("bengali") || normalized.contains("bangla") || normalized.contains("বাংলা") -> "bn"
                normalized.startsWith("gu") || normalized.contains("gujarati") || normalized.contains("ગુજરાતી") -> "gu"
                normalized.startsWith("ml") || normalized.contains("malayalam") || normalized.contains("മലയാളം") -> "ml"
                normalized.startsWith("pa") || normalized.contains("punjabi") || normalized.contains("ਪੰਜਾਬੀ") -> "pa"
                normalized.startsWith("or") || normalized.contains("odia") || normalized.contains("oriya") || normalized.contains("ଓଡ଼ಿଆ") -> "or"
                else -> "kn" // Default to Kannada for Vani-Kanoon primary region
            }
        }
    }

    data class LastInferenceDetails(
        val rawLanguageInput: String,
        val whisperLanguageCode: String,
        val encoderPath: String,
        val decoderPath: String,
        val tokensPath: String,
        val encoderSizeBytes: Long,
        val decoderSizeBytes: Long,
        val tokensSizeBytes: Long,
        val sampleRate: Int,
        val audioDurationMs: Long,
        val inferenceLatencyMs: Long,
        val transcribedText: String
    )

    private val engineMutex = Mutex()
    private var cachedRecognizer: OfflineRecognizer? = null
    private var currentConfiguredLanguage: String? = null

    @Volatile
    var lastInferenceDetails: LastInferenceDetails? = null
        private set

    /**
     * Initializes or retrieves the Sherpa-ONNX offline recognizer for the given language.
     */
    @Synchronized
    private fun getOrInitRecognizer(targetIsoLang: String, rawLanguageName: String): OfflineRecognizer {
        // Return cached instance if already initialized with matching language
        val existing = cachedRecognizer
        if (existing != null && currentConfiguredLanguage == targetIsoLang) {
            return existing
        }

        // Release prior instance if changing language
        existing?.release()
        cachedRecognizer = null

        // Verify model files exist on disk
        val isReady = modelManager.checkAndInitializeModel()
        if (!isReady || !modelManager.isModelReady()) {
            throw IllegalStateException(
                "Sherpa-ONNX neural model files are not ready. Please ensure model files are extracted."
            )
        }

        val encoderPath = modelManager.encoderFile.absolutePath
        val decoderPath = modelManager.decoderFile.absolutePath
        val tokensPath = modelManager.tokensFile.absolutePath

        // Critical logging required by specification
        Log.i(TAG, "STT DEBUG:")
        Log.i(TAG, "selectedLanguage = $rawLanguageName")
        Log.i(TAG, "whisperLanguageCode = $targetIsoLang")
        Log.i(TAG, "whisperTask = transcribe")
        Log.i(TAG, "encoderPath = $encoderPath")
        Log.i(TAG, "decoderPath = $decoderPath")
        Log.i(TAG, "tokensPath = $tokensPath")

        // Configure Whisper INT8 offline model
        val whisperConfig = OfflineWhisperModelConfig(
            encoder = encoderPath,
            decoder = decoderPath,
            language = targetIsoLang,
            task = "transcribe",
            tailPaddings = -1
        )

        // Low-memory, dual-thread CPU configuration optimized for mobile ARM (itel S23 / Cortex-A75/A55)
        val modelConfig = OfflineModelConfig(
            whisper = whisperConfig,
            tokens = tokensPath,
            numThreads = 2,
            debug = false,
            provider = "cpu",
            modelType = "whisper"
        )

        val recognizerConfig = OfflineRecognizerConfig(
            featConfig = FeatureConfig(sampleRate = 16000, featureDim = 80),
            modelConfig = modelConfig
        )

        val recognizer = OfflineRecognizer(
            assetManager = null,
            config = recognizerConfig
        )

        cachedRecognizer = recognizer
        currentConfiguredLanguage = targetIsoLang
        Log.i(TAG, "Sherpa-ONNX OfflineRecognizer successfully created for lang='$targetIsoLang'")
        return recognizer
    }

    /**
     * Transcribes raw normalized float PCM audio samples into text using Sherpa-ONNX neural inference.
     */
    suspend fun transcribeAudio(
        floatSamples: FloatArray,
        sampleRate: Int,
        languageNameOrCode: String
    ): String = withContext(Dispatchers.IO) {
        val isoLang = mapLanguageToIso(languageNameOrCode)
        val startTime = System.currentTimeMillis()
        val durationMs = if (sampleRate > 0) (floatSamples.size * 1000L) / sampleRate else 0L

        if (floatSamples.isEmpty()) {
            Log.w(TAG, "Cannot transcribe empty audio buffer.")
            return@withContext ""
        }

        // Basic silence check
        var maxAmp = 0.0f
        var sumAmp = 0.0
        for (sample in floatSamples) {
            val a = abs(sample)
            sumAmp += a
            if (a > maxAmp) maxAmp = a
        }
        val avgAmp = sumAmp / floatSamples.size
        if (avgAmp < 0.001f && maxAmp < 0.02f) {
            Log.i(TAG, "Audio buffer contains silence (avgAmp: $avgAmp, maxAmp: $maxAmp)")
            return@withContext ""
        }

        engineMutex.withLock {
            var stream: OfflineStream? = null
            try {
                val recognizer = getOrInitRecognizer(isoLang, languageNameOrCode)
                Log.i(TAG, "Running neural STT inference: ${floatSamples.size} samples @ ${sampleRate}Hz for $isoLang...")

                stream = recognizer.createStream()
                // Explicitly set language & task options on stream
                stream.setOption("language", isoLang)
                stream.setOption("task", "transcribe")
                stream.acceptWaveform(floatSamples, sampleRate)

                // Execute neural decoding
                recognizer.decode(stream)

                val result = recognizer.getResult(stream)
                val recognizedText = result.text.trim()
                val latencyMs = System.currentTimeMillis() - startTime

                Log.i(TAG, "Sherpa-ONNX inference completed in ${latencyMs}ms.")
                Log.i(TAG, "  Recognized Text: '$recognizedText'")

                lastInferenceDetails = LastInferenceDetails(
                    rawLanguageInput = languageNameOrCode,
                    whisperLanguageCode = isoLang,
                    encoderPath = modelManager.encoderFile.absolutePath,
                    decoderPath = modelManager.decoderFile.absolutePath,
                    tokensPath = modelManager.tokensFile.absolutePath,
                    encoderSizeBytes = modelManager.encoderFile.length(),
                    decoderSizeBytes = modelManager.decoderFile.length(),
                    tokensSizeBytes = modelManager.tokensFile.length(),
                    sampleRate = sampleRate,
                    audioDurationMs = durationMs,
                    inferenceLatencyMs = latencyMs,
                    transcribedText = recognizedText
                )

                recognizedText

            } catch (oom: OutOfMemoryError) {
                Log.e(TAG, "Out of memory during Sherpa-ONNX inference", oom)
                release()
                System.gc()
                throw IllegalStateException("Low device memory during voice inference. Please retry.", oom)
            } catch (e: Exception) {
                Log.e(TAG, "Sherpa-ONNX inference failed: ${e.message}", e)
                throw e
            } finally {
                try {
                    stream?.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing OfflineStream: ${e.message}")
                }
            }
        }
    }

    /**
     * Releases active native resources when engine is destroyed or low memory occurs.
     */
    fun release() {
        try {
            cachedRecognizer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing recognizer: ${e.message}")
        } finally {
            cachedRecognizer = null
            currentConfiguredLanguage = null
        }
    }
}

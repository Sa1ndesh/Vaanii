package com.vanikanoon.app.voice

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import com.k2fsa.sherpa.onnx.FeatureConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-Device Speech-to-Text Engine using official Sherpa-ONNX Android API (OfflineRecognizer & OfflineStream).
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
                normalized.startsWith("or") || normalized.contains("odia") || normalized.contains("oriya") || normalized.contains("ଓଡ଼ିଆ") -> "or"
                else -> "kn"
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

    var lastInferenceDetails: LastInferenceDetails? = null
        private set

    private var offlineRecognizer: OfflineRecognizer? = null

    @Synchronized
    private fun getOrCreateRecognizer(isoLang: String): OfflineRecognizer? {
        if (offlineRecognizer != null) return offlineRecognizer
        try {
            if (!modelManager.isModelReady()) {
                val initialized = modelManager.checkAndInitializeModel()
                if (!initialized) {
                    Log.e(TAG, "SttModelManager models not ready")
                    return null
                }
            }

            val encPath = modelManager.encoderFile.absolutePath
            val decPath = modelManager.decoderFile.absolutePath
            val tokPath = modelManager.tokensFile.absolutePath

            Log.i(TAG, "Initializing Sherpa-ONNX OfflineRecognizer with encoder=$encPath, decoder=$decPath, tokens=$tokPath, lang=$isoLang")

            val modelConfig = OfflineModelConfig(
                whisper = OfflineWhisperModelConfig(
                    encoder = encPath,
                    decoder = decPath,
                    language = isoLang,
                    task = "transcribe"
                ),
                tokens = tokPath,
                modelType = "whisper",
                numThreads = 2,
                debug = true
            )

            val featConfig = FeatureConfig(
                sampleRate = 16000,
                featureDim = 80
            )

            val config = OfflineRecognizerConfig(
                featConfig = featConfig,
                modelConfig = modelConfig
            )

            offlineRecognizer = OfflineRecognizer(context.assets, config)
            Log.i(TAG, "Sherpa-ONNX OfflineRecognizer successfully instantiated.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate Sherpa-ONNX OfflineRecognizer", e)
        }
        return offlineRecognizer
    }

    suspend fun transcribeAudio(
        floatSamples: FloatArray,
        sampleRate: Int,
        languageNameOrCode: String
    ): String = withContext(Dispatchers.IO) {
        val isoLang = mapLanguageToIso(languageNameOrCode)
        Log.i(TAG, "Transcribing audio samples (${floatSamples.size}, sampleRate=$sampleRate) for $isoLang using Sherpa-ONNX")
        if (floatSamples.isEmpty()) return@withContext ""

        val recognizer = getOrCreateRecognizer(isoLang)
        if (recognizer == null) {
            Log.e(TAG, "Sherpa-ONNX OfflineRecognizer is null. Cannot transcribe.")
            return@withContext ""
        }

        try {
            val startTime = System.currentTimeMillis()
            val stream = recognizer.createStream()
            stream.acceptWaveform(floatSamples, sampleRate)
            recognizer.decode(stream)
            val result = recognizer.getResult(stream)
            val text = result.text.trim()
            stream.release()

            val latency = System.currentTimeMillis() - startTime
            val audioDurationMs = (floatSamples.size.toLong() * 1000L) / sampleRate

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
                audioDurationMs = audioDurationMs,
                inferenceLatencyMs = latency,
                transcribedText = text
            )

            Log.i(TAG, "Sherpa-ONNX offline transcription success in ${latency}ms: '$text'")
            return@withContext text
        } catch (e: Exception) {
            Log.e(TAG, "Sherpa-ONNX decode error", e)
            return@withContext ""
        }
    }

    fun release() {
        try {
            offlineRecognizer?.release()
            offlineRecognizer = null
        } catch (_: Exception) {}
    }
}

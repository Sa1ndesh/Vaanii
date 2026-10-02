package com.vanikanoon.app.voice

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

enum class ModelStatus {
    MODEL_NOT_INSTALLED,
    MODEL_LOADING,
    MODEL_READY,
    MODEL_ERROR
}

data class SttModelInfo(
    val modelId: String = "sherpa-onnx-whisper-tiny-int8",
    val modelName: String = "Sherpa-ONNX Whisper Tiny Multilingual (INT8 Quantized)",
    val version: String = "1.0.0",
    val supportedLanguages: List<String> = listOf("kn", "mr", "hi", "en", "ta", "te", "bn", "gu", "ml", "pa", "or"),
    val status: ModelStatus = ModelStatus.MODEL_LOADING,
    val modelDirectory: File? = null,
    val encoderPath: String? = null,
    val decoderPath: String? = null,
    val tokensPath: String? = null,
    val totalSizeBytes: Long = 0,
    val errorMessage: String? = null
)

/**
 * Manages physical on-device neural speech model files for Sherpa-ONNX.
 *
 * Enforces strict verification:
 * - Checks existence and size of encoder, decoder, and tokens files.
 * - Extracts bundled models from Android assets into internal storage.
 * - Never reports MODEL_READY unless all binary neural network files exist with non-zero size.
 */
class SttModelManager(private val context: Context) {

    companion object {
        private const val TAG = "VaniKanoonVoice"
        const val MODELS_DIR_NAME = "models/stt"

        const val ENCODER_FILENAME = "tiny-encoder.int8.onnx"
        const val DECODER_FILENAME = "tiny-decoder.int8.onnx"
        const val TOKENS_FILENAME = "tiny-tokens.txt"

        // Minimum valid file sizes to prevent corrupted or empty stub files
        const val MIN_ENCODER_BYTES = 5_000_000L   // ~12.9 MB
        const val MIN_DECODER_BYTES = 30_000_000L  // ~85.6 MB
        const val MIN_TOKENS_BYTES = 100_000L      // ~798 KB
    }

    private val _modelInfo = MutableStateFlow(
        SttModelInfo(status = ModelStatus.MODEL_LOADING)
    )
    val modelInfo: StateFlow<SttModelInfo> = _modelInfo.asStateFlow()

    val modelsDir: File
        get() = File(context.filesDir, MODELS_DIR_NAME).apply { if (!exists()) mkdirs() }

    val encoderFile: File
        get() = File(modelsDir, ENCODER_FILENAME)

    val decoderFile: File
        get() = File(modelsDir, DECODER_FILENAME)

    val tokensFile: File
        get() = File(modelsDir, TOKENS_FILENAME)

    init {
        checkAndInitializeModel()
    }

    /**
     * Checks storage for valid model files. If absent or incomplete, extracts bundled assets.
     */
    fun checkAndInitializeModel(): Boolean {
        try {
            val dir = modelsDir

            // Step 1: Check if already properly unpacked in internal storage
            if (areModelFilesValid(dir)) {
                val totalSize = encoderFile.length() + decoderFile.length() + tokensFile.length()
                _modelInfo.value = SttModelInfo(
                    status = ModelStatus.MODEL_READY,
                    modelDirectory = dir,
                    encoderPath = encoderFile.absolutePath,
                    decoderPath = decoderFile.absolutePath,
                    tokensPath = tokensFile.absolutePath,
                    totalSizeBytes = totalSize,
                    errorMessage = null
                )
                Log.i(TAG, "Sherpa-ONNX model verified READY at ${dir.absolutePath} ($totalSize bytes)")
                return true
            }

            // Step 2: Unpack from app assets into filesDir
            _modelInfo.value = _modelInfo.value.copy(status = ModelStatus.MODEL_LOADING)
            Log.i(TAG, "Unpacking Sherpa-ONNX models from assets to ${dir.absolutePath}...")

            val copied = copyBundledAssetsToInternalStorage(dir)
            if (copied && areModelFilesValid(dir)) {
                val totalSize = encoderFile.length() + decoderFile.length() + tokensFile.length()
                _modelInfo.value = SttModelInfo(
                    status = ModelStatus.MODEL_READY,
                    modelDirectory = dir,
                    encoderPath = encoderFile.absolutePath,
                    decoderPath = decoderFile.absolutePath,
                    tokensPath = tokensFile.absolutePath,
                    totalSizeBytes = totalSize,
                    errorMessage = null
                )
                Log.i(TAG, "Successfully extracted and verified Sherpa-ONNX models ($totalSize bytes)")
                return true
            } else {
                val errorMsg = "Model files missing or incomplete in assets. Encoder: ${encoderFile.length()}, Decoder: ${decoderFile.length()}, Tokens: ${tokensFile.length()}"
                Log.e(TAG, errorMsg)
                _modelInfo.value = _modelInfo.value.copy(
                    status = ModelStatus.MODEL_ERROR,
                    errorMessage = errorMsg
                )
                return false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed initializing STT model files", e)
            _modelInfo.value = _modelInfo.value.copy(
                status = ModelStatus.MODEL_ERROR,
                errorMessage = e.message ?: "Failed loading neural model files"
            )
            return false
        }
    }

    private fun areModelFilesValid(dir: File): Boolean {
        val enc = File(dir, ENCODER_FILENAME)
        val dec = File(dir, DECODER_FILENAME)
        val tok = File(dir, TOKENS_FILENAME)

        return enc.exists() && enc.length() >= MIN_ENCODER_BYTES &&
               dec.exists() && dec.length() >= MIN_DECODER_BYTES &&
               tok.exists() && tok.length() >= MIN_TOKENS_BYTES
    }

    private fun copyBundledAssetsToInternalStorage(targetDir: File): Boolean {
        return try {
            val filesToCopy = listOf(ENCODER_FILENAME, DECODER_FILENAME, TOKENS_FILENAME)
            val assetPrefix = "models/stt"

            for (fileName in filesToCopy) {
                val destination = File(targetDir, fileName)
                // Skip if already valid
                val minSize = when (fileName) {
                    ENCODER_FILENAME -> MIN_ENCODER_BYTES
                    DECODER_FILENAME -> MIN_DECODER_BYTES
                    else -> MIN_TOKENS_BYTES
                }
                if (destination.exists() && destination.length() >= minSize) {
                    Log.d(TAG, "$fileName already present with size ${destination.length()} bytes")
                    continue
                }

                val assetPath = "$assetPrefix/$fileName"
                Log.i(TAG, "Copying $assetPath to ${destination.absolutePath}...")

                context.assets.open(assetPath).use { input: InputStream ->
                    FileOutputStream(destination).use { output: FileOutputStream ->
                        val buffer = ByteArray(64 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }
                Log.i(TAG, "Extracted $fileName (${destination.length()} bytes)")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed copying assets: ${e.message}", e)
            false
        }
    }

    suspend fun ensureModelReady(): Boolean = withContext(Dispatchers.IO) {
        checkAndInitializeModel()
    }

    fun isModelReady(): Boolean {
        return _modelInfo.value.status == ModelStatus.MODEL_READY && areModelFilesValid(modelsDir)
    }
}

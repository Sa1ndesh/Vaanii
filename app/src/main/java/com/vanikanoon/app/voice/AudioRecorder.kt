package com.vanikanoon.app.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

class AudioRecorder(private val context: Context) {

    companion object {
        private const val TAG = "VaniKanoonVoice"
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    data class RecordingResult(
        val pcmData: ByteArray,
        val floatData: FloatArray,
        val durationMs: Long,
        val sampleRate: Int = SAMPLE_RATE,
        val channels: Int = 1,
        val debugWavFile: File? = null
    )

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    @Volatile private var isRecording = false

    var onAmplitudeChanged: ((Float) -> Unit)? = null

    fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecording(
        scope: CoroutineScope,
        onStarted: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isPermissionGranted()) {
            val msg = "Microphone permission is required."
            Log.e(TAG, msg)
            onError(msg)
            return
        }

        if (isRecording) {
            Log.w(TAG, "Recording already in progress.")
            return
        }

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            val errorMsg = "AudioRecord parameter error. Buffer size invalid: $minBufferSize"
            Log.e(TAG, errorMsg)
            onError(errorMsg)
            return
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                val errorMsg = "AudioRecord failed to initialize (State != STATE_INITIALIZED)."
                Log.e(TAG, errorMsg)
                onError(errorMsg)
                return
            }

            audioRecord?.startRecording()
            isRecording = true
            Log.i(TAG, "AudioRecord started at $SAMPLE_RATE Hz, buffer size $bufferSize")
            onStarted()

        } catch (e: SecurityException) {
            val errorMsg = "Microphone permission error: ${e.message}"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
            return
        } catch (e: Exception) {
            val errorMsg = "Failed to start AudioRecord: ${e.message}"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
            return
        }
    }

    suspend fun recordUntilStopped(
        maxDurationSeconds: Int = 8,
        saveDebugWav: Boolean = true
    ): RecordingResult = withContext(Dispatchers.IO) {
        val record = audioRecord ?: throw IllegalStateException("AudioRecord not initialized")
        val pcmOutputStream = ByteArrayOutputStream()
        val shortBuffer = ShortArray(1024)
        val startTime = System.currentTimeMillis()
        var hasDetectedSpeech = false
        var lastSpeechTime = startTime

        try {
            while (isRecording && (System.currentTimeMillis() - startTime) < maxDurationSeconds * 1000L) {
                val shortsRead = record.read(shortBuffer, 0, shortBuffer.size)
                if (shortsRead > 0) {
                    val byteBuffer = ByteBuffer.allocate(shortsRead * 2).order(ByteOrder.LITTLE_ENDIAN)
                    var sumSquare = 0.0
                    for (i in 0 until shortsRead) {
                        val sample = shortBuffer[i]
                        byteBuffer.putShort(sample)
                        sumSquare += sample * sample
                    }
                    pcmOutputStream.write(byteBuffer.array())

                    // Calculate RMS amplitude normalized [0.0, 1.0]
                    val rms = sqrt(sumSquare / shortsRead) / 32768.0
                    val normalizedAmp = (rms * 4.0).coerceIn(0.0, 1.0).toFloat()
                    onAmplitudeChanged?.invoke(normalizedAmp)

                    val now = System.currentTimeMillis()
                    if (normalizedAmp > 0.08f) {
                        hasDetectedSpeech = true
                        lastSpeechTime = now
                    } else {
                        // If user spoke and then paused for 1.3s, stop recording and process speech
                        if (hasDetectedSpeech && (now - lastSpeechTime) > 1300L) {
                            Log.i(TAG, "Speech pause detected. Auto-stopping voice capture for quick processing.")
                            break
                        }
                    }
                } else if (shortsRead == AudioRecord.ERROR_INVALID_OPERATION || shortsRead == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "AudioRecord read error: $shortsRead")
                    break
                }
            }
        } finally {
            cleanupAudioRecord()
        }

        val pcmBytes = pcmOutputStream.toByteArray()
        val durationMs = System.currentTimeMillis() - startTime

        // Convert PCM 16-bit to FloatArray [-1.0f, 1.0f] for STT inference
        val shortCount = pcmBytes.size / 2
        val floatData = FloatArray(shortCount)
        val bb = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until shortCount) {
            floatData[i] = (bb.short / 32768.0f).coerceIn(-1.0f, 1.0f)
        }

        var wavFile: File? = null
        if (saveDebugWav && pcmBytes.isNotEmpty()) {
            try {
                wavFile = File(context.cacheDir, "debug_voice_sample_${System.currentTimeMillis()}.wav")
                writeWavHeader(wavFile, pcmBytes.size, SAMPLE_RATE, 1, 16)
                FileOutputStream(wavFile, true).use { fos ->
                    fos.write(pcmBytes)
                }
                Log.d(TAG, "Saved debug WAV to: ${wavFile.absolutePath}, size: ${wavFile.length()} bytes")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save debug WAV: ${e.message}")
            }
        }

        Log.i(TAG, "Recording completed: ${pcmBytes.size} bytes (${durationMs}ms)")
        RecordingResult(
            pcmData = pcmBytes,
            floatData = floatData,
            durationMs = durationMs,
            sampleRate = SAMPLE_RATE,
            channels = 1,
            debugWavFile = wavFile
        )
    }

    fun stopRecording() {
        isRecording = false
    }

    private fun cleanupAudioRecord() {
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord", e)
        } finally {
            audioRecord = null
            isRecording = false
        }
    }

    private fun writeWavHeader(
        file: File,
        totalAudioLen: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * (bitsPerSample / 8)

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 16 for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        FileOutputStream(file).use { it.write(header) }
    }
}

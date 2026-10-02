package com.vanikanoon.app.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

object OfflineSpeechHelper {
    private const val TAG = "OfflineSpeechHelper"

    data class LanguagePackInfo(
        val localeTag: String,
        val nameEn: String,
        val nameNative: String,
        var isInstalled: Boolean = true,
        var isDownloading: Boolean = false
    )

    val ESSENTIAL_LANGUAGE_PACKS = listOf(
        LanguagePackInfo("kn-IN", "Kannada", "ಕನ್ನಡ"),
        LanguagePackInfo("hi-IN", "Hindi", "हिन्दी"),
        LanguagePackInfo("en-IN", "English (India)", "English"),
        LanguagePackInfo("te-IN", "Telugu", "తెలుగు"),
        LanguagePackInfo("ta-IN", "Tamil", "தமிழ்"),
        LanguagePackInfo("mr-IN", "Marathi", "मराठी"),
        LanguagePackInfo("bn-IN", "Bengali", "বাংলা"),
        LanguagePackInfo("gu-IN", "Gujarati", "ગુજરાતી"),
        LanguagePackInfo("ml-IN", "Malayalam", "മലയാളം"),
        LanguagePackInfo("pa-IN", "Punjabi", "ਪੰਜਾਬੀ"),
        LanguagePackInfo("or-IN", "Odia", "ଓଡ଼ିଆ")
    )

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    fun isOnDeviceAvailable(context: Context): Boolean {
        return isRecognitionAvailable(context)
    }

    fun isRecognitionAvailable(context: Context): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(context)
        } catch (_: Exception) {
            false
        }
    }

    fun openVoiceSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    // =========================================================
    // CREATE BEST AVAILABLE SPEECH RECOGNIZER
    // =========================================================

    fun createBestSpeechRecognizer(
        context: Context,
        preferOnDevice: Boolean = true
    ): SpeechRecognizer? {

        // SpeechRecognizer must be created on main thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return null
        }

        return try {
            // Android 12 / API 31+
            if (
                preferOnDevice &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ) {
                // First preference: on-device recognition.
                if (
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(
                        context
                    )
                ) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(
                        context
                    )
                } else if (
                    SpeechRecognizer.isRecognitionAvailable(
                        context
                    )
                ) {
                    // Fallback to device speech recognizer.
                    SpeechRecognizer.createSpeechRecognizer(
                        context
                    )
                } else {
                    null
                }
            } else {
                // Older Android versions.
                if (
                    SpeechRecognizer.isRecognitionAvailable(
                        context
                    )
                ) {
                    SpeechRecognizer.createSpeechRecognizer(
                        context
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }


    // =========================================================
    // BUILD SPEECH RECOGNITION INTENT
    // =========================================================

    fun buildSpeechIntent(
        localeTag: String,
        prompt: String = "Speak your legal query...",
        preferOffline: Boolean = true
    ): Intent {

        val baseCode =
            localeTag
                .split("-", "_")
                .firstOrNull()
                ?.lowercase()
                ?: "en"

        val additionalLanguages =
            listOf(
                localeTag,
                "${baseCode}-IN",
                "${baseCode}_IN",
                baseCode,
                "hi-IN",
                "en-IN",
                Locale.getDefault().toLanguageTag()
            )
                .distinct()
                .toTypedArray()

        return Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // Selected language.
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                localeTag
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                localeTag
            )

            putExtra(
                "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                additionalLanguages
            )

            // Ask the speech service to work offline.
            putExtra(
                "android.speech.extra.PREFER_OFFLINE",
                preferOffline
            )

            // Partial text.
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )

            // Maximum result alternatives.
            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                prompt
            )

            putExtra(
                RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.vanikanoon.app"
            )

            // Dictation mode.
            putExtra(
                "android.speech.extra.DICTATION_MODE",
                true
            )
        }
    }


    // =========================================================
    // FRIENDLY ERROR MESSAGE
    // =========================================================

    fun getFriendlySpeechErrorMessage(
        context: Context,
        errorCode: Int,
        langNative: String = "",
        langEn: String = ""
    ): String {

        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO ->
                "Audio recording error. Please check your microphone."

            SpeechRecognizer.ERROR_CLIENT ->
                "Voice recognition reset. Please try again."

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission is required."

            SpeechRecognizer.ERROR_NO_MATCH ->
                "No speech detected. Please speak clearly."

            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "Listening timed out. Please speak again."

            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "Voice recognizer is busy. Please try again."

            SpeechRecognizer.ERROR_NETWORK ->
                "Network error. Offline speech requires internet or downloaded voice pack."

            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Speech recognition timed out."

            11, 12, 13, 14 ->
                if (langNative.isNotBlank()) {
                    "Offline voice pack for $langNative ($langEn) not installed on device (Error 12). Please install in Voice Settings or use typing."
                } else {
                    "Offline language pack not installed (Error 12). Please install in Voice Settings or use keyboard typing."
                }

            else ->
                "Voice recognition error: $errorCode (Language pack may need installation)"
        }
    }

    fun configureTtsLocale(tts: TextToSpeech?, localeTag: String): Locale {
        if (tts == null) return Locale.getDefault()
        return try {
            tts.setSpeechRate(0.92f)
            tts.setPitch(1.0f)
            val targetLocale = Locale.forLanguageTag(localeTag)
            if (tts.isLanguageAvailable(targetLocale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = targetLocale
                targetLocale
            } else {
                val baseCode = localeTag.split("-").firstOrNull() ?: "en"
                val baseLocale = Locale(baseCode)
                if (tts.isLanguageAvailable(baseLocale) >= TextToSpeech.LANG_AVAILABLE) {
                    tts.language = baseLocale
                    baseLocale
                } else {
                    val def = Locale.getDefault()
                    tts.language = def
                    def
                }
            }
        } catch (_: Exception) {
            Locale.getDefault()
        }
    }

    fun testLocalSpeech(
        context: Context,
        tts: TextToSpeech?,
        sampleText: String,
        localeTag: String,
        onStatus: (String) -> Unit
    ) {
        if (tts == null) {
            onStatus("Initializing local Text-to-Speech engine...")
            return
        }
        val configuredLocale = configureTtsLocale(tts, localeTag)
        val utteranceId = "TEST_LOCAL_TTS_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts.speak(sampleText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        onStatus("Playing local voice in ${configuredLocale.displayName}")
    }
}

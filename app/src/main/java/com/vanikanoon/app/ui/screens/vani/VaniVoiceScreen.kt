package com.vanikanoon.app.ui.screens.vani

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import com.vanikanoon.app.util.OfflineSpeechHelper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.vanikanoon.app.voice.VoiceInputManager
import com.vanikanoon.app.voice.VoiceInputState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import android.os.Handler
import android.os.Looper
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.data.auth.UserManager
import com.vanikanoon.app.data.knowledge.DialectRegistry
import com.vanikanoon.app.data.models.ChatMessage
import com.vanikanoon.app.data.models.LanguageCatalog
import com.vanikanoon.app.data.models.MessageRole
import com.vanikanoon.app.data.models.SupportedLanguage
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.components.VoiceMicVisualizer
import com.vanikanoon.app.ui.theme.LegalBgLight
import com.vanikanoon.app.ui.theme.LegalBlueDark
import com.vanikanoon.app.ui.theme.LegalBlueHighlight
import com.vanikanoon.app.ui.theme.LegalBlueLight
import com.vanikanoon.app.ui.theme.LegalBorder
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceVariant
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextMuted
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

private enum class VaniStep {
    LANGUAGE_SELECTION,
    STATE_SELECTION,
    DISTRICT_SELECTION,
    VOICE_CHAT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaniVoiceScreen(
    repository: LegalRepository,
    userManager: UserManager? = null,
    onBack: () -> Unit,
    onNavigateToOfflineStatus: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentUser by userManager?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }
    val userId = currentUser?.email?.ifBlank { "guest" } ?: "guest"

    var currentStep by rememberSaveable { mutableStateOf(VaniStep.LANGUAGE_SELECTION) }
    var selectedLanguageCode by rememberSaveable { mutableStateOf(LanguageCatalog.languages[0].code) }
    var selectedState by rememberSaveable { mutableStateOf("") }
    var selectedDistrict by rememberSaveable { mutableStateOf("") }
    var inputText by rememberSaveable { mutableStateOf("") }

    val selectedLanguage = remember(selectedLanguageCode) {
        LanguageCatalog.languages.find { it.code.equals(selectedLanguageCode, ignoreCase = true) }
            ?: LanguageCatalog.languages[0]
    }

    // Intercept back navigation so back press steps backwards smoothly without popping the entire screen
    BackHandler(enabled = currentStep != VaniStep.LANGUAGE_SELECTION) {
        when (currentStep) {
            VaniStep.VOICE_CHAT -> currentStep = VaniStep.DISTRICT_SELECTION
            VaniStep.DISTRICT_SELECTION -> currentStep = VaniStep.STATE_SELECTION
            VaniStep.STATE_SELECTION -> currentStep = VaniStep.LANGUAGE_SELECTION
            else -> onBack()
        }
    }

    var isThinking by remember { mutableStateOf(false) }
    val voiceInputManager = remember { VoiceInputManager.getInstance(context) }
    val voiceInputState by voiceInputManager.state.collectAsState()
    val isListening = voiceInputState is VoiceInputState.Listening
    val isProcessing = voiceInputState is VoiceInputState.Processing

    var activeMessageId by remember { mutableStateOf<String?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var currentChunkIndex by remember { mutableStateOf(0) }
    var speechChunks by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentLangTag by remember { mutableStateOf("en-IN") }
    var activeGenerationJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val cleanLang = selectedLanguage.code.uppercase()
    val cleanState = selectedState.ifBlank { "ALL" }.replace(Regex("[^A-Za-z0-9]"), "_").uppercase()
    val cleanDistrict = selectedDistrict.ifBlank { "ALL" }.replace(Regex("[^A-Za-z0-9]"), "_").uppercase()
    val channelId = "${userId}_VANI_VOICE_${cleanLang}_${cleanState}_${cleanDistrict}"
    
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    LaunchedEffect(channelId) {
        val loaded = repository.getChatMessages(channelId, userId).firstOrNull() ?: emptyList()
        chatMessages.clear()
        chatMessages.addAll(loaded)
    }

    // Android TTS Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    fun stopSpeaking() {
        try {
            ttsEngine?.stop()
        } catch (_: Exception) {}
        isSpeaking = false
        isPaused = false
        activeMessageId = null
        currentChunkIndex = 0
        speechChunks = emptyList()
    }

    fun handleSend(queryText: String) {
        if (queryText.isBlank() || isThinking) return
        val textToSend = queryText.trim()
        inputText = ""

        stopSpeaking()
        val job = scope.launch {
            android.util.Log.i("CHAT DEBUG", "User message added:\n$textToSend")
            val userMsg = ChatMessage(
                role = MessageRole.USER,
                text = textToSend,
                userId = userId
            )
            chatMessages.add(userMsg)
            repository.saveChatMessage(userMsg, channelId, userId)
            android.util.Log.i("CHAT DEBUG", "Messages count: ${chatMessages.size}")
            isThinking = true

            val botResponse = repository.askVaniVoice(
                language = selectedLanguage.code,
                state = selectedState,
                district = selectedDistrict,
                query = textToSend
            )
            android.util.Log.i("CHAT DEBUG", "Assistant message added:\n${botResponse.text}")
            val botMsgFinal = botResponse.copy(userId = userId)
            chatMessages.add(botMsgFinal)
            repository.saveChatMessage(botMsgFinal, channelId, userId)
            isThinking = false
            android.util.Log.i("CHAT DEBUG", "Final messages count: ${chatMessages.size}")

            // Scroll down
            listState.animateScrollToItem((chatMessages.size - 1).coerceAtLeast(0))
        }
        activeGenerationJob = job
    }

    LaunchedEffect(voiceInputState) {
        when (val s = voiceInputState) {
            is VoiceInputState.Success -> {
                android.util.Log.i("VOICE DEBUG", "STT result:\n${s.recognizedText}")
                android.util.Log.i("VOICE DEBUG", "Adding voice user message")
                inputText = s.recognizedText
                voiceInputManager.resetToIdle()
                if (s.recognizedText.isNotBlank() && !isThinking) {
                    android.util.Log.i("VOICE DEBUG", "Voice legal processing started")
                    handleSend(s.recognizedText)
                    android.util.Log.i("VOICE DEBUG", "Voice assistant answer received")
                }
            }
            is VoiceInputState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
                voiceInputManager.resetToIdle()
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceInputManager.cancel()
        }
    }

    fun stopAllOutput() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        isThinking = false
        stopSpeaking()
    }

    fun muteSpeaking() {
        try {
            ttsEngine?.stop()
        } catch (_: Exception) {}
        isSpeaking = false
        isPaused = true
    }

    fun speakCurrentChunk() {
        val tts = ttsEngine ?: return
        if (currentChunkIndex >= speechChunks.size) {
            isSpeaking = false
            isPaused = false
            currentChunkIndex = 0
            activeMessageId = null
            return
        }

        val chunkText = speechChunks[currentChunkIndex]
        val uId = "CHUNK_${activeMessageId}_$currentChunkIndex"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uId)
        }
        isSpeaking = true
        isPaused = false
        tts.speak(chunkText, TextToSpeech.QUEUE_FLUSH, params, uId)
    }

    fun resumeSpeaking() {
        val tts = ttsEngine ?: return
        if (activeMessageId == null || speechChunks.isEmpty()) return
        OfflineSpeechHelper.configureTtsLocale(tts, currentLangTag)
        isSpeaking = true
        isPaused = false
        speakCurrentChunk()
    }

    fun startSpeakingMessage(messageId: String, text: String, langTag: String) {
        val tts = ttsEngine ?: return
        try {
            tts.stop()
        } catch (_: Exception) {}

        val chunks = com.vanikanoon.app.util.SpeechSanitizer.splitIntoSpeechChunks(text)
        if (chunks.isEmpty()) return

        OfflineSpeechHelper.configureTtsLocale(tts, langTag)

        activeMessageId = messageId
        speechChunks = chunks
        currentChunkIndex = 0
        currentLangTag = langTag
        isSpeaking = true
        isPaused = false

        speakCurrentChunk()
    }

    fun toggleSpeaker(messageId: String, text: String, langTag: String) {
        if (activeMessageId == messageId) {
            if (isSpeaking) {
                muteSpeaking()
                Toast.makeText(context, "Muted. Tap speaker to resume from where you stopped.", Toast.LENGTH_SHORT).show()
            } else if (isPaused) {
                resumeSpeaking()
                Toast.makeText(context, "Resuming answer...", Toast.LENGTH_SHORT).show()
            } else {
                startSpeakingMessage(messageId, text, langTag)
            }
        } else {
            startSpeakingMessage(messageId, text, langTag)
        }
    }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize default
            }
        }
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    isSpeaking = true
                }
            }
            override fun onDone(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    if (utteranceId != null && utteranceId.startsWith("CHUNK_")) {
                        val parts = utteranceId.split("_")
                        if (parts.size >= 3) {
                            val msgId = parts[1]
                            val idx = parts[2].toIntOrNull() ?: -1
                            if (msgId == activeMessageId && isSpeaking && !isPaused) {
                                val nextIdx = idx + 1
                                if (nextIdx < speechChunks.size) {
                                    currentChunkIndex = nextIdx
                                    speakCurrentChunk()
                                } else {
                                    isSpeaking = false
                                    isPaused = false
                                    currentChunkIndex = 0
                                    activeMessageId = null
                                }
                            }
                        }
                    } else {
                        isSpeaking = false
                        isPaused = false
                    }
                }
            }
            override fun onError(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    isSpeaking = false
                    isPaused = false
                }
            }
        })
        ttsEngine = tts
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {}
        }
    }

    var showOfflineVoiceGuideDialog by remember { mutableStateOf(false) }

    fun openVoiceSettings() {
        OfflineSpeechHelper.openVoiceSettings(context)
    }

    // ===============================
    // MICROPHONE PERMISSION & ON-DEVICE VOICE
    // ===============================

    val speechRecognizerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val spokenText = results?.getOrNull(0) ?: ""
                android.util.Log.i("VOICE DEBUG", "Native SpeechRecognizer result: '$spokenText'")
                if (spokenText.isNotBlank()) {
                    inputText = spokenText
                    handleSend(spokenText)
                } else {
                    Toast.makeText(context, "No speech detected. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    fun launchSpeechRecognizer() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.speechLocaleTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your legal question in ${selectedLanguage.nameNative}...")
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            android.util.Log.w("VOICE DEBUG", "RecognizerIntent failed, falling back to VoiceInputManager: ${e.message}")
            voiceInputManager.startListening(
                languageNameOrCode = selectedLanguage.code,
                onTranscriptionReady = { recognized ->
                    inputText = recognized
                }
            )
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                launchSpeechRecognizer()
            } else {
                Toast.makeText(
                    context,
                    "Microphone permission is required for voice input.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    fun requestAndStartListening() {
        val permission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            launchSpeechRecognizer()
        } else {
            permissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("vani_voice_screen")
    ) {
        when (currentStep) {
            VaniStep.LANGUAGE_SELECTION -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        // Back to Home Navigation Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onBack,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = LegalDeepBlue.copy(alpha = 0.08f),
                                    contentColor = LegalDeepBlue
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("vani_back_to_home_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Back to Home", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(LegalDeepBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = LegalGoldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Vani-Kanoon Voice Assistant",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalTextPrimary
                            )
                        )
                        Text(
                            text = "Multilingual Legal Guidance with Regional Dialect Intelligence",
                            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "ಕೇಳಿ • विचारा • पूछिए • Ask in your language",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = LegalGoldDark,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LegalGoldContainer.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LegalGoldDark.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = LegalGoldDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Voice Assistant & Speech Playback Ready",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = LegalDeepBlue
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Speak and listen in your regional language. Powered directly by your device's built-in speech engine with zero language pack downloads required.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = LegalTextSecondary,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Step 1: Select Your Language",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(LanguageCatalog.languages) { lang ->
                        Card(
                            onClick = {
                                selectedLanguageCode = lang.code
                                selectedState = ""
                                selectedDistrict = ""
                                currentStep = VaniStep.STATE_SELECTION
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(1.dp, LegalBorder, RoundedCornerShape(16.dp))
                                .testTag("lang_select_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(lang.accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang.nameNative.take(2),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = lang.accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lang.nameNative,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = LegalTextPrimary
                                        )
                                    )
                                    Text(
                                        text = "${lang.nameEn} • Answers strictly in ${lang.nameNative}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = LegalSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = LegalTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            VaniStep.STATE_SELECTION -> {
                val states = DialectRegistry.getStatesForLanguage(selectedLanguage.code)
                var stateFilter by remember { mutableStateOf("") }
                val filteredStates = states.filter { it.contains(stateFilter, ignoreCase = true) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { currentStep = VaniStep.LANGUAGE_SELECTION },
                            modifier = Modifier.testTag("state_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Languages",
                                tint = LegalDeepBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Step 2: Select State",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalTextPrimary
                                )
                            )
                            Text(
                                text = "Choose state jurisdiction for ${selectedLanguage.nameNative} (${selectedLanguage.nameEn})",
                                style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (states.size > 4) {
                        OutlinedTextField(
                            value = stateFilter,
                            onValueChange = { stateFilter = it },
                            placeholder = { Text("Search state...", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LegalTextMuted) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LegalSurfaceWhite,
                                unfocusedContainerColor = LegalSurfaceWhite
                            )
                        )
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredStates) { st ->
                            val distCount = DialectRegistry.getDistricts(selectedLanguage.code, st).size
                            Card(
                                onClick = {
                                    selectedState = st
                                    selectedDistrict = ""
                                    currentStep = VaniStep.DISTRICT_SELECTION
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
                                    .testTag("state_item_$st")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = LegalGoldDark
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = st,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = LegalTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "$distCount Districts available in $st",
                                            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next",
                                        tint = LegalGoldDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            VaniStep.DISTRICT_SELECTION -> {
                val districts = DialectRegistry.getDistricts(selectedLanguage.code, selectedState)
                var districtFilter by remember { mutableStateOf("") }
                val filteredDistricts = districts.filter { it.contains(districtFilter, ignoreCase = true) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { currentStep = VaniStep.STATE_SELECTION },
                            modifier = Modifier.testTag("district_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to States",
                                tint = LegalDeepBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Step 3: Select District in $selectedState",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalTextPrimary
                                )
                            )
                            Text(
                                text = "Adapts regional dialect and statutory jurisdiction",
                                style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = districtFilter,
                        onValueChange = { districtFilter = it },
                        placeholder = { Text("Search district in $selectedState...", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LegalTextMuted) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LegalSurfaceWhite,
                            unfocusedContainerColor = LegalSurfaceWhite
                        )
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredDistricts) { dist ->
                            val info = DialectRegistry.getDialectInfo(selectedLanguage.code, dist)
                            Card(
                                onClick = {
                                    selectedDistrict = dist
                                    currentStep = VaniStep.VOICE_CHAT
                                    // Add initial greeting message in strictly selected language and state if empty
                                    scope.launch {
                                        val destCleanLang = selectedLanguage.code.uppercase()
                                        val destCleanState = selectedState.ifBlank { "ALL" }.replace(Regex("[^A-Za-z0-9]"), "_").uppercase()
                                        val destCleanDist = dist.ifBlank { "ALL" }.replace(Regex("[^A-Za-z0-9]"), "_").uppercase()
                                        val targetChannelId = "VANI_VOICE_${destCleanLang}_${destCleanState}_${destCleanDist}"

                                        val existing = repository.getChatMessages(targetChannelId, userId).firstOrNull() ?: emptyList()
                                        if (existing.isEmpty()) {
                                            val welcome = ChatMessage(
                                                role = MessageRole.ASSISTANT,
                                                text = "${info.greetingPhrase}!\n\n" + selectedLanguage.welcomeText,
                                                dialect = "${info.dialectName} • $dist ($selectedState)",
                                                userId = userId
                                            )
                                            repository.saveChatMessage(welcome, targetChannelId, userId)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
                                    .testTag("district_item_$dist")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = dist,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = LegalTextPrimary
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = LegalGoldContainer
                                        ) {
                                            Text(
                                                text = info.greetingPhrase,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = LegalGoldDark,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = info.dialectName + ": " + info.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            VaniStep.VOICE_CHAT -> {
                val dialectInfo = DialectRegistry.getDialectInfo(selectedLanguage.code, selectedDistrict)

                // 1. Context header badge with interactive breadcrumbs
                Surface(
                    color = LegalDeepBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Back to District/Steps Button
                            IconButton(
                                onClick = {
                                    voiceInputManager.cancel()
                                    currentStep = VaniStep.DISTRICT_SELECTION
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("voice_chat_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to District",
                                    tint = LegalGoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Language Chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LegalGoldPrimary,
                                modifier = Modifier.clickable {
                                    voiceInputManager.cancel()
                                    currentStep = VaniStep.LANGUAGE_SELECTION
                                }
                            ) {
                                Text(
                                    text = selectedLanguage.nameNative,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            // State Chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LegalBlueLight,
                                modifier = Modifier.clickable {
                                    voiceInputManager.cancel()
                                    currentStep = VaniStep.STATE_SELECTION
                                }
                            ) {
                                Text(
                                    text = selectedState,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = LegalSurfaceWhite,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            // District Chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LegalBlueLight,
                                modifier = Modifier.clickable {
                                    voiceInputManager.cancel()
                                    currentStep = VaniStep.DISTRICT_SELECTION
                                }
                            ) {
                                Text(
                                    text = selectedDistrict,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = LegalGoldPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            // Change Region Button (Single crisp line, never wraps vertically)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LegalGoldPrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, LegalGoldPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    voiceInputManager.cancel()
                                    selectedState = ""
                                    selectedDistrict = ""
                                    currentStep = VaniStep.LANGUAGE_SELECTION
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = LegalGoldPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Change Region",
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LegalGoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if (chatMessages.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.clearChatMessages(channelId, userId)
                                            val welcome = ChatMessage(
                                                role = MessageRole.ASSISTANT,
                                                text = "${dialectInfo.greetingPhrase}!\n\n" + selectedLanguage.welcomeText,
                                                dialect = "${dialectInfo.dialectName} • $selectedDistrict ($selectedState)",
                                                userId = userId
                                            )
                                            repository.saveChatMessage(welcome, channelId, userId)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Clear Chat",
                                        tint = LegalGoldPrimary.copy(alpha = 0.85f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🔒 Strict Language: Answers ONLY in ${selectedLanguage.nameNative} (${dialectInfo.dialectName})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = LegalSurfaceWhite.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                // 2. Chat history list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages) { message ->
                        android.util.Log.i("LEGAL DEBUG 7", "displayed message =\n${message.text}")
                        val isUser = message.role == MessageRole.USER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) LegalDeepBlue else LegalSurfaceWhite
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .widthIn(max = 320.dp)
                                    .border(
                                        if (!isUser && activeMessageId == message.id && isSpeaking) 2.dp else 1.dp,
                                        if (isUser) LegalDeepBlue else if (activeMessageId == message.id && isSpeaking) Color(0xFFDC2626) else if (activeMessageId == message.id && isPaused) LegalGoldDark else LegalBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .testTag(if (isUser) "user_msg_${message.id}" else "bot_msg_${message.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    if (!isUser && message.dialect != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = LegalGoldDark,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = message.dialect,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = LegalGoldDark,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isUser) LegalSurfaceWhite else LegalTextPrimary,
                                            lineHeight = 20.sp
                                        )
                                    )

                                    if (!isUser && message.relevantLaw != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = LegalGoldContainer,
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            Text(
                                                text = "⚖️ ${message.relevantLaw}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = LegalGoldDark,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (!isUser) {
                                        val isThisActive = activeMessageId == message.id
                                        val isThisSpeaking = isThisActive && isSpeaking
                                        val isThisPaused = isThisActive && isPaused

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isThisSpeaking) {
                                                Text(
                                                    text = "🔊 Speaking (${currentChunkIndex + 1}/${speechChunks.size.coerceAtLeast(1)})",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFFDC2626),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            } else if (isThisPaused) {
                                                Text(
                                                    text = "⏸️ Muted at part ${currentChunkIndex + 1}/${speechChunks.size.coerceAtLeast(1)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = LegalGoldDark,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isThisSpeaking) {
                                                    // MUTE BUTTON: Pauses & remembers position
                                                    Button(
                                                        onClick = {
                                                            toggleSpeaker(message.id, message.text, selectedLanguage.ttsLocaleTag)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                        shape = RoundedCornerShape(16.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .height(30.dp)
                                                            .testTag("speaker_mute_button_${message.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.VolumeOff,
                                                            contentDescription = "Mute",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Mute",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        )
                                                    }
                                                } else if (isThisPaused) {
                                                    // RESUME BUTTON: Restarts right from where stopped
                                                    Button(
                                                        onClick = {
                                                            toggleSpeaker(message.id, message.text, selectedLanguage.ttsLocaleTag)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = LegalDeepBlue,
                                                            contentColor = LegalGoldPrimary
                                                        ),
                                                        shape = RoundedCornerShape(16.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .height(30.dp)
                                                            .testTag("speaker_resume_button_${message.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Resume",
                                                            tint = LegalGoldPrimary,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Resume",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = LegalGoldPrimary,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    // IDLE: Click to speak answer
                                                    IconButton(
                                                        onClick = {
                                                            toggleSpeaker(message.id, message.text, selectedLanguage.ttsLocaleTag)
                                                        },
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .testTag("speak_output_button_${message.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.VolumeUp,
                                                            contentDescription = "Speak Answer",
                                                            tint = LegalBlueHighlight,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isThinking) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = LegalGoldPrimary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = selectedLanguage.thinkingText,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = LegalTextSecondary,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Offline Voice Assistant Quick Prompts Bar (Always Available Offline)
                Surface(
                    color = LegalBgLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = LegalGoldContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LegalGoldDark.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { showOfflineVoiceGuideDialog = true }
                                    .testTag("voice_settings_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Voice Settings",
                                        tint = LegalGoldDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Voice Settings",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LegalGoldDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFECFDF5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clickable { onNavigateToOfflineStatus() }
                                    .testTag("offline_diagnostics_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Diagnostics",
                                        tint = Color(0xFF047857),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Offline Status & Test",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF047857),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        items(selectedLanguage.sampleQuestions) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = LegalSurfaceWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LegalBorder),
                                modifier = Modifier
                                    .clickable { handleSend(prompt) }
                                    .testTag("voice_quick_prompt")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = LegalBlueHighlight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LegalDeepBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Voice & Text Input Bar
                Surface(
                    color = LegalSurfaceWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (isSpeaking || isThinking) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = {
                                        stopAllOutput()
                                        Toast.makeText(context, if (isThinking) "Generation stopped" else "Voice output stopped", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("global_voice_stop_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.StopCircle,
                                        contentDescription = "Stop",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isThinking) "⏹️ Stop Generating" else "⏹️ Stop Voice Playback",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        if (isListening) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🎙️ ${selectedLanguage.nameNative} (AudioRecord) - Speak now...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LegalDeepBlue,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            voiceInputManager.stopListeningAndTranscribe()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LegalDeepBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Done", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        if (isProcessing) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LegalGoldContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LegalGoldDark.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = LegalGoldDark
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🧠 On-Device STT transcribing speech offline...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LegalDeepBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        // Quick Question Chips for 1-Tap Answers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickQuestions = when (selectedLanguage.code.lowercase()) {
                                "kannada" -> listOf(
                                    "ಝೀರೋ ಎಫ್‌ಐಆರ್ ದಾಖಲಿಸುವ ವಿಧಾನ?",
                                    "ಮನೆ ಖಾಲಿ ಮಾಡಿಸುವ ನಿಯಮಗಳು?",
                                    "ಹೆಣ್ಣುಮಕ್ಕಳ ಆಸ್ತಿ ಹಕ್ಕು?",
                                    "ಚೆಕ್ ಬೌನ್ಸ್ ಆದರೆ ಏನು ಮಾಡಬೇಕು?",
                                    "ಜಾಮೀನು ಪಡೆಯುವ ಹಕ್ಕು?"
                                )
                                "marathi" -> listOf(
                                    "झिरो एफआयआर कशी नोंदवावी?",
                                    "घरमालक जबरदस्तीने काढू शकतो का?",
                                    "वडिलोपार्जित मालमत्ता अधिकार?",
                                    "चेक बाऊन्स कलम १३८ काय आहे?",
                                    "जामीन मिळवण्याचे नियम?"
                                )
                                "hindi" -> listOf(
                                    "जीरो एफआईआर कैसे दर्ज करें?",
                                    "किरायेदार के कानूनी अधिकार?",
                                    "पैतृक संपत्ति में बेटियों का हक?",
                                    "चेक बाउंस होने पर क्या करें?",
                                    "जमानत लेने की प्रक्रिया?"
                                )
                                else -> listOf(
                                    "What is Zero FIR procedure?",
                                    "Can landlord evict without notice?",
                                    "Daughter rights in ancestral property?",
                                    "Cheque bounce Sec 138 notice?",
                                    "How to get anticipatory bail?"
                                )
                            }

                            quickQuestions.forEach { qq ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = LegalGoldContainer.copy(alpha = 0.7f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LegalGoldDark.copy(alpha = 0.3f)),
                                    modifier = Modifier.clickable {
                                        if (!isThinking) {
                                            handleSend(qq)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = qq,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LegalDeepBlue,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VoiceMicVisualizer(
                                isListening = isListening,
                                isProcessing = isProcessing,
                                onClick = {
                                    if (isListening) {
                                        voiceInputManager.stopListeningAndTranscribe()
                                    } else {
                                        requestAndStartListening()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        text = if (isListening) "Listening to voice..." else selectedLanguage.placeholderText,
                                        style = MaterialTheme.typography.bodySmall.copy(color = LegalTextMuted),
                                        maxLines = 1
                                    )
                                },
                                shape = RoundedCornerShape(24.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (inputText.isNotBlank() && !isThinking) {
                                        handleSend(inputText)
                                    }
                                }),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LegalGoldPrimary,
                                    unfocusedBorderColor = LegalBorder,
                                    focusedContainerColor = LegalBgLight,
                                    unfocusedContainerColor = LegalBgLight
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("voice_text_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { handleSend(inputText) },
                                enabled = inputText.isNotBlank() && !isThinking,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (inputText.isNotBlank() && !isThinking) LegalDeepBlue else LegalSurfaceVariant)
                                    .testTag("send_voice_query_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Query",
                                    tint = if (inputText.isNotBlank() && !isThinking) LegalGoldPrimary else LegalTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOfflineVoiceGuideDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineVoiceGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = LegalGoldDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Voice Setup: ${selectedLanguage.nameNative}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "When offline, Google Speech Services requires downloading the language pack once on your phone to transcribe voice without Internet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = LegalTextPrimary)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LegalGoldContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "How to enable 100% Offline Voice for ${selectedLanguage.nameNative}:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalDeepBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. Tap 'Download Voice Pack' below to open Google Speech Settings.\n2. Tap 'Offline speech recognition' -> 'ALL' tab.\n3. Find '${selectedLanguage.nameNative} (${selectedLanguage.nameEn})' and tap Download (~12 MB).\n4. Return here: Voice input will now work completely offline without Internet!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = LegalTextPrimary,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                    Text(
                        text = "💡 Tip: You can also tap the text box below and use the keyboard microphone 🎤 or pick from the suggested legal questions.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LegalTextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOfflineVoiceGuideDialog = false
                        openVoiceSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LegalDeepBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Voice Pack", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineVoiceGuideDialog = false }) {
                    Text("OK, Got It", color = LegalDeepBlue)
                }
            }
        )
    }
}

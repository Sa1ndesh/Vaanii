package com.vanikanoon.app.ui.screens.chat

import android.app.Activity
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import com.vanikanoon.app.util.OfflineSpeechHelper
import com.vanikanoon.app.voice.VoiceInputManager
import com.vanikanoon.app.voice.VoiceInputState
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.LocationOn
import com.vanikanoon.app.data.knowledge.DialectRegistry
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.vanikanoon.app.data.auth.UserManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.data.models.ChatMessage
import com.vanikanoon.app.data.models.LanguageCatalog
import com.vanikanoon.app.data.models.MessageRole
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.theme.LegalBgLight
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatbotScreen(
    repository: LegalRepository,
    userManager: UserManager? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentUser by userManager?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }
    val userId = currentUser?.email?.ifBlank { "guest" } ?: "guest"

    var inputText by rememberSaveable { mutableStateOf("") }
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
    var currentLocaleTag by remember { mutableStateOf("en-IN") }
    var activeGenerationJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var selectedLanguageCode by rememberSaveable { mutableStateOf("english") }
    var selectedState by rememberSaveable { mutableStateOf("All India") }
    var showStateDialog by remember { mutableStateOf(false) }

    val cleanChatState = selectedState.ifBlank { "ALL" }.replace(Regex("[^A-Za-z0-9]"), "_").uppercase()
    val channelId = "${userId}_CHATBOT_${selectedLanguageCode.uppercase()}_${cleanChatState}"
    
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    LaunchedEffect(channelId) {
        val loaded = repository.getChatMessages(channelId, userId).firstOrNull() ?: emptyList()
        chatMessages.clear()
        chatMessages.addAll(loaded)
    }

    // Speech Recognizer setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var isOnDeviceAvailable by remember { mutableStateOf(false) }

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

    fun handleSend(query: String) {
        if (query.isBlank() || isThinking) return
        val messageText = query.trim()
        inputText = ""

        stopSpeaking()
        val job = scope.launch {
            android.util.Log.i("CHAT DEBUG", "User message added:\n$messageText")
            val userMsg = ChatMessage(role = MessageRole.USER, text = messageText, userId = userId)
            chatMessages.add(userMsg)
            repository.saveChatMessage(userMsg, channelId, userId)
            android.util.Log.i("CHAT DEBUG", "Messages count: ${chatMessages.size}")
            isThinking = true

            val botMsg = repository.askChatbot(
                query = messageText,
                history = chatMessages.toList(),
                language = selectedLanguageCode,
                stateName = selectedState
            )
            android.util.Log.i("CHAT DEBUG", "Assistant message added:\n${botMsg.text}")
            val botMsgFinal = botMsg.copy(userId = userId)
            chatMessages.add(botMsgFinal)
            repository.saveChatMessage(botMsgFinal, channelId, userId)
            isThinking = false
            android.util.Log.i("CHAT DEBUG", "Final messages count: ${chatMessages.size}")

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
        val uId = "CHAT_CHUNK_${activeMessageId}_$currentChunkIndex"
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
        val locale = java.util.Locale.forLanguageTag(currentLocaleTag)
        tts.language = locale
        isSpeaking = true
        isPaused = false
        speakCurrentChunk()
    }

    fun startSpeakingMessage(messageId: String, text: String, tag: String) {
        val tts = ttsEngine ?: return
        try {
            tts.stop()
        } catch (_: Exception) {}

        val chunks = com.vanikanoon.app.util.SpeechSanitizer.splitIntoSpeechChunks(text)
        if (chunks.isEmpty()) return

        val locale = java.util.Locale.forLanguageTag(tag)
        tts.language = locale

        activeMessageId = messageId
        speechChunks = chunks
        currentChunkIndex = 0
        currentLocaleTag = tag
        isSpeaking = true
        isPaused = false

        speakCurrentChunk()
    }

    fun toggleSpeaker(messageId: String, text: String, tag: String) {
        if (activeMessageId == messageId) {
            if (isSpeaking) {
                muteSpeaking()
                Toast.makeText(context, "Muted. Tap speaker to resume from where you stopped.", Toast.LENGTH_SHORT).show()
            } else if (isPaused) {
                resumeSpeaking()
                Toast.makeText(context, "Resuming answer...", Toast.LENGTH_SHORT).show()
            } else {
                startSpeakingMessage(messageId, text, tag)
            }
        } else {
            startSpeakingMessage(messageId, text, tag)
        }
    }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
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
                    if (utteranceId != null && utteranceId.startsWith("CHAT_CHUNK_")) {
                        val parts = utteranceId.split("_")
                        if (parts.size >= 4) {
                            val msgId = parts[2]
                            val idx = parts[3].toIntOrNull() ?: -1
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

    val localeTag = when (selectedLanguageCode) {
        "kannada" -> "kn-IN"
        "marathi" -> "mr-IN"
        "hindi" -> "hi-IN"
        "tamil" -> "ta-IN"
        "telugu" -> "te-IN"
        "bengali" -> "bn-IN"
        "gujarati" -> "gu-IN"
        "malayalam" -> "ml-IN"
        "punjabi" -> "pa-IN"
        "odia" -> "or-IN"
        else -> "en-IN"
    }

    val speechActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.getOrNull(0) ?: ""
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
            val localeTag = when (selectedLanguageCode.lowercase()) {
                "kannada" -> "kn-IN"
                "marathi" -> "mr-IN"
                "hindi" -> "hi-IN"
                else -> "en-IN"
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your legal question in $selectedLanguageCode...")
            }
            speechActivityLauncher.launch(intent)
        } catch (e: Exception) {
            voiceInputManager.startListening(
                languageNameOrCode = selectedLanguageCode,
                onTranscriptionReady = { recognized ->
                    inputText = recognized
                }
            )
        }
    }

    // ===============================
    // MICROPHONE PERMISSION & ON-DEVICE VOICE
    // ===============================

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

    val promptSuggestions = when (selectedLanguageCode) {
        "kannada" -> listOf(
            "ಜೀರೋ ಎಫ್‌ಐಆರ್ (Zero FIR) ದಾಖಲಿಸುವ ಪ್ರಕ್ರಿಯೆ ಏನು?",
            "ಬಾಡಿಗೆದಾರರನ್ನು ನೋಟಿಸ್ ಇಲ್ಲದೆ ಖಾಲಿ ಮಾಡಿಸಬಹುದೇ?",
            "ಚೆಕ್ ಬೌನ್ಸ್ ಪ್ರಕರಣದಲ್ಲಿ ಸೆಕ್ಷನ್ 138 ಅಡಿಯಲ್ಲಿ ಪರಿಹಾರವೇನು?",
            "ಗ್ರಾಹಕ ನ್ಯಾಯಾಲಯದಲ್ಲಿ ಆನ್‌ಲೈನ್ ದೂರು ಸಲ್ಲಿಸುವುದು ಹೇಗೆ?"
        )
        "marathi" -> listOf(
            "झिरो एफआयआर (Zero FIR) नोंदवण्याची कायदेशीर पद्धत काय आहे?",
            "घरमालक भाडेकरूला पूर्वसूचनेशिवाय बाहेर काढू शकतो का?",
            "चेक बाऊन्स प्रकरणात कलम १३८ अन्वये कायदेशीर उपाय काय आहेत?",
            "ग्राहक न्यायालयात ऑनलाईन तक्रार कशी नोंदवावी?"
        )
        "hindi" -> listOf(
            "जीरो एफआईआर (Zero FIR) दर्ज कराने की क्या प्रक्रिया है?",
            "क्या मकान मालिक बिना नोटिस के किराएदार को निकाल सकता है?",
            "चेक बाउंस मामले में धारा 138 एनआई एक्ट के तहत क्या कानूनी उपाय हैं?",
            "उपभोक्ता फोरम (e-Daakhil) में ऑनलाइन शिकायत कैसे दर्ज करें?"
        )
        else -> listOf(
            "What is the procedure to file a Zero FIR?",
            "Can a landlord evict a tenant without notice?",
            "What is Section 138 NI Act for cheque bounce?",
            "How to file a consumer complaint online in e-Daakhil?"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("chatbot_screen")
    ) {
        // Top Toolbar action strip
        Surface(
            color = LegalSurfaceWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vani-Kanoon AI Legal Intelligence",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                    }

                    if (chatMessages.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                scope.launch { repository.clearChatMessages(channelId, userId) }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Chat",
                                tint = LegalTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Language Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "kannada" to "ಕನ್ನಡ",
                        "marathi" to "मराठी",
                        "hindi" to "हिंदी",
                        "english" to "English"
                    ).forEach { (code, label) ->
                        val isSelected = selectedLanguageCode == code
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) LegalDeepBlue else LegalSurfaceVariant,
                            modifier = Modifier
                                .clickable { selectedLanguageCode = code }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) LegalGoldPrimary else LegalTextSecondary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // State Jurisdiction Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "State:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalTextMuted
                        )
                    )
                    listOf("All India", "Bihar", "Karnataka", "Maharashtra", "Uttar Pradesh", "Delhi (NCT)").forEach { st ->
                        val isSelected = selectedState.equals(st, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) LegalGoldDark else LegalSurfaceVariant,
                            modifier = Modifier.clickable { selectedState = st }
                        ) {
                            Text(
                                text = st,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) LegalSurfaceWhite else LegalTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LegalGoldPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LegalGoldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { showStateDialog = true }
                    ) {
                        Text(
                            text = if (listOf("All India", "Bihar", "Karnataka", "Maharashtra", "Uttar Pradesh", "Delhi (NCT)").contains(selectedState)) "+ More States" else "• $selectedState",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LegalDeepBlue,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (showStateDialog) {
            AlertDialog(
                onDismissRequest = { showStateDialog = false },
                title = { Text("Select State Jurisdiction", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedState == "All India") LegalGoldContainer else LegalSurfaceWhite,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedState = "All India"
                                        showStateDialog = false
                                    }
                            ) {
                                Text("All India (Auto-detect State from question)", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        items(DialectRegistry.allIndianStates) { st ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedState == st) LegalGoldContainer else LegalSurfaceWhite,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedState = st
                                        showStateDialog = false
                                    }
                            ) {
                                Text(st, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStateDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .border(1.dp, LegalBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LegalGoldDark,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Hello! I am Vani-Kanoon.",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ask any legal question regarding Indian law (Bharatiya Nyaya Sanhita, BNSS, Contracts, Family Law, Cheque Bounce, RERA, Consumer Rights). I will provide statutory citations and practical legal steps.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LegalTextSecondary,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

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
                            .widthIn(max = 330.dp)
                            .border(
                                if (!isUser && activeMessageId == message.id && isSpeaking) 2.dp else 1.dp,
                                if (isUser) LegalDeepBlue else if (activeMessageId == message.id && isSpeaking) Color(0xFFDC2626) else if (activeMessageId == message.id && isPaused) LegalGoldDark else LegalBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .testTag(if (isUser) "user_chat_${message.id}" else "bot_chat_${message.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                        text = "🏛️ ${message.relevantLaw}",
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
                                            // MUTE BUTTON
                                            Button(
                                                onClick = {
                                                    toggleSpeaker(message.id, message.text, localeTag)
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
                                            // RESUME BUTTON
                                            Button(
                                                onClick = {
                                                    toggleSpeaker(message.id, message.text, localeTag)
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
                                            // IDLE SPEAKER BUTTON
                                            IconButton(
                                                onClick = {
                                                    toggleSpeaker(message.id, message.text, localeTag)
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("speak_chat_output_button_${message.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Read Aloud",
                                                    tint = LegalGoldDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Legal Answer", message.text)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied legal answer to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Text",
                                            tint = LegalTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "Indian Legal Guidance - Vani-Kanoon")
                                                putExtra(Intent.EXTRA_TEXT, message.text)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Legal Advice"))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = LegalTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
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
                                    text = "Vani-Kanoon is researching Indian legal codes...",
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

        // Suggestions
        if (chatMessages.size <= 1) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                promptSuggestions.forEach { prompt ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LegalSurfaceWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LegalBorder),
                        modifier = Modifier.clickable { handleSend(prompt) }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LegalDeepBlue,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Input bar
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
                                .testTag("chatbot_global_stop_button")
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

                // Quick Legal Question Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val promptChips = listOf(
                        "Procedure to file Zero FIR?",
                        "Can landlord evict without notice?",
                        "Section 138 Cheque Bounce notice",
                        "Equal property rights for daughters",
                        "Consumer court complaint steps",
                        "Anticipatory bail requirements"
                    )
                    promptChips.forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LegalGoldContainer.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LegalGoldDark.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable {
                                if (!isThinking) {
                                    handleSend(prompt)
                                }
                            }
                        ) {
                            Text(
                                text = prompt,
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask a legal question (e.g. 'What is RERA section 18?')",
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
                        .testTag("chatbot_text_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (isListening) {
                            voiceInputManager.stopListeningAndTranscribe()
                        } else {
                            requestAndStartListening()
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFFEF4444) else if (isProcessing) LegalDeepBlue else LegalGoldContainer)
                        .testTag("chatbot_mic_button")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Voice Input",
                            tint = if (isListening) Color.White else LegalGoldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { handleSend(inputText) },
                    enabled = inputText.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isThinking) LegalDeepBlue else LegalSurfaceVariant)
                        .testTag("chatbot_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isThinking) LegalGoldPrimary else LegalTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
}

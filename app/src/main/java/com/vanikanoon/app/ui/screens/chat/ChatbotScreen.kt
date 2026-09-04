package com.vanikanoon.app.ui.screens.chat

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
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
import android.speech.tts.TextToSpeech
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.vanikanoon.app.data.auth.UserManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var selectedLanguageCode by remember { mutableStateOf("english") }

    val channelId = "CHATBOT_${selectedLanguageCode.uppercase()}"
    val chatMessages by repository.getChatMessages(channelId, userId).collectAsState(initial = emptyList())

    // Speech Recognizer setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    DisposableEffect(Unit) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val sr = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = sr
        }
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    // TextToSpeech Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakMessage(text: String, tag: String) {
        ttsEngine?.let { tts ->
            val locale = java.util.Locale.forLanguageTag(tag)
            tts.language = locale
            val clean = com.vanikanoon.app.util.SpeechSanitizer.sanitizeForSpeech(text)
            tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "CHAT_TTS")
        }
    }

    val localeTag = when (selectedLanguageCode) {
        "kannada" -> "kn-IN"
        "marathi" -> "mr-IN"
        "hindi" -> "hi-IN"
        else -> "en-IN"
    }

    fun startListening() {
        val sr = speechRecognizer ?: run {
            Toast.makeText(context, "Speech recognition initializing or not available on device", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your legal question in ${selectedLanguageCode.replaceFirstChar { it.uppercase() }}...")
        }

        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                    SpeechRecognizer.ERROR_NETWORK -> "Network required for online speech engine, or switch to typing."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                    else -> "Speech recognition error ($error)"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognized = matches[0]
                    inputText = recognized
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputText = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            sr.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            isListening = false
            Toast.makeText(context, "Could not start mic: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
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

    fun handleSend(query: String) {
        if (query.isBlank() || isThinking) return
        val messageText = query.trim()
        inputText = ""

        scope.launch {
            val userMsg = ChatMessage(role = MessageRole.USER, text = messageText, userId = userId)
            repository.saveChatMessage(userMsg, channelId, userId)
            isThinking = true

            val botMsg = repository.askChatbot(
                query = messageText,
                history = chatMessages,
                language = selectedLanguageCode
            )
            repository.saveChatMessage(botMsg.copy(userId = userId), channelId, userId)
            isThinking = false

            listState.animateScrollToItem((chatMessages.size + 1).coerceAtLeast(0))
        }
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
            }
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
                                1.dp,
                                if (isUser) LegalDeepBlue else LegalBorder,
                                RoundedCornerShape(16.dp)
                            )
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            speakMessage(message.text, localeTag)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Read Aloud",
                                            tint = LegalGoldDark,
                                            modifier = Modifier.size(17.dp)
                                        )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
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
                            speechRecognizer?.stopListening()
                            isListening = false
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFFEF4444) else LegalGoldContainer)
                        .testTag("chatbot_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Voice Input",
                        tint = if (isListening) Color.White else LegalGoldDark,
                        modifier = Modifier.size(22.dp)
                    )
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

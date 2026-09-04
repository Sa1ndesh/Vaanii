package com.vanikanoon.app.ui.screens.vani

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentUser by userManager?.currentUser?.collectAsState() ?: remember { mutableStateOf(null) }
    val userId = currentUser?.email?.ifBlank { "guest" } ?: "guest"

    var currentStep by remember { mutableStateOf(VaniStep.LANGUAGE_SELECTION) }
    var selectedLanguage by remember { mutableStateOf(LanguageCatalog.languages[0]) }
    var selectedState by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("") }

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    val channelId = "VANI_VOICE_${selectedLanguage.code.uppercase()}"
    val chatMessages by repository.getChatMessages(channelId, userId).collectAsState(initial = emptyList())

    // Android TTS Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize default
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(text: String, langTag: String) {
        ttsEngine?.let { tts ->
            val locale = Locale.forLanguageTag(langTag)
            tts.language = locale
            val cleanSpeech = com.vanikanoon.app.util.SpeechSanitizer.sanitizeForSpeech(text)
            tts.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "VANI_TTS")
        }
    }

    // Android Speech Recognizer
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

    fun startSpeechRecognition() {
        val sr = speechRecognizer ?: run {
            Toast.makeText(context, "Speech Recognition initializing or not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.ttsLocaleTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguage.ttsLocaleTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your legal question in ${selectedLanguage.nameEn}...")
        }
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try speaking clearly."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error with speech recognizer. You can also type below."
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
            startSpeechRecognition()
        } else {
            Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSend(queryText: String) {
        if (queryText.isBlank() || isThinking) return
        val textToSend = queryText.trim()
        inputText = ""

        scope.launch {
            val userMsg = ChatMessage(
                role = MessageRole.USER,
                text = textToSend,
                userId = userId
            )
            repository.saveChatMessage(userMsg, channelId, userId)
            isThinking = true

            val botResponse = repository.askVaniVoice(
                language = selectedLanguage.code,
                state = selectedState,
                district = selectedDistrict,
                query = textToSend
            )
            repository.saveChatMessage(botResponse.copy(userId = userId), channelId, userId)
            isThinking = false

            // Auto-speak response if available
            speakText(botResponse.text, selectedLanguage.ttsLocaleTag)

            // Scroll down
            listState.animateScrollToItem((chatMessages.size + 1).coerceAtLeast(0))
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
                        Spacer(modifier = Modifier.height(20.dp))
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
                        Spacer(modifier = Modifier.height(24.dp))
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
                                selectedLanguage = lang
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
                        IconButton(onClick = { currentStep = VaniStep.LANGUAGE_SELECTION }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
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
                        IconButton(onClick = {
                            currentStep = VaniStep.STATE_SELECTION
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
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
                                    // Add initial greeting message in strictly selected language if empty
                                    scope.launch {
                                        if (chatMessages.isEmpty()) {
                                            val welcome = ChatMessage(
                                                role = MessageRole.ASSISTANT,
                                                text = "${info.greetingPhrase}!\n\n" + selectedLanguage.welcomeText,
                                                dialect = "${info.dialectName} • $dist ($selectedState)",
                                                userId = userId
                                            )
                                            repository.saveChatMessage(welcome, channelId, userId)
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
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Language Chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LegalGoldPrimary,
                                    modifier = Modifier.clickable { currentStep = VaniStep.LANGUAGE_SELECTION }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = selectedLanguage.nameNative,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = LegalDeepBlue
                                            )
                                        )
                                    }
                                }

                                // State Chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LegalBlueLight,
                                    modifier = Modifier.clickable { currentStep = VaniStep.STATE_SELECTION }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = selectedState,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = LegalSurfaceWhite,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }

                                // District Chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LegalBlueLight,
                                    modifier = Modifier.clickable { currentStep = VaniStep.DISTRICT_SELECTION }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = selectedDistrict,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = LegalGoldPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (chatMessages.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                repository.clearChatMessages(channelId, userId)
                                                // Re-add initial greeting
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

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LegalGoldPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable {
                                        selectedState = ""
                                        selectedDistrict = ""
                                        currentStep = VaniStep.LANGUAGE_SELECTION
                                    }
                                ) {
                                    Text(
                                        text = "Change Region",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LegalGoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                                        1.dp,
                                        if (isUser) LegalDeepBlue else LegalBorder,
                                        RoundedCornerShape(16.dp)
                                    )
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
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    speakText(message.text, selectedLanguage.ttsLocaleTag)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Speak Response",
                                                    tint = LegalBlueHighlight,
                                                    modifier = Modifier.size(18.dp)
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

                // 3. Quick prompts
                if (chatMessages.size <= 2) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedLanguage.examplePrompts.forEach { prompt ->
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

                // 4. Voice & Text Input Bar
                Surface(
                    color = LegalSurfaceWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VoiceMicVisualizer(
                                isListening = isListening,
                                onClick = {
                                    if (isListening) {
                                        speechRecognizer?.stopListening()
                                        isListening = false
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
}

package com.vanikanoon.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import com.vanikanoon.app.util.OfflineSpeechHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.vanikanoon.app.ui.theme.LegalBgLight
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.theme.LegalBorder
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextMuted
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary

@Composable
fun SettingsScreen(
    userManager: UserManager,
    onNavigateToProfile: () -> Unit,
    onNavigateToCustomerSupport: () -> Unit,
    onNavigateToReportIssue: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToOfflineStatus: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { LegalRepository(context) }
    val user by userManager.currentUser.collectAsState()
    var backendUrl by remember { mutableStateOf(userManager.getBackendUrl()) }
    var geminiApiKey by remember { mutableStateOf(userManager.getGeminiApiKey()) }
    var isTestingAi by remember { mutableStateOf(false) }
    var aiTestResult by remember { mutableStateOf("") }

    // Local Text-to-Speech Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsStatusMessage by remember { mutableStateOf("Local STT & TTS ready on device") }
    var testSpeechResult by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsStatusMessage = "Local TTS synthesizer active"
            }
        }
        ttsEngine = tts
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {}
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LegalDeepBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(LegalGoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Balance,
                        contentDescription = null,
                        tint = LegalDeepBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Vani-Kanoon Legal Assistant",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalSurfaceWhite
                    )
                )
                Text(
                    text = "FastAPI + React + Android • Indian Kanoon AI",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LegalGoldPrimary.copy(alpha = 0.9f)
                    )
                )
            }
        }

        // Account / Profile Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
                .clickable { onNavigateToProfile() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(LegalGoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = LegalGoldDark, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.fullName ?: "Sandesh Birannavar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalDeepBlue
                        )
                    )
                    Text(
                        text = user?.email ?: "sandeshbirannavar@gmail.com",
                        style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = LegalTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // AI Legal Intelligence & Gemini Engine Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = LegalDeepBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Legal Intelligence Engine",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "Dual Engine Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Vani-Kanoon uses Google Gemini 3.5 Flash along with a comprehensive On-Device Neural Law Engine. It delivers accurate legal advice for all Indian criminal, civil, family, property, and state statutes with zero required downloads.",
                    style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                )

                OutlinedTextField(
                    value = geminiApiKey,
                    onValueChange = { geminiApiKey = it },
                    label = { Text("Gemini API Key (Optional Override)") },
                    placeholder = { Text("Leave blank to use built-in engine") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            userManager.setGeminiApiKey(geminiApiKey.trim())
                            Toast.makeText(context, if (geminiApiKey.isNotBlank()) "Gemini API Key saved!" else "Using built-in engine!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalDeepBlue,
                            contentColor = LegalGoldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save AI Key")
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isTestingAi) {
                                isTestingAi = true
                                scope.launch {
                                    try {
                                        val answer = repository.sendQuery("What is Section 138 NI Act for cheque bounce?")
                                        aiTestResult = if (answer.length > 200) answer.take(200) + "..." else answer
                                        Toast.makeText(context, "AI Response Received!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        aiTestResult = "Test failed: ${e.message}"
                                    } finally {
                                        isTestingAi = false
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isTestingAi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Test AI")
                        }
                    }
                }

                if (aiTestResult.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LegalBgLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Sample AI Engine Response:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = LegalDeepBlue)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = aiTestResult,
                                style = MaterialTheme.typography.bodySmall.copy(color = LegalTextPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Backend URL & API Configuration Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = LegalDeepBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Backend Service Endpoint",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalDeepBlue
                        )
                    )
                }

                Text(
                    text = "Configure your local or remote FastAPI server URL. (Emulator default: http://10.0.2.2:8000)",
                    style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                )

                OutlinedTextField(
                    value = backendUrl,
                    onValueChange = { backendUrl = it },
                    label = { Text("FastAPI Server URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            userManager.setBackendUrl(backendUrl.trim())
                            Toast.makeText(context, "Backend endpoint saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalDeepBlue,
                            contentColor = LegalGoldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Endpoint")
                    }

                    Button(
                        onClick = {
                            backendUrl = "http://10.0.2.2:8000"
                            userManager.setBackendUrl("http://10.0.2.2:8000")
                            Toast.makeText(context, "Reset to Emulator Default", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray.copy(alpha = 0.4f),
                            contentColor = LegalTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Default")
                    }
                }
            }
        }

        // Local STT & TTS Voice Engine Card (Zero Downloads Required)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
                .testTag("local_speech_engine_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = LegalDeepBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local Speech (STT) & Audio (TTS)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        Text(
                            text = "Built-in on device • 100% Zero Downloads Required",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LegalGoldDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        contentColor = Color(0xFF047857)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ready", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Text(
                    text = "Vani-Kanoon utilizes your device's native Android Speech Recognizer and Text-to-Speech synthesizer. It listens and speaks in Indian regional languages directly on your phone without requiring manual language pack downloads.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LegalTextSecondary,
                        lineHeight = 18.sp
                    )
                )

                // Feature check items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LegalGoldContainer.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = LegalGoldDark,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Local Speech-to-Text (STT)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalDeepBlue
                                )
                            )
                            Text(
                                text = "Instant on-device speech transcription. Taps into your Android keyboard & system voice engine with zero downloads.",
                                style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = LegalGoldDark,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Local Text-to-Speech (TTS)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalDeepBlue
                                )
                            )
                            Text(
                                text = "Instant spoken legal explanations. Cascades intelligently across regional languages (Hindi, Kannada, Tamil, Marathi, Bengali, Telugu, etc.) without demanding extra voice packs.",
                                style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary, fontSize = 11.sp)
                            )
                        }
                    }
                }

                if (testSpeechResult.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LegalDeepBlue.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Last Voice Input: \"$testSpeechResult\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LegalDeepBlue,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Interactive Test Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            OfflineSpeechHelper.testLocalSpeech(
                                context = context,
                                tts = ttsEngine,
                                sampleText = "नमस्ते! वाणी-कानून लोकल स्पीच और ऑडियो आपके फोन पर बिना किसी डाउनलोड के पूरी तरह तैयार है।",
                                localeTag = "hi-IN",
                                onStatus = { msg ->
                                    ttsStatusMessage = msg
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalDeepBlue,
                            contentColor = LegalGoldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Local TTS", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = onNavigateToOfflineStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalGoldContainer,
                            contentColor = LegalDeepBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Local STT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                // Device voice settings button
                Button(
                    onClick = { OfflineSpeechHelper.openVoiceSettings(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.35f),
                        contentColor = LegalTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Android System Voice Settings (Optional)", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onNavigateToOfflineStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF047857),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_offline_diagnostics_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline System Health & Airplane Mode Test", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Support & Policy Navigation List
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsNavRow(
                    icon = Icons.Default.HeadsetMic,
                    title = "Customer Support Policy",
                    subtitle = "Contact sandeshbirannavar@gmail.com",
                    onClick = onNavigateToCustomerSupport
                )

                SettingsNavRow(
                    icon = Icons.Default.BugReport,
                    title = "Report an Issue",
                    subtitle = "Submit bug report or feature request",
                    onClick = onNavigateToReportIssue
                )

                SettingsNavRow(
                    icon = Icons.Default.Gavel,
                    title = "Terms & Conditions",
                    subtitle = "Statutory legal usage guidelines",
                    onClick = onNavigateToTerms
                )

                SettingsNavRow(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    subtitle = "Data processing & DPDP Act compliance",
                    onClick = onNavigateToPrivacyPolicy
                )

                SettingsNavRow(
                    icon = Icons.Default.Info,
                    title = "About Vani-Kanoon",
                    subtitle = "AI Legal Tech Mission & Platform Details",
                    onClick = onNavigateToAbout
                )
            }
        }

        // System & Engine Info
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Capabilities",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalDeepBlue
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingItemRow(
                    icon = Icons.Default.Language,
                    title = "Regional Dialects Supported",
                    description = "Kannada (ಕನ್ನಡ), Marathi (मराठी), Hindi (हिंदी), English"
                )

                SettingItemRow(
                    icon = Icons.Default.Key,
                    title = "AI Intelligence Architecture",
                    description = "FastAPI Backend + Gemini 2.5 Flash + Statutory Offline RAG"
                )

                SettingItemRow(
                    icon = Icons.Default.Security,
                    title = "On-Device Persistence",
                    description = "Encrypted SQLite Room Database for drafted contracts, notices, and chat logs."
                )
            }
        }

        // Statutory Legal Disclaimer
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalGoldContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = LegalGoldDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Statutory Legal Disclaimer",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalGoldDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vani-Kanoon provides legal awareness, document drafting templates, and case law summarization under Indian Law (BNS, BNSS, CPA 2019, CPC). It does not constitute an advocate-client relationship. For formal court appearances, consult a registered advocate.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LegalTextPrimary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LegalDeepBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = LegalDeepBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = LegalTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = LegalTextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun SettingItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LegalDeepBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = LegalDeepBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LegalTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LegalTextSecondary,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

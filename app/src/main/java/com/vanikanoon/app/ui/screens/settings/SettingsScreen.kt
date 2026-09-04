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
import com.vanikanoon.app.ui.theme.LegalBorder
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
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
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val user by userManager.currentUser.collectAsState()
    var backendUrl by remember { mutableStateOf(userManager.getBackendUrl()) }

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

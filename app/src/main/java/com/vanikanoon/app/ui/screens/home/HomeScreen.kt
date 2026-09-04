package com.vanikanoon.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.data.auth.UserManager
import com.vanikanoon.app.ui.components.FeatureLauncherCard
import com.vanikanoon.app.ui.components.LegalDisclaimerBanner
import com.vanikanoon.app.ui.theme.LegalBgLight
import com.vanikanoon.app.ui.theme.LegalBlueDark
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSuccess
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary

@Composable
fun HomeScreen(
    userManager: UserManager,
    onNavigateToVaniVoice: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToAdvocatePortal: () -> Unit,
    onNavigateToStudentPortal: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onNavigateToAnalyzer: () -> Unit,
    onNavigateToSummarizer: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCustomerSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTerms: () -> Unit
) {
    val currentUser by userManager.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Status / Quick Switch Bar
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (currentUser?.isGuest == false) onNavigateToProfile() else onNavigateToLogin()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    currentUser?.isAdvocate == true -> LegalGoldPrimary
                                    currentUser?.isStudent == true -> Color(0xFF64B5F6)
                                    else -> Color(0xFFE0E0E0)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                currentUser?.isAdvocate == true -> Icons.Default.Balance
                                currentUser?.isStudent == true -> Icons.Default.School
                                else -> Icons.Default.AccountCircle
                            },
                            contentDescription = null,
                            tint = LegalDeepBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.fullName ?: "Citizen User",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        Text(
                            text = when {
                                currentUser?.isAdvocate == true -> "Verified Bar Advocate • ${currentUser?.barEnrollmentNumber ?: "KAR/3842/2019"}"
                                currentUser?.isStudent == true -> "Law Scholar • ${currentUser?.collegeName ?: "NLSIU Bengaluru"}"
                                else -> "Public Citizen Mode"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary),
                            maxLines = 1
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LegalDeepBlue
                    ) {
                        Text(
                            text = if (currentUser?.isGuest == false) "Profile" else "Login",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LegalGoldPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Hero Banner - Free Voice & Legal Chat (Citizen Access)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LegalDeepBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(LegalDeepBlue, LegalBlueDark, Color(0xFF0F395A))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = LegalGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ಕನ್ನಡ • मराठी • हिंदी • Eng",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = LegalSurfaceWhite.copy(alpha = 0.85f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Vani-Kanoon Voice & Legal Chat",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = LegalSurfaceWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Ask any legal question in your regional language. Get instant statute explanations, citizen rights, and procedure guidance.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LegalSurfaceWhite.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onNavigateToVaniVoice,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LegalGoldPrimary,
                                    contentColor = LegalDeepBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("hero_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Voice Assistant",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onNavigateToChatbot,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("hero_chat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Chat AI")
                            }
                        }
                    }
                }
            }
        }

        // Quick Voice Banner Card
        item {
            Card(
                onClick = onNavigateToVaniVoice,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LegalGoldContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(LegalGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = LegalDeepBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ವಾಣಿ-ಕಾನೂನ್ • वाणी-कानून",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        Text(
                            text = "Open Voice Assistant • Indian Regional Dialects",
                            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextPrimary)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = LegalDeepBlue
                    )
                }
            }
        }

        // Section Title: Professional Portals
        item {
            Text(
                text = "Professional Portals (Login Required)",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LegalTextPrimary
                ),
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        // Advocate Portal Gateway Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp, 
                    if (currentUser?.isAdvocate == true) LegalSuccess else LegalGoldPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAdvocatePortal() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LegalDeepBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Balance,
                                    contentDescription = null,
                                    tint = LegalGoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Advocate Legal Tech Portal",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )
                                Text(
                                    text = if (currentUser?.isAdvocate == true) "Active: Verified Bar Access" else "Access using Bar Council Enrollment ID",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (currentUser?.isAdvocate == true) LegalSuccess else LegalTextSecondary
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = if (currentUser?.isAdvocate == true) Icons.Default.VerifiedUser else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (currentUser?.isAdvocate == true) LegalSuccess else LegalGoldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Includes: Legal Document Generator (NDAs, Rent, Sale Deeds), Statutory Notice Generator (Sec 138 NI Act / Sec 33C), FIR Scrutinizer & Defense Strategy, Case Precedent Summarizer, and Client FAQ Builder.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LegalTextPrimary,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToAdvocatePortal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalDeepBlue,
                            contentColor = LegalGoldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (currentUser?.isAdvocate == true) "Open Advocate Suite →" else "Advocate Login / Verification Details →",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Student Hub Gateway Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp, 
                    if (currentUser?.isStudent == true) LegalSuccess else Color(0xFF64B5F6)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStudentPortal() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A5F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF90CAF9),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Law Student Academic Hub",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )
                                Text(
                                    text = if (currentUser?.isStudent == true) "Active: Verified Student Access" else "Access using Law College / Student ID",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (currentUser?.isStudent == true) LegalSuccess else LegalTextSecondary
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = if (currentUser?.isStudent == true) Icons.Default.VerifiedUser else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (currentUser?.isStudent == true) LegalSuccess else Color(0xFF1E3A5F),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Includes: 10-Mark Law Exam Answer Grader & Feedback, Ratio Decidendi & Judgment Briefs, Bare Act Simplifier, and Moot Court Research Aid.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LegalTextPrimary,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToStudentPortal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A5F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (currentUser?.isStudent == true) "Open Student Hub →" else "Law Student Login / College ID →",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section Title: Open Statutes & Public Law
        item {
            Text(
                text = "Indian Statutes & Public Bare Acts",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LegalTextPrimary
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            FeatureLauncherCard(
                icon = Icons.Default.Assignment,
                title = "Indian Legal Knowledge Base",
                description = "Browse searchable Indian statutes (BNS 2023, BNSS, Consumer Protection, Tenancy, Labour, Cyber Law).",
                tag = "Free Bare Acts",
                onClick = onNavigateToKnowledgeBase
            )
        }

        // Quick Footer Links
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToCustomerSupport() }
                    ) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = LegalDeepBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Support", style = MaterialTheme.typography.labelSmall.copy(color = LegalDeepBlue))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToAbout() }
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = LegalDeepBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("About", style = MaterialTheme.typography.labelSmall.copy(color = LegalDeepBlue))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToTerms() }
                    ) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = LegalDeepBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Terms", style = MaterialTheme.typography.labelSmall.copy(color = LegalDeepBlue))
                    }
                }
            }
        }

        // Disclaimer Banner
        item {
            LegalDisclaimerBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


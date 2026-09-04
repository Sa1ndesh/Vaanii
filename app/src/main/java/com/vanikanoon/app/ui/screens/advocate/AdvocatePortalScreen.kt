package com.vanikanoon.app.ui.screens.advocate

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.data.auth.UserManager
import com.vanikanoon.app.ui.components.FeatureLauncherCard
import com.vanikanoon.app.ui.theme.LegalBgLight
import com.vanikanoon.app.ui.theme.LegalBlueDark
import com.vanikanoon.app.ui.theme.LegalBorder
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSuccess
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary

@Composable
fun AdvocatePortalScreen(
    userManager: UserManager,
    onNavigateToDocuments: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onNavigateToAnalyzer: () -> Unit,
    onNavigateToSummarizer: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by userManager.currentUser.collectAsState()

    if (currentUser?.isAdvocate == true) {
        // Logged-in Advocate Portal Dashboard
        AdvocateAuthenticatedDashboard(
            user = currentUser!!,
            onLogout = { userManager.logout() },
            onNavigateToDocuments = onNavigateToDocuments,
            onNavigateToNotices = onNavigateToNotices,
            onNavigateToAnalyzer = onNavigateToAnalyzer,
            onNavigateToSummarizer = onNavigateToSummarizer,
            onNavigateToFaq = onNavigateToFaq,
            onNavigateToKnowledgeBase = onNavigateToKnowledgeBase
        )
    } else {
        // Advocate Login & Verification Screen
        AdvocateLoginView(
            userManager = userManager,
            onLoginSuccess = {
                Toast.makeText(context, "Advocate credentials verified successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AdvocateAuthenticatedDashboard(
    user: com.vanikanoon.app.data.auth.User,
    onLogout: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onNavigateToAnalyzer: () -> Unit,
    onNavigateToSummarizer: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("advocate_portal_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Advocate Bar Profile Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LegalDeepBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(LegalDeepBlue, LegalBlueDark, Color(0xFF0D3250))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LegalGoldPrimary.copy(alpha = 0.25f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = LegalGoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "VERIFIED ADVOCATE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LegalGoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Switch Account",
                                    tint = LegalSurfaceWhite.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = LegalSurfaceWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "Bar Council Enrollment: ${user.barEnrollmentNumber ?: "KAR/3842/2019"}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LegalGoldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Text(
                            text = "${user.stateBarCouncil ?: "Bar Council of Karnataka"} • Chamber: ${user.city}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LegalSurfaceWhite.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Advocate Professional Toolkit",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LegalTextPrimary
                )
            )
        }

        // Feature 1: Legal Document Generator
        item {
            FeatureLauncherCard(
                icon = Icons.Default.Description,
                title = "Legal Document Generator",
                description = "Draft customized print-ready NDAs, General Affidavits, Rent Agreements, Sale Deeds, and Lease Deeds.",
                tag = "5 Formats",
                onClick = onNavigateToDocuments
            )
        }

        // Feature 2: Statutory Notice Generator
        item {
            FeatureLauncherCard(
                icon = Icons.Default.Article,
                title = "Statutory Notice Generator",
                description = "Produce formal legal demand notices under Section 138 NI Act (Cheque Bounce) & Section 33C ID Act (Unpaid Salary).",
                tag = "Demand Notices",
                onClick = onNavigateToNotices
            )
        }

        // Feature 3: FIR & Evidence Analyzer
        item {
            FeatureLauncherCard(
                icon = Icons.Default.FactCheck,
                title = "FIR & Evidence Scrutinizer",
                description = "Scrutinize police complaints, extract BNS/IPC sections, assess bailability status, cognizable offences, and defense roadmaps.",
                tag = "Criminal Law",
                onClick = onNavigateToAnalyzer
            )
        }

        // Feature 4: Case Law Summarizer
        item {
            FeatureLauncherCard(
                icon = Icons.Default.Gavel,
                title = "Case Law & Precedent Summarizer",
                description = "Summarize lengthy Supreme Court and High Court judgments with extracted Ratio Decidendi and legal issues.",
                tag = "Precedents",
                onClick = onNavigateToSummarizer
            )
        }

        // Feature 5: Client FAQ Builder
        item {
            FeatureLauncherCard(
                icon = Icons.Default.QuestionAnswer,
                title = "Client Consultation FAQ Builder",
                description = "Generate structured legal FAQs for client consultations across criminal, civil, tenancy, and corporate matters.",
                tag = "Consultations",
                onClick = onNavigateToFaq
            )
        }

        // Feature 6: Bare Acts & Statutory Codes
        item {
            FeatureLauncherCard(
                icon = Icons.Default.Assignment,
                title = "Indian Bare Acts & Searchable Codes",
                description = "Search Bharatiya Nyaya Sanhita (BNS 2023), BNSS, BSA, Consumer Protection, and Labour Acts with instant citations.",
                tag = "Statutes",
                onClick = onNavigateToKnowledgeBase
            )
        }
    }
}

@Composable
private fun AdvocateLoginView(
    userManager: UserManager,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var barEnrollment by remember { mutableStateOf("KAR/3842/2019") }
    var stateCouncil by remember { mutableStateOf("Bar Council of Karnataka") }
    var advocateName by remember { mutableStateOf("Adv. Sandesh Birannavar") }
    var email by remember { mutableStateOf("sandesh.advocate@vanikanoon.in") }
    var password by remember { mutableStateOf("barPass@2024") }
    var passwordVisible by remember { mutableStateOf(false) }
    var city by remember { mutableStateOf("Bengaluru") }
    var isLoading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .padding(16.dp)
            .testTag("advocate_login_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LegalDeepBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LegalGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = null,
                                tint = LegalDeepBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Advocate Bar Portal",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalSurfaceWhite
                                )
                            )
                            Text(
                                text = "Restricted Legal Practitioner Access",
                                style = MaterialTheme.typography.bodySmall.copy(color = LegalGoldPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Access automated legal drafting (Agreements, Notices, Affidavits), police FIR scrutinizer, case precedent summarizer, and consultation builders.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LegalSurfaceWhite.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Quick Demo Fill Button
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LegalGoldContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚡ Instant Verified Demo Access",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        Text(
                            text = "Login as Adv. Sandesh (Bar Reg: KAR/3842/2019)",
                            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                        )
                    }
                    Button(
                        onClick = {
                            userManager.loginAsAdvocate(
                                name = "Adv. Sandesh Birannavar",
                                email = "sandesh.advocate@vanikanoon.in",
                                barEnrollmentNumber = "KAR/3842/2019",
                                stateBarCouncil = "Bar Council of Karnataka",
                                city = "Bengaluru"
                            )
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LegalDeepBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("1-Tap Login", color = LegalGoldPrimary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Login Form Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, LegalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bar Council Verification & Login",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalDeepBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Bar Enrollment Number
                    OutlinedTextField(
                        value = barEnrollment,
                        onValueChange = { barEnrollment = it },
                        label = { Text("Bar Enrollment Number *") },
                        placeholder = { Text("e.g., KAR/3842/2019") },
                        leadingIcon = {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = LegalDeepBlue)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // State Bar Council
                    OutlinedTextField(
                        value = stateCouncil,
                        onValueChange = { stateCouncil = it },
                        label = { Text("State Bar Council *") },
                        placeholder = { Text("e.g., Bar Council of Karnataka") },
                        leadingIcon = {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = LegalDeepBlue)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Advocate Full Name
                    OutlinedTextField(
                        value = advocateName,
                        onValueChange = { advocateName = it },
                        label = { Text("Advocate / Chamber Name *") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = LegalDeepBlue)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Chamber Email Address *") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = LegalDeepBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password *") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = LegalDeepBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = LegalTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (barEnrollment.isBlank() || advocateName.isBlank() || email.isBlank()) {
                                Toast.makeText(context, "Please fill all required Bar details", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            userManager.loginAsAdvocate(
                                name = advocateName,
                                email = email,
                                barEnrollmentNumber = barEnrollment,
                                stateBarCouncil = stateCouncil,
                                city = city
                            )
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalDeepBlue,
                            contentColor = LegalGoldPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LegalGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verify & Access Advocate Suite",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

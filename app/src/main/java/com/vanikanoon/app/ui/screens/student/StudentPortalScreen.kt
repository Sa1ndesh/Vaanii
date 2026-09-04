package com.vanikanoon.app.ui.screens.student

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary

@Composable
fun StudentPortalScreen(
    userManager: UserManager,
    onNavigateToLearning: () -> Unit,
    onNavigateToSummarizer: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit,
    onNavigateToFaq: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by userManager.currentUser.collectAsState()

    if (currentUser?.isStudent == true) {
        // Logged-in Student Academic Dashboard
        StudentAuthenticatedDashboard(
            user = currentUser!!,
            onLogout = { userManager.logout() },
            onNavigateToLearning = onNavigateToLearning,
            onNavigateToSummarizer = onNavigateToSummarizer,
            onNavigateToKnowledgeBase = onNavigateToKnowledgeBase,
            onNavigateToFaq = onNavigateToFaq
        )
    } else {
        // Student Login & Academic Verification Screen
        StudentLoginView(
            userManager = userManager,
            onLoginSuccess = {
                Toast.makeText(context, "Student credentials verified successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun StudentAuthenticatedDashboard(
    user: com.vanikanoon.app.data.auth.User,
    onLogout: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToSummarizer: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit,
    onNavigateToFaq: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("student_portal_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student ID Card
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
                                listOf(LegalDeepBlue, Color(0xFF1E3A5F), Color(0xFF0F2E4A))
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
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = LegalGoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LAW SCHOLAR / STUDENT",
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
                            text = user.collegeName ?: "National Law School of India University (NLSIU)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LegalGoldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Text(
                            text = "Student ID: ${user.studentId ?: "NLSIU-BALLB-2023-88"} • ${user.semesterOrYear ?: "3rd Year (Semester 5)"}",
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
                text = "Law Student Academic Suite",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LegalTextPrimary
                )
            )
        }

        // Feature 1: Exam Evaluator & Learning Hub
        item {
            FeatureLauncherCard(
                icon = Icons.Default.MenuBook,
                title = "10-Mark Law Exam Answer Evaluator",
                description = "Submit your answers to legal problem questions and receive structured grading out of 10 marks with model answers and citations.",
                tag = "Exam Grader",
                onClick = onNavigateToLearning
            )
        }

        // Feature 2: Case Law Summarizer (Ratio Decidendi)
        item {
            FeatureLauncherCard(
                icon = Icons.Default.Gavel,
                title = "Judgment Brief & Ratio Decidendi Extractor",
                description = "Transform 80+ page Supreme Court & High Court judgments into concise student case briefs with facts, issues, and obiter dicta.",
                tag = "Moot & Briefs",
                onClick = onNavigateToSummarizer
            )
        }

        // Feature 3: Searchable Bare Acts
        item {
            FeatureLauncherCard(
                icon = Icons.Default.Assignment,
                title = "Indian Bare Acts & New Criminal Codes",
                description = "Compare old IPC/CrPC sections with new Bharatiya Nyaya Sanhita (BNS 2023) and BNSS with instant concept search.",
                tag = "Bare Acts",
                onClick = onNavigateToKnowledgeBase
            )
        }

        // Feature 4: Client & Citizen FAQ Builder
        item {
            FeatureLauncherCard(
                icon = Icons.Default.QuestionAnswer,
                title = "Legal Clinic & Moot FAQ Builder",
                description = "Generate structured legal questions and answers for clinical legal education and moot court problem research.",
                tag = "Research Aid",
                onClick = onNavigateToFaq
            )
        }
    }
}

@Composable
private fun StudentLoginView(
    userManager: UserManager,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var collegeName by remember { mutableStateOf("National Law School of India University (NLSIU)") }
    var studentId by remember { mutableStateOf("NLSIU-BALLB-2023-88") }
    var semester by remember { mutableStateOf("3rd Year (5th Semester) - BA.LLB") }
    var studentName by remember { mutableStateOf("Priya Sharma") }
    var email by remember { mutableStateOf("priya.law@nlsiu.ac.in") }
    var password by remember { mutableStateOf("studentPass@2024") }
    var passwordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .padding(16.dp)
            .testTag("student_login_screen"),
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
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = LegalDeepBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Law Student Academic Hub",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalSurfaceWhite
                                )
                            )
                            Text(
                                text = "LL.B / LL.M & Academic Scholar Access",
                                style = MaterialTheme.typography.bodySmall.copy(color = LegalGoldPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Access automated 10-Mark Law Exam answer evaluation, case judgment briefs, ratio decidendi extractors, Bare Act simplifiers, and moot court research tools.",
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
                            text = "⚡ Instant Student Demo Access",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        Text(
                            text = "Login as Priya Sharma (NLSIU Bengaluru)",
                            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                        )
                    }
                    Button(
                        onClick = {
                            userManager.loginAsStudent(
                                name = "Priya Sharma",
                                email = "priya.law@nlsiu.ac.in",
                                collegeName = "National Law School of India University (NLSIU)",
                                studentId = "NLSIU-BALLB-2023-88",
                                semesterOrYear = "3rd Year (5th Semester) - BA.LLB",
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

        // Student Form Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, LegalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Law College & Academic Login",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalDeepBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // College Name
                    OutlinedTextField(
                        value = collegeName,
                        onValueChange = { collegeName = it },
                        label = { Text("Law College / University *") },
                        placeholder = { Text("e.g., NLSIU Bengaluru / DU Faculty of Law") },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null, tint = LegalDeepBlue)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Student Roll / ID
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Student Enrollment / Roll No. *") },
                        placeholder = { Text("e.g., NLSIU-BALLB-2023-88") },
                        leadingIcon = {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = LegalDeepBlue)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Year / Semester
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Course & Semester / Year *") },
                        placeholder = { Text("e.g., 3rd Year (Semester 5) - BA.LLB") },
                        leadingIcon = {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = LegalDeepBlue)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Full Name
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student Full Name *") },
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
                        label = { Text("College Email Address *") },
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
                            if (collegeName.isBlank() || studentId.isBlank() || studentName.isBlank() || email.isBlank()) {
                                Toast.makeText(context, "Please fill all required academic details", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            userManager.loginAsStudent(
                                name = studentName,
                                email = email,
                                collegeName = collegeName,
                                studentId = studentId,
                                semesterOrYear = semester
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
                            text = "Verify & Access Student Hub",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

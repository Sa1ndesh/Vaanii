package com.vanikanoon.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
fun LoginScreen(
    userManager: UserManager,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("sandeshbirannavar@gmail.com") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Brand Logo & Heading
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(LegalDeepBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Balance,
                contentDescription = null,
                tint = LegalGoldPrimary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Vani-Kanoon",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LegalDeepBlue
            )
        )

        Text(
            text = "AI Legal Assistant • Login to Your Account",
            style = MaterialTheme.typography.bodyMedium.copy(color = LegalTextSecondary)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Login Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC62828)),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email Address *") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = LegalDeepBlue)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input")
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password *") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = LegalDeepBlue)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = LegalTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                // Forgot Password Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onNavigateToForgotPassword) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = LegalDeepBlue
                            )
                        )
                    }
                }

                // Sign In Button
                Button(
                    onClick = {
                        if (email.isBlank() || !email.contains("@")) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }
                        isLoading = true
                        userManager.login(
                            email = email.trim(),
                            name = if (email.contains("sandesh")) "Sandesh Birannavar" else email.substringBefore("@").replace(".", " ").capitalize(),
                            role = "Advocate & Legal Researcher"
                        )
                        Toast.makeText(context, "Welcome back to Vani-Kanoon!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalDeepBlue,
                        contentColor = LegalGoldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = LegalGoldPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalGoldPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Role Login Cards
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalGoldContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚡ Quick One-Tap Role Sign-In",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalDeepBlue
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            userManager.loginAsAdvocate(
                                name = "Adv. Sandesh Birannavar",
                                email = "sandesh.advocate@vanikanoon.in",
                                barEnrollmentNumber = "KAR/3842/2019",
                                stateBarCouncil = "Bar Council of Karnataka",
                                city = "Bengaluru"
                            )
                            Toast.makeText(context, "Logged in as Verified Advocate", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LegalDeepBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Advocate Login",
                            color = LegalGoldPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            userManager.loginAsStudent(
                                name = "Priya Sharma",
                                email = "priya.law@nlsiu.ac.in",
                                collegeName = "National Law School of India University (NLSIU)",
                                studentId = "NLSIU-BALLB-2023-88",
                                semesterOrYear = "3rd Year (Semester 5) - BA.LLB",
                                city = "Bengaluru"
                            )
                            Toast.makeText(context, "Logged in as Law Student", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Student Login",
                            color = Color(0xFF90CAF9),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Guest / Quick Access Button
        OutlinedButton(
            onClick = {
                userManager.loginAsGuest()
                Toast.makeText(context, "Continuing as Public Citizen (Free Voice & Chat)", Toast.LENGTH_SHORT).show()
                onContinueAsGuest()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_guest_btn")
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = LegalDeepBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continue as Citizen (Free Voice & Chat)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = LegalDeepBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Don't have an account? Sign Up
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium.copy(color = LegalTextSecondary)
            )
            TextButton(onClick = onNavigateToSignUp) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalDeepBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

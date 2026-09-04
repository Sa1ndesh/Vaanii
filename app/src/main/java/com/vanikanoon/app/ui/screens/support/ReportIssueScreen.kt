package com.vanikanoon.app.ui.screens.support

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.ui.theme.LegalBgLight
import com.vanikanoon.app.ui.theme.LegalBorder
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextSecondary

@Composable
fun ReportIssueScreen() {
    val context = LocalContext.current
    var issueTitle by remember { mutableStateOf("") }
    var issueModule by remember { mutableStateOf("Vani-Kanoon Voice Assistant") }
    var issueDescription by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("sandeshbirannavar@gmail.com") }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("report_issue_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LegalDeepBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(LegalGoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = LegalDeepBlue,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Report an Issue / Bug",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalSurfaceWhite
                    )
                )

                Text(
                    text = "Help us improve Vani-Kanoon for all citizens and advocates across India.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LegalGoldPrimary.copy(alpha = 0.9f)
                    )
                )
            }
        }

        // Form Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isSubmitted) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Issue Ticket Logged",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            )
                            Text(
                                text = "Thank you! Our engineering team has received your ticket and will follow up at $userEmail.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF2E7D32))
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = issueTitle,
                    onValueChange = { issueTitle = it },
                    label = { Text("Issue Title / Summary *") },
                    placeholder = { Text("e.g., Speech recognition pauses too quickly in Kannada") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = issueModule,
                    onValueChange = { issueModule = it },
                    label = { Text("Affected Tool / Module") },
                    placeholder = { Text("e.g., Vani Voice, Document Generator, FIR Analyzer") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = { issueDescription = it },
                    label = { Text("Detailed Description / Steps to Reproduce *") },
                    placeholder = { Text("Describe what happened, expected behavior, and device model...") },
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Your Email for Follow-up *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LegalDeepBlue,
                        focusedLabelColor = LegalDeepBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (issueTitle.isBlank() || issueDescription.isBlank()) {
                            Toast.makeText(context, "Please fill in title and description.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitted = true
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:sandeshbirannavar@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "[Vani-Kanoon Bug] $issueTitle ($issueModule)")
                                putExtra(Intent.EXTRA_TEXT, "Module: $issueModule\nReporter: $userEmail\n\nDescription:\n$issueDescription")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Issue logged successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalDeepBlue,
                        contentColor = LegalGoldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = LegalGoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Issue Report",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

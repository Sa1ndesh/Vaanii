package com.vanikanoon.app.ui.screens.analyzer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.vanikanoon.app.data.models.FirAnalysisResult
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.theme.LegalBgLight
import com.vanikanoon.app.ui.theme.LegalBorder
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceVariant
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary
import kotlinx.coroutines.launch

@Composable
fun FirAnalyzerScreen(
    repository: LegalRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var firInputText by remember {
        mutableStateOf(
            """
FIRST INFORMATION REPORT (Under Section 173 BNSS / 154 CrPC)
Police Station: Koramangala PS, Bengaluru City | FIR No: 0142/2025 | Date: 12-02-2025
Complainant: Sri Harish Gowda, residing at #44, 5th Block, Koramangala
Incident Date & Time: 11-02-2025 at about 21:30 hrs
Place of Occurrence: Commercial Complex parking lot, 80 Feet Road, Koramangala

Details of Complaint:
The complainant states that on 11-02-2025 around 9:30 PM, while returning from his office, the accused persons named Rahul Verma and two unknown accomplices wrongfully restrained the complainant, used abusive language, voluntarily caused hurt by hitting with iron rod on the shoulder, and forcibly snatched the complainant's gold chain weighing 24 grams and iPhone 15 Pro. The incident was witnessed by security guard Sri Basavaraj and shop owner Sri Vignesh. The accused fled on an unregistered motorcycle.

Offences Invoked:
Section 303(2) BNS (Theft), Section 115(2) BNS (Voluntarily Causing Hurt), Section 126(2) BNS (Wrongful Restraint), Section 351(2) BNS (Criminal Intimidation).
Investigating Officer: Sub-Inspector S. Murthy.
            """.trimIndent()
        )
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<FirAnalysisResult?>(null) }

    fun runAnalysis() {
        if (firInputText.isBlank() || isAnalyzing) return
        isAnalyzing = true
        scope.launch {
            analysisResult = repository.analyzeFir(firInputText)
            isAnalyzing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("fir_analyzer_screen")
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LegalDeepBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LegalGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = LegalDeepBlue)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FIR & Evidence Intelligence",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalSurfaceWhite
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Parse police complaints, verify BNS/BNSS statutory sections, check bailability, and compute legal defense strategies.",
                    style = MaterialTheme.typography.bodySmall.copy(color = LegalSurfaceWhite.copy(alpha = 0.85f))
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Input text area
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Paste Police FIR / Written Complaint",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalDeepBlue
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = firInputText,
                    onValueChange = { firInputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("fir_text_input"),
                    placeholder = { Text("Paste the text of the FIR here...") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            firInputText = """
FIR No: 89/2025 | Cyber Crime PS | Date: 18-01-2025
Complainant: Priya Sundaram
Accused: Unknown Cyber Fraudsters (Telegram ID @fast_wealth_7)
Offences: Section 66D IT Act 2000, Section 318(4) BNS (Cheating)
Summary: Complainant was induced to invest ₹4,20,000/- in fraudulent crypto investment scheme through unauthorized UPI handles.
                            """.trimIndent()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Load Cyber FIR", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { runAnalysis() },
                        enabled = firInputText.isNotBlank() && !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LegalDeepBlue,
                            contentColor = LegalGoldPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analyze_fir_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = LegalGoldPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Analyze FIR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Analysis Results Breakdown
        analysisResult?.let { result ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LegalGoldPrimary, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📋 Structured FIR Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (result.legalRiskLevel.contains("High")) Color(0xFFFEE2E2) else LegalGoldContainer
                        ) {
                            Text(
                                text = "Risk: ${result.legalRiskLevel}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (result.legalRiskLevel.contains("High")) Color(0xFFDC2626) else LegalGoldDark,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FirDetailRow("FIR No & PS", "${result.firNumber} • ${result.policeStation}")
                    FirDetailRow("Date & Time", "${result.dateOfFiling} (Incident: ${result.incidentDateTime})")
                    FirDetailRow("Complainant", result.complainant)
                    FirDetailRow("Accused", result.accused.joinToString(", "))
                    FirDetailRow("Witnesses", result.witnesses.joinToString(", "))
                    FirDetailRow("Statutory Sections", result.offencesMentioned.joinToString("; "))
                    FirDetailRow("Bailability Status", result.bailableStatus)
                    FirDetailRow("Investigating Officer", result.investigatingOfficer)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "⚖️ Recommended Legal Action & Defense:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalDeepBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    result.proceduralAdvice.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", color = LegalGoldDark, fontWeight = FontWeight.Bold)
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = LegalTextPrimary,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = {
                            val shareText = "FIR Analysis (${result.firNumber}):\nSections: ${result.offencesMentioned}\nBailability: ${result.bailableStatus}\nAdvice: ${result.proceduralAdvice.joinToString("\n")}"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "FIR Legal Assessment")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share FIR Analysis"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = LegalDeepBlue)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun FirDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = LegalDeepBlue
            ),
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextPrimary),
            modifier = Modifier.weight(1f)
        )
    }
}

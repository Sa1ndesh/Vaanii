package com.vanikanoon.app.ui.screens.summarizer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.data.models.CaseSummary
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.theme.LegalBgLight
import com.vanikanoon.app.ui.theme.LegalBorder
import com.vanikanoon.app.ui.theme.LegalDeepBlue
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaseSummarizerScreen(
    repository: LegalRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var caseInputText by remember {
        mutableStateOf(
            """
IN THE SUPREME COURT OF INDIA
CRIMINAL APPELLATE JURISDICTION
Writ Petition (Criminal) No. 68 of 2008
Lalita Kumari ... Petitioner
Versus
Govt. of U.P. & Others ... Respondents

BENCH: P. Sathasivam (CJI), B.S. Chauhan, Ranjana P. Desai, Ranjan Gogoi, S.A. Bobde, JJ.

JUDGMENT:
The core legal question referred to this Constitution Bench is whether a police officer is bound to register a First Information Report (FIR) upon receiving any information relating to commission of a cognizable offence under Section 154 of the Code of Criminal Procedure, 1973, or if the police officer has the power, discretion, or authority to conduct a preliminary inquiry before registration of an FIR.

HELD:
1. The language of Section 154(1) CrPC uses the mandatory word 'shall', leaving no discretion with the police officer if the information discloses a cognizable offence.
2. Registration of FIR is mandatory under Section 154 of the Code, if the information discloses commission of a cognizable offence and no preliminary inquiry is permissible in such a situation.
3. If the information does not disclose a cognizable offence but indicates the necessity for an inquiry, a preliminary inquiry may be conducted only to ascertain whether cognizable offence is disclosed or not.
4. A preliminary inquiry must be completed expeditiously within 7 days.
            """.trimIndent()
        )
    }

    var isSummarizing by remember { mutableStateOf(false) }
    var caseSummary by remember { mutableStateOf<CaseSummary?>(null) }

    val landmarkPresets = listOf(
        "Lalita Kumari v. Govt of UP (Mandatory FIR)",
        "Arnesh Kumar v. State of Bihar (Arrest Limits)",
        "Maneka Gandhi v. UOI (Article 21 Due Process)",
        "K.S. Puttaswamy v. UOI (Right to Privacy)"
    )

    fun loadPreset(name: String) {
        caseInputText = when {
            name.contains("Arnesh") -> """
IN THE SUPREME COURT OF INDIA
Arnesh Kumar v. State of Bihar & Anr. (2014) 8 SCC 273
Bench: Chandramauli Kr. Prasad, Pinaki Chandra Ghose, JJ.

Facts: The appellant preferred an appeal apprehending arrest under Section 498A IPC and Section 4 of the Dowry Prohibition Act.
Issues: Unchecked and mechanical arrests made by police in offences carrying imprisonment up to 7 years.
Held: Police officers cannot automatically arrest accused under Section 498A IPC without satisfying Section 41 CrPC requirements. A notice of appearance under Section 41A CrPC must be served within two weeks from date of institution.
            """.trimIndent()
            name.contains("Maneka") -> """
IN THE SUPREME COURT OF INDIA
Maneka Gandhi v. Union of India (1978) 1 SCC 248
Bench: M.H. Beg (CJI), Y.V. Chandrachud, P.N. Bhagwati, V.R. Krishna Iyer, N.L. Untwalia, P.S. Kailasam, S. Murtaza Fazal Ali, JJ.

Facts: Petitioner's passport was impounded by the government under Section 10(3)(c) of Passport Act without furnishing any reasons.
Held: Procedure established by law under Article 21 must be 'right, just, and fair' and not arbitrary. Articles 14, 19, and 21 form a Golden Triangle and must be read together.
            """.trimIndent()
            name.contains("Puttaswamy") -> """
IN THE SUPREME COURT OF INDIA
Justice K.S. Puttaswamy (Retd.) v. Union of India (2017) 10 SCC 1
Bench: 9-Judge Constitution Bench (J.S. Khehar CJI, J. Chelameswar, S.A. Bobde, D.Y. Chandrachud et al.)

Facts: Challenge to Aadhaar scheme on grounds of infringement of privacy.
Held: Right to Privacy is a Fundamental Right guaranteed under Article 21 and Part III of the Constitution of India. Any limitation must satisfy proportionality, legality, and legitimate state aim.
            """.trimIndent()
            else -> caseInputText
        }
    }

    fun runSummarizer() {
        if (caseInputText.isBlank() || isSummarizing) return
        isSummarizing = true
        scope.launch {
            caseSummary = repository.summarizeCaseJudgment(caseInputText)
            isSummarizing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("case_summarizer_screen")
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
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = LegalDeepBlue)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "9-Point Case Law Summarizer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalSurfaceWhite
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Distills complex Supreme Court and High Court judgments into facts, issues, statutory sections, arguments, and Ratio Decidendi.",
                    style = MaterialTheme.typography.bodySmall.copy(color = LegalSurfaceWhite.copy(alpha = 0.85f))
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Chips
        Text(
            text = "Landmark Supreme Court Presets:",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LegalDeepBlue
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            landmarkPresets.forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = LegalSurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LegalBorder),
                    modifier = Modifier.clickable { loadPreset(preset) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = LegalGoldDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall.copy(color = LegalDeepBlue)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Judgment Text Box
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Paste Court Judgment / Legal Text",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalDeepBlue
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = caseInputText,
                    onValueChange = { caseInputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("judgment_text_input"),
                    placeholder = { Text("Paste judgment or order text here...") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { runSummarizer() },
                    enabled = caseInputText.isNotBlank() && !isSummarizing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalDeepBlue,
                        contentColor = LegalGoldPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("summarize_case_button")
                ) {
                    if (isSummarizing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LegalGoldPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Generate 9-Point Deep Summary", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Formatted Summary Output
        caseSummary?.let { summary ->
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
                            text = "📜 9-Point Judicial Summary",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )

                        Row {
                            IconButton(onClick = {
                                val fullText = """
${summary.caseTitle} (${summary.courtAndBench})
Citation: ${summary.citation}
Facts: ${summary.factsOfTheCase}
Issues: ${summary.keyLegalIssues.joinToString("; ")}
Statutes: ${summary.statutesAndSectionsInvoked.joinToString("; ")}
Ratio Decidendi: ${summary.reasoningAndRatioDecidendi}
Disposition: ${summary.finalJudgment}
                                """.trimIndent()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Case Summary", fullText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = LegalDeepBlue)
                            }

                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, summary.caseTitle)
                                    putExtra(Intent.EXTRA_TEXT, "${summary.caseTitle}\n\nRatio Decidendi:\n${summary.reasoningAndRatioDecidendi}\n\nDecision:\n${summary.finalJudgment}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Summary"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = LegalDeepBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SummarySectionCard("1. Case Title & Citation", "${summary.caseTitle}\nCitation: ${summary.citation}")
                    SummarySectionCard("2. Court & Bench", summary.courtAndBench)
                    SummarySectionCard("3. Representation", summary.partiesInvolved)
                    SummarySectionCard("4. Facts of the Case", summary.factsOfTheCase)
                    SummarySectionCard("5. Key Legal Issues", summary.keyLegalIssues.joinToString("\n• ", prefix = "• "))
                    SummarySectionCard("6. Statutory Provisions", summary.statutesAndSectionsInvoked.joinToString("\n• ", prefix = "• "))
                    SummarySectionCard("7. Arguments of Parties", summary.petitionerArguments)
                    SummarySectionCard("8. Reasoning & Ratio Decidendi", summary.reasoningAndRatioDecidendi, highlight = true)
                    SummarySectionCard("9. Final Decision & Significance", "${summary.finalJudgment}\n\nSignificance: ${summary.landmarkSignificance}")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SummarySectionCard(title: String, content: String, highlight: Boolean = false) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) LegalGoldContainer.copy(alpha = 0.5f) else LegalBgLight
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (highlight) LegalGoldDark else LegalDeepBlue
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LegalTextPrimary,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

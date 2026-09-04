package com.vanikanoon.app.ui.screens.learning

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.data.models.AnswerEvaluation
import com.vanikanoon.app.data.models.BareActSimplified
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.screens.summarizer.SummarySectionCard
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

@Composable
fun LearningHubScreen(
    repository: LegalRepository
) {
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Bare Act Simplifier, 1 = Exam Answer Evaluator

    // Bare Act Fields
    var actName by remember { mutableStateOf("Bharatiya Nyaya Sanhita, 2023 (BNS)") }
    var sectionNumber by remember { mutableStateOf("303 (Theft)") }
    var isSimplifying by remember { mutableStateOf(false) }
    var simplifiedResult by remember { mutableStateOf<BareActSimplified?>(null) }

    // Answer Evaluator Fields
    var examQuestion by remember {
        mutableStateOf("Explain the essentials of Theft under Section 303 BNS (IPC 379) and distinguish it from Criminal Misappropriation.")
    }
    var studentAnswer by remember {
        mutableStateOf(
            """
Theft is defined under Section 303 of BNS.
The essentials are:
1. Dishonest intention to take movable property.
2. The property must be in possession of another person.
3. Taking without that person's consent.
4. Moving the property in order to effect such taking.

Distinction from Criminal Misappropriation: In theft, the property is taken out of someone's possession dishonestly. In misappropriation (Sec 314 BNS / IPC 403), the property comes into possession innocently first, and dishonest intention develops subsequently.
            """.trimIndent()
        )
    }
    var isEvaluating by remember { mutableStateOf(false) }
    var evaluationResult by remember { mutableStateOf<AnswerEvaluation?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("learning_hub_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = LegalDeepBlue,
            contentColor = Color.White,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = LegalGoldPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Bare Act Simplifier", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Exam Answer Evaluator (Marks/10)", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (activeTab == 0) {
                // Bare Act Simplifier View
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
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
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = LegalDeepBlue)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Bare Act Simplifier",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )
                                Text(
                                    text = "Deconstruct statutes into plain meaning, ingredients & mnemonics",
                                    style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = actName,
                            onValueChange = { actName = it },
                            label = { Text("Act / Code Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        )

                        OutlinedTextField(
                            value = sectionNumber,
                            onValueChange = { sectionNumber = it },
                            label = { Text("Section Number / Title") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        )

                        Button(
                            onClick = {
                                if (actName.isNotBlank() && !isSimplifying) {
                                    isSimplifying = true
                                    scope.launch {
                                        simplifiedResult = repository.simplifyBareAct(actName, sectionNumber)
                                        isSimplifying = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LegalDeepBlue,
                                contentColor = LegalGoldPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isSimplifying) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LegalGoldPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simplify Bare Act Section", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                simplifiedResult?.let { sim ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LegalGoldPrimary, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📖 ${sim.title}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LegalDeepBlue
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SummarySectionCard("Plain Language Summary", sim.plainEnglishMeaning, highlight = true)
                            SummarySectionCard("Core Legal Ingredients", sim.legalIngredients.joinToString("\n• ", prefix = "• "))
                            SummarySectionCard("Exceptions / Defenses", sim.exceptions.joinToString("\n• ", prefix = "• "))
                            SummarySectionCard("Practical Illustration", sim.realLifeIllustration)
                            SummarySectionCard("Landmark Precedents", sim.landmarkPrecedents.joinToString("\n• ", prefix = "• "))
                            SummarySectionCard("Memory Mnemonic", sim.memoryMnemonic, highlight = true)
                        }
                    }
                }
            } else {
                // Law Answer Evaluator View
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
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
                                Icon(Icons.Default.School, contentDescription = null, tint = LegalDeepBlue)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Law Exam Evaluator & Grader",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )
                                Text(
                                    text = "Judicial services & LLB exam grading with model answers",
                                    style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = examQuestion,
                            onValueChange = { examQuestion = it },
                            label = { Text("Law Exam / Mock Question") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        )

                        OutlinedTextField(
                            value = studentAnswer,
                            onValueChange = { studentAnswer = it },
                            label = { Text("Your Written Answer Draft") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(bottom = 14.dp)
                        )

                        Button(
                            onClick = {
                                if (examQuestion.isNotBlank() && studentAnswer.isNotBlank() && !isEvaluating) {
                                    isEvaluating = true
                                    scope.launch {
                                        evaluationResult = repository.evaluateAnswer(examQuestion, studentAnswer)
                                        isEvaluating = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LegalDeepBlue,
                                contentColor = LegalGoldPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isEvaluating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LegalGoldPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Evaluate Answer & Award Marks", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                evaluationResult?.let { eval ->
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
                                Column {
                                    Text(
                                        text = "Grade: ${eval.grade}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = LegalDeepBlue
                                        )
                                    )
                                    Text(
                                        text = "Score: ${eval.marksAwarded} / ${eval.maxMarks} Marks",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = LegalGoldDark
                                        )
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = LegalGoldContainer,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${eval.marksAwarded * 10}%",
                                            fontWeight = FontWeight.Bold,
                                            color = LegalGoldDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SummarySectionCard("✅ Strengths in Draft", eval.positiveAspects.joinToString("\n• ", prefix = "• "))
                            SummarySectionCard("⚠️ Missing Ingredients & Flaws", eval.missingElements.joinToString("\n• ", prefix = "• "), highlight = true)
                            SummarySectionCard("🏆 Model 10/10 Answer Structure", eval.modelAnswer)
                            SummarySectionCard("💡 Examiner Tips for High Marks", eval.examinerTips.joinToString("\n• ", prefix = "• "))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

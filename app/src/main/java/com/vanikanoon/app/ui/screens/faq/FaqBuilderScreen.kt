package com.vanikanoon.app.ui.screens.faq

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Share
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
import com.vanikanoon.app.data.models.FaqCollection
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
fun FaqBuilderScreen(
    repository: LegalRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var topicQuery by remember { mutableStateOf("Tenant Eviction & Security Deposit Disputes") }
    var isGenerating by remember { mutableStateOf(false) }
    var faqCollection by remember { mutableStateOf<FaqCollection?>(null) }

    val presetTopics = listOf(
        "Tenant Eviction & Rent Deposit",
        "Section 138 Cheque Dishonour",
        "Online Cyber Financial Fraud",
        "Unpaid Salary & FnF Dues",
        "Domestic Violence & Maintenance",
        "Defective Goods & Consumer Court"
    )

    fun generateFaqsFor(topic: String) {
        if (topic.isBlank() || isGenerating) return
        isGenerating = true
        scope.launch {
            faqCollection = repository.generateFaqs(topic)
            isGenerating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .padding(16.dp)
            .testTag("faq_builder_screen")
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
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = LegalDeepBlue)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Client FAQ Builder",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LegalSurfaceWhite
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Generate client-ready, categorized FAQs with statutory citations for any legal subject matter.",
                    style = MaterialTheme.typography.bodySmall.copy(color = LegalSurfaceWhite.copy(alpha = 0.85f))
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Topic Presets
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetTopics.forEach { topic ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = LegalSurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LegalBorder),
                    modifier = Modifier.clickable {
                        topicQuery = topic
                        generateFaqsFor(topic)
                    }
                ) {
                    Text(
                        text = topic,
                        style = MaterialTheme.typography.labelSmall.copy(color = LegalDeepBlue),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = topicQuery,
                onValueChange = { topicQuery = it },
                label = { Text("Topic / Legal Area") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { generateFaqsFor(topicQuery) },
                enabled = topicQuery.isNotBlank() && !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LegalDeepBlue,
                    contentColor = LegalGoldPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(54.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LegalGoldPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Build FAQs", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FAQ Items List
        faqCollection?.let { collection ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 FAQs on: ${collection.topic}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        IconButton(onClick = {
                            val formatted = collection.items.joinToString("\n\n") { "Q: ${it.question}\nA: ${it.answer} (${it.relevantLaw})" }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "FAQs on ${collection.topic}")
                                putExtra(Intent.EXTRA_TEXT, formatted)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share FAQs"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = LegalDeepBlue)
                        }
                    }
                }

                items(collection.items) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LegalBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = LegalGoldDark,
                                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.question,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.answer,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LegalTextPrimary,
                                    lineHeight = 20.sp
                                )
                            )
                            if (item.relevantLaw != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LegalGoldContainer,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = "🏛️ ${item.relevantLaw}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LegalGoldDark,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

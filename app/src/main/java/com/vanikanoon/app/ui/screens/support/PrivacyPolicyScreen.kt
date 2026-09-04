package com.vanikanoon.app.ui.screens.support

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun PrivacyPolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("privacy_policy_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = LegalDeepBlue,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalSurfaceWhite
                    )
                )

                Text(
                    text = "Digital Personal Data Protection (DPDP) Act Compliant • 2026",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LegalGoldPrimary.copy(alpha = 0.9f)
                    )
                )
            }
        }

        TermsSectionCard(
            sectionNumber = "1",
            title = "Information We Process",
            content = "Vani-Kanoon processes user-supplied information strictly for executing requested legal tasks. This includes: contract party names, addresses, rental sums, audio transcripts for voice legal queries, and FIR summaries for analysis."
        )

        TermsSectionCard(
            sectionNumber = "2",
            title = "Local-First Storage & Encryption",
            content = "All generated legal documents, chat logs, and demand notices are stored on your device in local SQLite databases (Room persistence). Your sensitive drafts remain in your custody."
        )

        TermsSectionCard(
            sectionNumber = "3",
            title = "Voice Data & Audio Processing",
            content = "Voice inputs in Kannada, Marathi, Hindi, and English are processed securely for real-time speech recognition. Audio recordings are not permanently stored or sold to third-party advertising networks."
        )

        TermsSectionCard(
            sectionNumber = "4",
            title = "Third-Party AI Processing",
            content = "When advanced generative legal synthesis is triggered, queries are passed via enterprise SSL/TLS encrypted APIs to Google Gemini AI. Data is governed by Google Cloud enterprise privacy standards."
        )

        TermsSectionCard(
            sectionNumber = "5",
            title = "Contact Data Protection Officer",
            content = "For any data deletion requests or privacy inquiries, contact our Data Privacy Officer at sandeshbirannavar@gmail.com."
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

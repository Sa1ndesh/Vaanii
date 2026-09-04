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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
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
import com.vanikanoon.app.ui.theme.LegalGoldContainer
import com.vanikanoon.app.ui.theme.LegalGoldDark
import com.vanikanoon.app.ui.theme.LegalGoldPrimary
import com.vanikanoon.app.ui.theme.LegalSurfaceWhite
import com.vanikanoon.app.ui.theme.LegalTextPrimary
import com.vanikanoon.app.ui.theme.LegalTextSecondary

@Composable
fun TermsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("terms_screen"),
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
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = LegalDeepBlue,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Terms & Conditions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LegalSurfaceWhite
                    )
                )

                Text(
                    text = "Last Updated: 2026 • Governing Law: Republic of India",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LegalGoldPrimary.copy(alpha = 0.9f)
                    )
                )
            }
        }

        TermsSectionCard(
            sectionNumber = "1",
            title = "Nature of Service & Educational Purpose",
            content = "Vani-Kanoon is an Artificial Intelligence-powered legal assistant designed to provide automated document drafts, statutory information, FIR analysis, and case summarization under Indian Law (Bharatiya Nyaya Sanhita 2023, Bharatiya Nagarik Suraksha Sanhita 2023, CPC, and Consumer Protection Act 2019). The platform does not constitute an advocate-client relationship under the Advocates Act, 1961."
        )

        TermsSectionCard(
            sectionNumber = "2",
            title = "Document Drafting & User Responsibility",
            content = "Automated contract templates (such as NDAs, Rent Agreements, Sale Deeds, Lease Deeds, and Affidavits) are standard legal drafts provided for informational and preliminary drafting purposes. Users must exercise due diligence and have all high-value contracts vetted by an enrolled advocate prior to execution and registration."
        )

        TermsSectionCard(
            sectionNumber = "3",
            title = "Intellectual Property & Fair Usage",
            content = "All proprietary algorithms, voice models, RAG retrieval workflows, and application code are intellectual property of Vani-Kanoon. Users are granted a non-exclusive license to use the generated legal documents for personal and commercial business needs."
        )

        TermsSectionCard(
            sectionNumber = "4",
            title = "Limitation of Liability",
            content = "Under no circumstances shall Vani-Kanoon, its developers, or contributors be held liable for any direct, indirect, incidental, or consequential damages resulting from legal outcomes, court rulings, or contractual disputes arising out of the use of AI-assisted outputs."
        )

        TermsSectionCard(
            sectionNumber = "5",
            title = "Jurisdiction & Dispute Resolution",
            content = "Any disputes arising out of or related to the usage of Vani-Kanoon services shall be governed exclusively by the laws of India and subject to the exclusive jurisdiction of the competent courts in Bengaluru, Karnataka."
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TermsSectionCard(
    sectionNumber: String,
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$sectionNumber. $title",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LegalDeepBlue
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LegalTextSecondary,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

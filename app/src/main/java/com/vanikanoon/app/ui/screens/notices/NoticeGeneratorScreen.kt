package com.vanikanoon.app.ui.screens.notices

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanikanoon.app.data.knowledge.DocumentTemplates
import com.vanikanoon.app.data.models.NoticeType
import com.vanikanoon.app.data.models.SavedNotice
import com.vanikanoon.app.data.repository.LegalRepository
import com.vanikanoon.app.ui.screens.docs.LegalInputSectionTitle
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
fun NoticeGeneratorScreen(
    repository: LegalRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedNoticeType by remember { mutableStateOf(NoticeType.UNPAID_SALARY) }
    var activeTab by remember { mutableIntStateOf(0) }
    var generatedNotice by remember { mutableStateOf<String?>(null) }

    // Salary Notice Fields
    var empName by remember { mutableStateOf("Siddharth Menon") }
    var empAddress by remember { mutableStateOf("Flat 204, Green Palms, HSR Layout, Bengaluru - 560102") }
    var compName by remember { mutableStateOf("AcroTech Technologies India Pvt Ltd") }
    var compAddress by remember { mutableStateOf("Prestige Tech Park, Marathahalli, Bengaluru - 560103") }
    var empDesignation by remember { mutableStateOf("Senior Software Engineer") }
    var unpaidSalaryAmount by remember { mutableStateOf("185000") }
    var periodFrom by remember { mutableStateOf("01-11-2024") }
    var periodTo by remember { mutableStateOf("31-01-2025") }
    var noticeCity by remember { mutableStateOf("Bengaluru") }

    // Loan / Cheque Notice Fields
    var lenderName by remember { mutableStateOf("Raghavendra Rao") }
    var lenderAddress by remember { mutableStateOf("#88, 5th Cross, Malleshwaram, Bengaluru - 560003") }
    var borrowerName by remember { mutableStateOf("Kiran Kumar Shetty") }
    var borrowerAddress by remember { mutableStateOf("#19, Jayanagar 7th Block, Bengaluru - 560082") }
    var loanAmount by remember { mutableStateOf("350000") }
    var loanDate by remember { mutableStateOf("15-06-2024") }
    var dueDate by remember { mutableStateOf("15-12-2024") }
    var interestRate by remember { mutableStateOf("12%") }
    var chequeNumber by remember { mutableStateOf("482910 (HDFC Bank)") }

    val savedNotices by repository.getSavedNotices().collectAsState(initial = emptyList())

    fun generateNoticeDraft() {
        val draft = when (selectedNoticeType) {
            NoticeType.UNPAID_SALARY -> DocumentTemplates.generateSalaryNotice(
                employeeName = empName,
                employeeAddress = empAddress,
                companyName = compName,
                companyAddress = compAddress,
                designation = empDesignation,
                unpaidAmount = unpaidSalaryAmount.toLongOrNull() ?: 100000L,
                periodFrom = periodFrom,
                periodTo = periodTo,
                city = noticeCity
            )
            NoticeType.LOAN_REPAYMENT -> DocumentTemplates.generateLoanRepaymentNotice(
                lenderName = lenderName,
                lenderAddress = lenderAddress,
                borrowerName = borrowerName,
                borrowerAddress = borrowerAddress,
                loanAmount = loanAmount.toLongOrNull() ?: 200000L,
                loanDate = loanDate,
                dueDate = dueDate,
                interestRate = interestRate,
                chequeNumber = chequeNumber,
                city = noticeCity
            )
        }
        generatedNotice = draft
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("notice_generator_screen")
    ) {
        // Tab Navigation
        Surface(color = LegalDeepBlue, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeTab == 0) LegalGoldPrimary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = { activeTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = if (activeTab == 0) LegalDeepBlue else Color.White
                        )
                    ) {
                        Text("Notice Generator", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeTab == 1) LegalGoldPrimary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = { activeTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = if (activeTab == 1) LegalDeepBlue else Color.White
                        )
                    ) {
                        Text("Saved Notices (${savedNotices.size})", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        if (activeTab == 1) {
            // Saved Notices List
            if (savedNotices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved legal notices yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = LegalTextSecondary)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedNotices) { notice ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LegalBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = LegalGoldContainer
                                    ) {
                                        Text(
                                            text = notice.type.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = LegalGoldDark,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { scope.launch { repository.deleteNotice(notice.id) } },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "From: ${notice.senderName} → To: ${notice.recipientName}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        generatedNotice = notice.content
                                        activeTab = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LegalDeepBlue,
                                        contentColor = LegalGoldPrimary
                                    )
                                ) {
                                    Text("Open Notice")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Type Selector
                TabRow(
                    selectedTabIndex = NoticeType.values().indexOf(selectedNoticeType),
                    containerColor = LegalSurfaceWhite,
                    contentColor = LegalDeepBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[NoticeType.values().indexOf(selectedNoticeType)]),
                            color = LegalGoldPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    NoticeType.values().forEach { type ->
                        Tab(
                            selected = selectedNoticeType == type,
                            onClick = {
                                selectedNoticeType = type
                                generatedNotice = null
                            },
                            text = {
                                Text(
                                    text = when (type) {
                                        NoticeType.UNPAID_SALARY -> "Unpaid Salary"
                                        NoticeType.LOAN_REPAYMENT -> "Loan Default / 138 NI"
                                    },
                                    fontWeight = if (selectedNoticeType == type) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = selectedNoticeType.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        Text(
                            text = "Statutory Citation: ${selectedNoticeType.actCitation}",
                            style = MaterialTheme.typography.bodySmall.copy(color = LegalGoldDark, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedNoticeType) {
                    NoticeType.UNPAID_SALARY -> {
                        LegalInputSectionTitle("1. Employee (Sender)")
                        OutlinedTextField(
                            value = empName,
                            onValueChange = { empName = it },
                            label = { Text("Employee Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = empDesignation,
                            onValueChange = { empDesignation = it },
                            label = { Text("Designation") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = empAddress,
                            onValueChange = { empAddress = it },
                            label = { Text("Residential Address & Contact") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("2. Company / Employer (Recipient)")
                        OutlinedTextField(
                            value = compName,
                            onValueChange = { compName = it },
                            label = { Text("Company Legal Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = compAddress,
                            onValueChange = { compAddress = it },
                            label = { Text("Company Registered Office") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("3. Claim Dues & Salary Period")
                        OutlinedTextField(
                            value = unpaidSalaryAmount,
                            onValueChange = { unpaidSalaryAmount = it },
                            label = { Text("Total Unpaid Amount (₹)") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = periodFrom,
                                onValueChange = { periodFrom = it },
                                label = { Text("Period From") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = periodTo,
                                onValueChange = { periodTo = it },
                                label = { Text("Period To") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    NoticeType.LOAN_REPAYMENT -> {
                        LegalInputSectionTitle("1. Creditor / Lender")
                        OutlinedTextField(
                            value = lenderName,
                            onValueChange = { lenderName = it },
                            label = { Text("Lender Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = lenderAddress,
                            onValueChange = { lenderAddress = it },
                            label = { Text("Lender Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("2. Debtor / Borrower")
                        OutlinedTextField(
                            value = borrowerName,
                            onValueChange = { borrowerName = it },
                            label = { Text("Borrower Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = borrowerAddress,
                            onValueChange = { borrowerAddress = it },
                            label = { Text("Borrower Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("3. Debt & Dishonoured Cheque Particulars")
                        OutlinedTextField(
                            value = loanAmount,
                            onValueChange = { loanAmount = it },
                            label = { Text("Principal Debt Amount (₹)") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = chequeNumber,
                            onValueChange = { chequeNumber = it },
                            label = { Text("Dishonoured Cheque No. & Bank Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { generateNoticeDraft() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalDeepBlue,
                        contentColor = LegalGoldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_notice_button")
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Formal Legal Notice",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (generatedNotice != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                                    text = "⚖️ Generated Legal Notice",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )

                                Row {
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Legal Notice", generatedNotice)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Notice copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = LegalDeepBlue)
                                    }

                                    IconButton(onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, selectedNoticeType.title)
                                            putExtra(Intent.EXTRA_TEXT, generatedNotice)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Notice"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = LegalDeepBlue)
                                    }

                                    IconButton(onClick = {
                                        val saved = SavedNotice(
                                            type = selectedNoticeType,
                                            senderName = if (selectedNoticeType == NoticeType.UNPAID_SALARY) empName else lenderName,
                                            recipientName = if (selectedNoticeType == NoticeType.UNPAID_SALARY) compName else borrowerName,
                                            content = generatedNotice!!
                                        )
                                        scope.launch {
                                            repository.saveNotice(saved)
                                            Toast.makeText(context, "Notice saved to device", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(Icons.Default.Bookmark, contentDescription = "Save", tint = LegalGoldDark)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LegalBgLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = generatedNotice!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = LegalTextPrimary
                                    ),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

package com.vanikanoon.app.ui.screens.docs

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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import com.vanikanoon.app.data.models.DocumentType
import com.vanikanoon.app.data.models.SavedDocument
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
fun DocumentGeneratorScreen(
    repository: LegalRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDocType by remember { mutableStateOf(DocumentType.NDA) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Draft Generator, 1 = Saved Documents

    // Draft Preview state
    var generatedDraft by remember { mutableStateOf<String?>(null) }

    // NDA Form Fields
    var ndaDisclosingName by remember { mutableStateOf("Infosys Technologies Ltd") }
    var ndaDisclosingAddress by remember { mutableStateOf("Electronic City, Bengaluru, Karnataka - 560100") }
    var ndaReceivingName by remember { mutableStateOf("NexGen Software Solutions Pvt Ltd") }
    var ndaReceivingAddress by remember { mutableStateOf("Koramangala 4th Block, Bengaluru, Karnataka - 560034") }
    var ndaPurpose by remember { mutableStateOf("exploring strategic AI software integration and mutual commercial collaboration") }
    var ndaBusiness by remember { mutableStateOf("AI legal assistants, proprietary algorithms and cloud systems") }
    var ndaCity by remember { mutableStateOf("Bengaluru") }

    // Affidavit Form Fields
    var affDeponentName by remember { mutableStateOf("Ramesh Kumar Sharma") }
    var affAge by remember { mutableStateOf("38") }
    var affRelation by remember { mutableStateOf("Suresh Sharma") }
    var affAddress by remember { mutableStateOf("Flat 302, Green Glen Layout, Bellandur, Bengaluru - 560103") }
    var affPurpose by remember { mutableStateOf("declaration of official permanent address and name spelling correction across identity documents") }
    var affPlace by remember { mutableStateOf("Bengaluru") }
    var affIdentifier by remember { mutableStateOf("Adv. Ananya Rao (KAR/1429/2016)") }
    var affNotary by remember { mutableStateOf("K. V. Subrahmanyam, Advocate & Notary") }
    var affNotaryReg by remember { mutableStateOf("NOT/KA/8842/2019") }
    var affNotaryOffice by remember { mutableStateOf("City Civil Court Complex, Bengaluru") }

    // Rent Agreement Form Fields
    var rentCity by remember { mutableStateOf("Bengaluru") }
    var rentLandlordName by remember { mutableStateOf("Vikramaditya Hegde") }
    var rentLandlordAge by remember { mutableStateOf("52") }
    var rentLandlordRelation by remember { mutableStateOf("Late Narayana Hegde") }
    var rentLandlordAddress by remember { mutableStateOf("#45, 2nd Main, Indiranagar, Bengaluru - 560038") }
    var rentLandlordPhone by remember { mutableStateOf("+91 98450 12345") }
    var rentTenantName by remember { mutableStateOf("Pooja Nair") }
    var rentTenantAge by remember { mutableStateOf("29") }
    var rentTenantRelation by remember { mutableStateOf("Raghavan Nair") }
    var rentTenantAddress by remember { mutableStateOf("Flat 101, Palm Meadows, Whitefield, Bengaluru - 560066") }
    var rentTenantPhone by remember { mutableStateOf("+91 97420 54321") }
    var rentPropertyAddress by remember { mutableStateOf("House No. 128, 4th Cross, 1st Stage, Indiranagar, Bengaluru - 560038") }
    var rentPropertyDesc by remember { mutableStateOf("2 BHK Semi-furnished residential apartment on 1st floor with dedicated parking space") }
    var rentStartDate by remember { mutableStateOf("01-04-2025") }
    var rentDurationMonths by remember { mutableStateOf("11") }
    var rentMonthlyAmount by remember { mutableStateOf("28000") }
    var rentMonthlyWords by remember { mutableStateOf("Twenty-Eight Thousand") }
    var rentDeposit by remember { mutableStateOf("150000") }
    var rentPaymentMode by remember { mutableStateOf("Bank NEFT / UPI Transfer") }
    var rentNoticePeriod by remember { mutableStateOf("2") }

    // Sale Deed Fields
    var saleCity by remember { mutableStateOf("Bengaluru") }
    var saleSellerName by remember { mutableStateOf("Chandrashekhar Reddy") }
    var saleSellerAge by remember { mutableStateOf("60") }
    var saleSellerRelation by remember { mutableStateOf("Late Venkat Reddy") }
    var saleSellerAddress by remember { mutableStateOf("#12, HSR Layout Sector 2, Bengaluru - 560102") }
    var saleBuyerName by remember { mutableStateOf("Aditya Verma") }
    var saleBuyerAge by remember { mutableStateOf("34") }
    var saleBuyerRelation by remember { mutableStateOf("Mahesh Verma") }
    var saleBuyerAddress by remember { mutableStateOf("Flat 404, Prestige Ozone, Whitefield, Bengaluru - 560066") }
    var salePropertyAddress by remember { mutableStateOf("Site No. 84, Sy No. 14/2, Begur Hobli, Bengaluru South") }
    var saleOwnershipDetails by remember { mutableStateOf("Registered Sale Deed dated 14-06-2008 registered as Doc No. 452/2008 in SRO Begur") }
    var saleAmount by remember { mutableStateOf("8500000") }
    var saleAmountWords by remember { mutableStateOf("Eighty-Five Lakhs") }
    var saleSurveyNo by remember { mutableStateOf("14/2, Khata No. 1290") }
    var saleArea by remember { mutableStateOf("2400 sq.ft (30 ft x 80 ft)") }

    // Commercial Lease Deed Fields
    var leaseCity by remember { mutableStateOf("Bengaluru") }
    var leaseLessorName by remember { mutableStateOf("Brigade Commercial Ventures Ltd") }
    var leaseLessorAddress by remember { mutableStateOf("Tower A, World Trade Center, Rajajinagar, Bengaluru - 560055") }
    var leaseLesseeName by remember { mutableStateOf("Nexus Infotech Solutions Pvt Ltd") }
    var leaseLesseeAddress by remember { mutableStateOf("Unit 302, Outer Ring Road, Bellandur, Bengaluru - 560103") }
    var leasePropertyDesc by remember { mutableStateOf("Commercial Office Suite No. 402, 4th Floor (4,500 sq.ft Super Built-up)") }
    var leasePropertyAddress by remember { mutableStateOf("Tech Park Phase 1, Whitefield Main Road, Bengaluru - 560066") }
    var leaseDurationYears by remember { mutableStateOf("5") }
    var leaseMonthlyRent by remember { mutableStateOf("185000") }
    var leaseMonthlyWords by remember { mutableStateOf("One Lakh Eighty-Five Thousand") }
    var leaseDeposit by remember { mutableStateOf("1110000") }
    var leaseEscalation by remember { mutableStateOf("5") }
    var leaseLockIn by remember { mutableStateOf("24") }
    var leaseNoticePeriod by remember { mutableStateOf("3") }

    val savedDocuments by repository.getSavedDocuments().collectAsState(initial = emptyList())

    fun generateCurrentDraft() {
        val draft = when (selectedDocType) {
            DocumentType.NDA -> DocumentTemplates.generateNda(
                disclosingPartyName = ndaDisclosingName,
                disclosingPartyAddress = ndaDisclosingAddress,
                receivingPartyName = ndaReceivingName,
                receivingPartyAddress = ndaReceivingAddress,
                purpose = ndaPurpose,
                businessPurpose = ndaBusiness,
                durationYears = 2,
                jurisdictionCity = ndaCity
            )
            DocumentType.AFFIDAVIT -> DocumentTemplates.generateAffidavit(
                deponentName = affDeponentName,
                deponentAge = affAge.toIntOrNull() ?: 35,
                relationName = affRelation,
                deponentAddress = affAddress,
                purpose = affPurpose,
                verificationPlace = affPlace,
                identifierName = affIdentifier,
                notaryName = affNotary,
                notaryRegNo = affNotaryReg,
                notaryOfficeAddress = affNotaryOffice
            )
            DocumentType.RENT_AGREEMENT -> DocumentTemplates.generateRentAgreement(
                city = rentCity,
                landlordName = rentLandlordName,
                landlordAge = rentLandlordAge.toIntOrNull() ?: 50,
                landlordRelation = rentLandlordRelation,
                landlordAddress = rentLandlordAddress,
                landlordPhone = rentLandlordPhone,
                tenantName = rentTenantName,
                tenantAge = rentTenantAge.toIntOrNull() ?: 30,
                tenantRelation = rentTenantRelation,
                tenantAddress = rentTenantAddress,
                tenantPhone = rentTenantPhone,
                propertyAddress = rentPropertyAddress,
                propertyDescription = rentPropertyDesc,
                startDate = rentStartDate,
                durationMonths = rentDurationMonths.toIntOrNull() ?: 11,
                monthlyRent = rentMonthlyAmount.toLongOrNull() ?: 25000L,
                monthlyRentWords = rentMonthlyWords,
                dueDay = 5,
                paymentMode = rentPaymentMode,
                paymentDetails = "Bank Account Transfer / NEFT",
                securityDeposit = rentDeposit.toLongOrNull() ?: 100000L,
                usageType = "Residential",
                noticePeriodMonths = rentNoticePeriod.toIntOrNull() ?: 2
            )
            DocumentType.LEASE_DEED -> DocumentTemplates.generateLeaseDeed(
                executionCity = leaseCity,
                lessorName = leaseLessorName,
                lessorAddress = leaseLessorAddress,
                lesseeName = leaseLesseeName,
                lesseeAddress = leaseLesseeAddress,
                propertyAddress = leasePropertyAddress,
                propertyDescription = leasePropertyDesc,
                leaseDurationYears = leaseDurationYears.toIntOrNull() ?: 5,
                leaseRentAmount = leaseMonthlyRent.toLongOrNull() ?: 185000L,
                leaseRentWords = leaseMonthlyWords,
                securityDeposit = leaseDeposit.toLongOrNull() ?: 1110000L,
                terminationNoticeMonths = leaseNoticePeriod.toIntOrNull() ?: 3
            )
            DocumentType.SALE_DEED -> DocumentTemplates.generateSaleDeed(
                executionCity = saleCity,
                sellerName = saleSellerName,
                sellerAge = saleSellerAge.toIntOrNull() ?: 55,
                sellerRelation = saleSellerRelation,
                sellerAddress = saleSellerAddress,
                buyerName = saleBuyerName,
                buyerAge = saleBuyerAge.toIntOrNull() ?: 32,
                buyerRelation = saleBuyerRelation,
                buyerAddress = saleBuyerAddress,
                propertyAddress = salePropertyAddress,
                ownershipDetails = saleOwnershipDetails,
                saleAmount = saleAmount.toLongOrNull() ?: 5000000L,
                saleAmountWords = saleAmountWords,
                paymentMode = "RTGS / Cheque",
                paymentReference = "UTR# 998241941",
                paymentDate = "22-03-2025",
                propertyType = "Residential Plot & Schedule Property",
                propertyArea = saleArea,
                surveyNumber = saleSurveyNo,
                boundaryEast = "Road",
                boundaryWest = "Site No. 85",
                boundaryNorth = "Site No. 72",
                boundarySouth = "Site No. 96"
            )
        }
        generatedDraft = draft
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LegalBgLight)
            .testTag("document_generator_screen")
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
                        ),
                        modifier = Modifier.testTag("tab_draft_generator")
                    ) {
                        Text(
                            text = "Draft Generator",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
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
                        ),
                        modifier = Modifier.testTag("tab_saved_documents")
                    ) {
                        Text(
                            text = "Saved (${savedDocuments.size})",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (activeTab == 1) {
            // Saved Documents List
            if (savedDocuments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved legal documents yet.\nGenerate a contract and click 'Save to Device'.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LegalTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedDocuments) { doc ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                            text = doc.type.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = LegalGoldDark,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { scope.launch { repository.deleteDocument(doc.id) } },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = doc.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = doc.summary,
                                    style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            generatedDraft = doc.content
                                            activeTab = 0
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LegalDeepBlue,
                                            contentColor = LegalGoldPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Open Draft")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Draft Generator View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. Template Selector Tabs
                ScrollableTabRow(
                    selectedTabIndex = DocumentType.values().indexOf(selectedDocType),
                    containerColor = LegalSurfaceWhite,
                    contentColor = LegalDeepBlue,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[DocumentType.values().indexOf(selectedDocType)]),
                            color = LegalGoldPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    DocumentType.values().forEach { type ->
                        Tab(
                            selected = selectedDocType == type,
                            onClick = {
                                selectedDocType = type
                                generatedDraft = null
                            },
                            text = {
                                Text(
                                    text = when (type) {
                                        DocumentType.NDA -> "NDA"
                                        DocumentType.AFFIDAVIT -> "Affidavit"
                                        DocumentType.RENT_AGREEMENT -> "Rent Agreement"
                                        DocumentType.SALE_DEED -> "Sale Deed"
                                        DocumentType.LEASE_DEED -> "Lease Deed"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedDocType == type) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            modifier = Modifier.testTag("doc_type_tab_${type.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Template description card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LegalSurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LegalBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = selectedDocType.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LegalDeepBlue
                            )
                        )
                        Text(
                            text = selectedDocType.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = LegalTextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Dynamic Input Fields for selected template
                when (selectedDocType) {
                    DocumentType.NDA -> {
                        LegalInputSectionTitle("1. Disclosing Party (Company/Individual)")
                        OutlinedTextField(
                            value = ndaDisclosingName,
                            onValueChange = { ndaDisclosingName = it },
                            label = { Text("Disclosing Party Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = ndaDisclosingAddress,
                            onValueChange = { ndaDisclosingAddress = it },
                            label = { Text("Registered Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("2. Receiving Party (Contractor/Partner)")
                        OutlinedTextField(
                            value = ndaReceivingName,
                            onValueChange = { ndaReceivingName = it },
                            label = { Text("Receiving Party Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = ndaReceivingAddress,
                            onValueChange = { ndaReceivingAddress = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("3. Project & Confidential Information Scope")
                        OutlinedTextField(
                            value = ndaPurpose,
                            onValueChange = { ndaPurpose = it },
                            label = { Text("Permitted Evaluation Purpose") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = ndaBusiness,
                            onValueChange = { ndaBusiness = it },
                            label = { Text("Nature of Proprietary Information") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = ndaCity,
                            onValueChange = { ndaCity = it },
                            label = { Text("Jurisdiction Court City") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                    }

                    DocumentType.AFFIDAVIT -> {
                        LegalInputSectionTitle("1. Deponent Details")
                        OutlinedTextField(
                            value = affDeponentName,
                            onValueChange = { affDeponentName = it },
                            label = { Text("Full Name of Deponent") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = affAge,
                                onValueChange = { affAge = it },
                                label = { Text("Age (Years)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = affRelation,
                                onValueChange = { affRelation = it },
                                label = { Text("Father/Spouse Name") },
                                modifier = Modifier.weight(2f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = affAddress,
                            onValueChange = { affAddress = it },
                            label = { Text("Permanent Residence Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("2. Purpose of Affidavit")
                        OutlinedTextField(
                            value = affPurpose,
                            onValueChange = { affPurpose = it },
                            label = { Text("Solemn Declaration Purpose") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("3. Notary & Verification")
                        OutlinedTextField(
                            value = affIdentifier,
                            onValueChange = { affIdentifier = it },
                            label = { Text("Advocate / Identifier") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = affNotary,
                            onValueChange = { affNotary = it },
                            label = { Text("Notary Public Name & Reg No.") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                    }

                    DocumentType.RENT_AGREEMENT -> {
                        LegalInputSectionTitle("1. Landlord Details")
                        OutlinedTextField(
                            value = rentLandlordName,
                            onValueChange = { rentLandlordName = it },
                            label = { Text("Landlord Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = rentLandlordAddress,
                            onValueChange = { rentLandlordAddress = it },
                            label = { Text("Landlord Address & Phone") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("2. Tenant Details")
                        OutlinedTextField(
                            value = rentTenantName,
                            onValueChange = { rentTenantName = it },
                            label = { Text("Tenant Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = rentTenantAddress,
                            onValueChange = { rentTenantAddress = it },
                            label = { Text("Tenant Address & Phone") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("3. Rental Terms & Financials")
                        OutlinedTextField(
                            value = rentPropertyAddress,
                            onValueChange = { rentPropertyAddress = it },
                            label = { Text("Residential Premises Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = rentMonthlyAmount,
                                onValueChange = { rentMonthlyAmount = it },
                                label = { Text("Monthly Rent (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = rentDeposit,
                                onValueChange = { rentDeposit = it },
                                label = { Text("Deposit (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    DocumentType.LEASE_DEED -> {
                        LegalInputSectionTitle("1. Lessor (Property Owner / Entity)")
                        OutlinedTextField(
                            value = leaseLessorName,
                            onValueChange = { leaseLessorName = it },
                            label = { Text("Lessor Company / Entity Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = leaseLessorAddress,
                            onValueChange = { leaseLessorAddress = it },
                            label = { Text("Lessor Registered Office") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("2. Lessee (Commercial Tenant)")
                        OutlinedTextField(
                            value = leaseLesseeName,
                            onValueChange = { leaseLesseeName = it },
                            label = { Text("Lessee Company / Entity Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = leaseLesseeAddress,
                            onValueChange = { leaseLesseeAddress = it },
                            label = { Text("Lessee Registered Office") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("3. Commercial Premises & Commercial Terms")
                        OutlinedTextField(
                            value = leasePropertyDesc,
                            onValueChange = { leasePropertyDesc = it },
                            label = { Text("Premises Description & Super Built-up Area") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = leasePropertyAddress,
                            onValueChange = { leasePropertyAddress = it },
                            label = { Text("Complete Commercial Property Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = leaseMonthlyRent,
                                onValueChange = { leaseMonthlyRent = it },
                                label = { Text("Monthly Rent (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = leaseDeposit,
                                onValueChange = { leaseDeposit = it },
                                label = { Text("Interest-Free Deposit (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = leaseDurationYears,
                                onValueChange = { leaseDurationYears = it },
                                label = { Text("Term (Years)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = leaseEscalation,
                                onValueChange = { leaseEscalation = it },
                                label = { Text("Annual Escalation (%)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    DocumentType.SALE_DEED -> {
                        LegalInputSectionTitle("1. Vendor (Seller)")
                        OutlinedTextField(
                            value = saleSellerName,
                            onValueChange = { saleSellerName = it },
                            label = { Text("Seller Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = saleSellerAddress,
                            onValueChange = { saleSellerAddress = it },
                            label = { Text("Seller Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("2. Vendee (Buyer)")
                        OutlinedTextField(
                            value = saleBuyerName,
                            onValueChange = { saleBuyerName = it },
                            label = { Text("Buyer Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = saleBuyerAddress,
                            onValueChange = { saleBuyerAddress = it },
                            label = { Text("Buyer Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        LegalInputSectionTitle("3. Property Schedule & Consideration")
                        OutlinedTextField(
                            value = salePropertyAddress,
                            onValueChange = { salePropertyAddress = it },
                            label = { Text("Property Address & Survey No.") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = saleAmount,
                            onValueChange = { saleAmount = it },
                            label = { Text("Total Consideration Amount (₹)") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Generate Button
                Button(
                    onClick = { generateCurrentDraft() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LegalDeepBlue,
                        contentColor = LegalGoldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("generate_draft_button")
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Print-Ready Contract",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Formatted Contract Preview
                if (generatedDraft != null) {
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
                                    text = "📄 Generated Legal Document",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LegalDeepBlue
                                    )
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Legal Contract", generatedDraft)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Contract copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = LegalDeepBlue)
                                    }

                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, selectedDocType.title)
                                                putExtra(Intent.EXTRA_TEXT, generatedDraft)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Contract"))
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = LegalDeepBlue)
                                    }

                                    IconButton(
                                        onClick = {
                                            val newDoc = SavedDocument(
                                                type = selectedDocType,
                                                title = "${selectedDocType.title} (${System.currentTimeMillis() % 10000})",
                                                content = generatedDraft!!,
                                                summary = "Drafted on ${DocumentTemplates.generateNda("", "", "", "", "", "", 1, "").take(20)}"
                                            )
                                            scope.launch {
                                                repository.saveDocument(newDoc)
                                                Toast.makeText(context, "Saved document to device database", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
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
                                    text = generatedDraft!!,
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

@Composable
fun LegalInputSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = LegalDeepBlue
        ),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShieldAlert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanRecord
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleRed
import com.example.ui.theme.adaptive

data class CapturedEvidenceItem(
    val id: String,
    val title: String,
    val category: String,
    val timestamp: String,
    val threatScore: Int, // 0 to 100
    val extractedSnippet: String,
    val locationTag: String,
    val firestoreDocId: String,
    val typeIcon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceGalleryScreen(
    scanRecords: List<ScanRecord> = emptyList(),
    onSelectEvidenceItem: (CapturedEvidenceItem) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedEvidenceForDetail by remember { mutableStateOf<CapturedEvidenceItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Demo captured crime evidence items synced from Firestore
    val defaultEvidenceItems = remember {
        listOf(
            CapturedEvidenceItem(
                id = "ev_101",
                title = "Fake SBI Netbanking SMS",
                category = "Bank Scam",
                timestamp = "Sep 29, 2026, 17:42",
                threatScore = 94,
                extractedSnippet = "ALERT: Netbanking blocked. Update KYC at http://sbi-net-kyc.top immediately.",
                locationTag = "Mumbai, MH • 19.0760, 72.8777",
                firestoreDocId = "fs_doc_9481a",
                typeIcon = "📲"
            ),
            CapturedEvidenceItem(
                id = "ev_102",
                title = "Fake Police Digital Arrest",
                category = "Digital Arrest",
                timestamp = "Sep 29, 2026, 14:15",
                threatScore = 98,
                extractedSnippet = "Mumbai Crime Branch: Seized illegal parcel under your Aadhaar. Pay ₹50,000 fine via UPI.",
                locationTag = "Delhi NCR • 28.6139, 77.2090",
                firestoreDocId = "fs_doc_8829b",
                typeIcon = "⚖️"
            ),
            CapturedEvidenceItem(
                id = "ev_103",
                title = "Paytm Refund QR Code Trap",
                category = "QR Trap",
                timestamp = "Sep 28, 2026, 09:30",
                threatScore = 88,
                extractedSnippet = "upi://pay?pa=refund_desk@upi&am=2500 - Scan to receive refund. Enter UPI PIN to confirm.",
                locationTag = "Bengaluru, KA • 12.9716, 77.5946",
                firestoreDocId = "fs_doc_7311c",
                typeIcon = "📷"
            ),
            CapturedEvidenceItem(
                id = "ev_104",
                title = "WhatsApp Video Like Job",
                category = "Job Scam",
                timestamp = "Sep 27, 2026, 18:20",
                threatScore = 76,
                extractedSnippet = "Part-time job offer: Earn ₹3,000 daily by liking YouTube videos. Telegram @parttime_hr",
                locationTag = "Hyderabad, TS • 17.3850, 78.4867",
                firestoreDocId = "fs_doc_6190d",
                typeIcon = "💼"
            ),
            CapturedEvidenceItem(
                id = "ev_105",
                title = "Electricity Bill Disconnect Notice",
                category = "Utility Fraud",
                timestamp = "Sep 26, 2026, 21:05",
                threatScore = 91,
                extractedSnippet = "Power connection will be disconnected tonight at 9:30 PM due to unpaid bill. Call 98421XXXXX.",
                locationTag = "Chennai, TN • 13.0827, 80.2707",
                firestoreDocId = "fs_doc_5044e",
                typeIcon = "⚡"
            ),
            CapturedEvidenceItem(
                id = "ev_106",
                title = "Official Govt Helpline Advisory",
                category = "Verified Safe",
                timestamp = "Sep 25, 2026, 11:00",
                threatScore = 12,
                extractedSnippet = "National Cyber Crime Reporting Portal (NCRP) helpline number is 1930. Official portal cybercrime.gov.in",
                locationTag = "New Delhi • 28.6100, 77.2300",
                firestoreDocId = "fs_doc_1020f",
                typeIcon = "🛡️"
            )
        )
    }

    val filteredItems = remember(searchQuery, selectedCategoryFilter) {
        defaultEvidenceItems.filter { item ->
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                    item.extractedSnippet.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "ALL" -> true
                "CRITICAL" -> item.threatScore >= 85
                "BANK" -> item.category == "Bank Scam"
                "QR" -> item.category == "QR Trap"
                "ARREST" -> item.category == "Digital Arrest"
                else -> true
            }
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Text(
            text = "Evidence Gallery",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                letterSpacing = (-0.5).sp
            ).adaptive(),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Captured crime screenshots & payloads synced from Firestore",
            style = MaterialTheme.typography.bodyMedium.adaptive(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search evidence, text or category...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "ALL" to "All Evidence",
                "CRITICAL" to "🚨 Critical Threat",
                "BANK" to "🏦 Bank Scam",
                "QR" to "📱 QR Trap"
            )

            filters.forEach { (code, label) ->
                FilterChip(
                    selected = selectedCategoryFilter == code,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCategoryFilter = code
                    },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppleBlue.copy(alpha = 0.2f),
                        selectedLabelColor = AppleBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gallery Grid View
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(filteredItems) { item ->
                CapturedEvidenceCard(
                    item = item,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedEvidenceForDetail = item
                        onSelectEvidenceItem(item)
                    }
                )
            }
        }
    }

    // Detail Lightbox Modal Sheet
    selectedEvidenceForDetail?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { selectedEvidenceForDetail = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.typeIcon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { selectedEvidenceForDetail = null }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Threat Banner
                val bannerColor = if (item.threatScore >= 80) AppleRed else if (item.threatScore >= 50) Color(0xFFFF9500) else AppleGreen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bannerColor.copy(alpha = 0.15f))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.threatScore}% AI Threat Score",
                            fontWeight = FontWeight.Bold,
                            color = bannerColor
                        )
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = bannerColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Extracted OCR Payload Box
                Text(
                    text = "EXTRACTED FORENSIC CONTENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = item.extractedSnippet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Firestore Doc ID", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.firestoreDocId, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Timestamp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.timestamp, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Location", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.locationTag, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { selectedEvidenceForDetail = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
                ) {
                    Text("Export Cybercrime 1930 Dossier", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CapturedEvidenceCard(
    item: CapturedEvidenceItem,
    onClick: () -> Unit
) {
    val badgeColor = if (item.threatScore >= 80) AppleRed else if (item.threatScore >= 50) Color(0xFFFF9500) else AppleGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Simulated Thumbnail Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.typeIcon, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.threatScore}% Threat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.timestamp,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.extractedSnippet,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

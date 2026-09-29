package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleRed

data class SimulatorScenario(
    val id: String,
    val title: String,
    val category: String,
    val incomingMessage: String,
    val options: List<SimulatorOption>
)

data class SimulatorOption(
    val label: String,
    val isDangerous: Boolean,
    val aiResponseTitle: String,
    val aiExplanation: String,
    val riskScore: Int,
    val redFlags: List<String>
)

@Composable
fun ScamSimulatorDialog(
    onDismiss: () -> Unit,
    onTriggerEmergency: () -> Unit
) {
    val scenarios = remember {
        listOf(
            SimulatorScenario(
                id = "sc_1",
                title = "1. Digital Arrest / Fake Police Call",
                category = "Fake Police & Customs Scam",
                incomingMessage = "📲 INCOMING VIDEO CALL: 'Inspector Sharma - Mumbai Crime Branch'\n\n\"Your Aadhaar is linked to a seized parcel containing illegal narcotics. Pay ₹50,000 clearance fine via UPI or police officers will arrest you in 30 minutes!\"",
                options = listOf(
                    SimulatorOption(
                        label = "Pay ₹50,000 via UPI immediately",
                        isDangerous = true,
                        aiResponseTitle = "🚨 CRITICAL SCAM INTERCEPT DETECTED!",
                        aiExplanation = "DO NOT TRANSFER MONEY! Real police officers, CBI, and Customs NEVER conduct arrests over video call, nor do they accept UPI bail payments.",
                        riskScore = 96,
                        redFlags = listOf(
                            "Severe fear tactic and 30-minute artificial time pressure",
                            "Police cannot conduct 'digital arrests' over video calls",
                            "Requests money via personal/unverified UPI handle"
                        )
                    ),
                    SimulatorOption(
                        label = "Run CrimeLens AI Forensic Check",
                        isDangerous = false,
                        aiResponseTitle = "🛡️ CrimeLens AI Forensic Shield Active",
                        aiExplanation = "CrimeLens AI detected high psychological pressure and fake authority impersonation. Recommended action: Disconnect immediately and call 1930.",
                        riskScore = 96,
                        redFlags = listOf(
                            "Fake Police Impersonation detected in voice transcript",
                            "Illegal demand for UPI transfer under threat of arrest",
                            "Urgency trigger designed to bypass logical verification"
                        )
                    )
                )
            ),
            SimulatorScenario(
                id = "sc_2",
                title = "2. UPI QR Code 'Receive Money' Trap",
                category = "UPI Refund & Merchant Fraud",
                incomingMessage = "💬 WhatsApp Message from unknown seller:\n\n\"I am sending you ₹2,000 refund for your purchase. Please scan this QR code and enter your 6-digit UPI PIN to claim the money into your bank account.\"",
                options = listOf(
                    SimulatorOption(
                        label = "Scan QR Code & Enter UPI PIN",
                        isDangerous = true,
                        aiResponseTitle = "🚨 CRITICAL UPI REFUND SCAM TRAP!",
                        aiExplanation = "STOP! Entering your UPI PIN ALWAYS DEDUCTS money from your bank account! You NEVER enter a PIN to receive money.",
                        riskScore = 92,
                        redFlags = listOf(
                            "False claim that PIN entry receives money",
                            "QR payload directs debit transfer to unknown VPA handle",
                            "Scammer exploiting lack of awareness on UPI architecture"
                        )
                    ),
                    SimulatorOption(
                        label = "Verify QR Payload with CrimeLens AI",
                        isDangerous = false,
                        aiResponseTitle = "🛡️ CrimeLens AI Shield Intercepted QR",
                        aiExplanation = "QR code contains a payment request header payload pointing to merchant VPA 'refund-merchant-claim@okaxis.top'. No PIN required to receive money.",
                        riskScore = 92,
                        redFlags = listOf(
                            "Payload formatted as DEBIT request, not credit",
                            "Misleading domain extension (.top)",
                            "Merchant name spoofs official bank entity"
                        )
                    )
                )
            ),
            SimulatorScenario(
                id = "sc_3",
                title = "3. Bank KYC Account Suspension SMS",
                category = "Bank Impersonation & Phishing",
                incomingMessage = "📩 SMS from 'QP-SBI-KYC':\n\n\"Dear SBI User, your Netbanking access has been SUSPENDED today due to missing PAN update. Click http://sbi-net-kyc.top/verify immediately to avoid permanent block.\"",
                options = listOf(
                    SimulatorOption(
                        label = "Click link & enter login credentials",
                        isDangerous = true,
                        aiResponseTitle = "🚨 PHISHING & CREDENTIAL HARVESTING LINK!",
                        aiExplanation = "DO NOT CLICK! This domain is a fake clone designed to steal your SBI netbanking username, password, and OTP.",
                        riskScore = 94,
                        redFlags = listOf(
                            "Domain mismatch: 'sbi-net-kyc.top' is NOT official 'sbi.co.in'",
                            "Creates artificial panic about account block",
                            "Unsecured HTTP URL structure"
                        )
                    ),
                    SimulatorOption(
                        label = "Check URL with CrimeLens AI",
                        isDangerous = false,
                        aiResponseTitle = "🛡️ Domain Typosquatting Analysis Result",
                        aiExplanation = "CrimeLens AI verified domain WHOIS data registered 2 days ago. This is a credential harvesting phishing portal.",
                        riskScore = 94,
                        redFlags = listOf(
                            "Domain registered anonymously in offshore registry",
                            "Visual clone of SBI login page detected",
                            "Steals 6-digit OTP in real-time"
                        )
                    )
                )
            )
        )
    }

    var activeScenarioIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<SimulatorOption?>(null) }

    val activeScenario = scenarios[activeScenarioIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Scam Simulator",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Interactive Presentation Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Scenario selector tabs
                item {
                    Text(
                        text = "SELECT DEMO SCENARIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        scenarios.forEachIndexed { idx, sc ->
                            val isSelected = idx == activeScenarioIndex
                            Card(
                                onClick = {
                                    activeScenarioIndex = idx
                                    selectedOption = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Demo ${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Incoming Scenario Simulation Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = activeScenario.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeScenario.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = activeScenario.incomingMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Interactive Decision Buttons
                item {
                    Text(
                        text = "TEST USER ACTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeScenario.options.forEach { opt ->
                            val isChosen = selectedOption == opt
                            OutlinedButton(
                                onClick = { selectedOption = opt },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sim_opt_${opt.riskScore}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isChosen) (if (opt.isDangerous) AppleRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer) else Color.Transparent
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (opt.isDangerous) Icons.Outlined.Warning else Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = if (opt.isDangerous) AppleRed else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opt.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (opt.isDangerous) AppleRed else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Intercept Analysis View
                if (selectedOption != null) {
                    item {
                        val opt = selectedOption!!
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (opt.isDangerous) AppleRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (opt.isDangerous) AppleRed else MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = opt.aiResponseTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (opt.isDangerous) AppleRed else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Risk: ${opt.riskScore}%",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = AppleRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = opt.aiExplanation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "RED FLAGS DETECTED BY CRIMELENS AI:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                opt.redFlags.forEach { flag ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Warning,
                                            contentDescription = null,
                                            tint = AppleRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = flag,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (opt.isDangerous) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onTriggerEmergency()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Open Emergency Helpline 1930", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close Simulator", color = Color.White)
            }
        }
    )
}

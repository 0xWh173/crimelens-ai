package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleRed
import com.example.ui.theme.adaptive
import com.example.util.HapticFeedbackStyle
import com.example.util.rememberHapticFeedbackHelper

data class BankHelpline(
    val bankName: String,
    val phoneNumber: String,
    val website: String
)

@Composable
fun EmergencyModeScreen() {
    val context = LocalContext.current
    val hapticHelper = rememberHapticFeedbackHelper()

    val bankHelplines = listOf(
        BankHelpline("National Cybercrime Helpline", "1930", "https://cybercrime.gov.in"),
        BankHelpline("State Bank of India (SBI)", "18001234", "https://sbi.co.in"),
        BankHelpline("HDFC Bank Cyber Security", "18002026161", "https://hdfcbank.com"),
        BankHelpline("ICICI Bank Fraud Toll-Free", "18002662", "https://icicibank.com"),
        BankHelpline("Axis Bank Emergency Fraud", "18004195959", "https://axisbank.com"),
        BankHelpline("Paytm Fraud Helpline", "01203888388", "https://paytm.com")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Emergency",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        letterSpacing = (-0.5).sp
                    ).adaptive(),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Helplines and immediate protection protocol",
                    style = MaterialTheme.typography.bodyLarge.adaptive(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dial 1930 Call Action
        item {
            Button(
                onClick = {
                    hapticHelper.trigger(HapticFeedbackStyle.ALERT_WARNING)
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("dial_1930_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Outlined.PhoneInTalk, contentDescription = "Call 1930", tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Call National Helpline 1930",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                    color = Color.White
                )
            }
        }

        // 4 Critical Emergency Steps
        item {
            Column {
                Text(
                    text = "Protection Steps",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ).adaptive(),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        EmergencyStepRow("1. Do Not Enter UPI PIN", "Entering PIN deducts money. No PIN is needed to receive funds.")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        EmergencyStepRow("2. Do Not Share OTP", "Bank employees and law enforcement never ask for OTPs.")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        EmergencyStepRow("3. Disconnect Video Calls", "Police and Customs do not conduct arrests via video call.")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        EmergencyStepRow("4. Freeze Bank Accounts", "Contact your bank toll-free hotline to block card and UPI access.")
                    }
                }
            }
        }

        // Bank Toll-Free Helplines
        item {
            Column {
                Text(
                    text = "Bank Helplines",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ).adaptive(),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        bankHelplines.forEachIndexed { index, bank ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bank.bankName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = bank.phoneNumber,
                                        style = MaterialTheme.typography.bodyMedium.adaptive(),
                                        color = AppleBlue
                                    )
                                }

                                Button(
                                    onClick = {
                                        hapticHelper.trigger(HapticFeedbackStyle.KEYBOARD_PRESS)
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${bank.phoneNumber}"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.Call, contentDescription = "Dial", tint = AppleBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call", color = AppleBlue, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold).adaptive())
                                }
                            }
                            if (index < bankHelplines.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyStepRow(title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Cancel,
            contentDescription = null,
            tint = AppleRed,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium.adaptive(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



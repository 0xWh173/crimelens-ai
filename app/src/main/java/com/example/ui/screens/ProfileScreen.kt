package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalysisLog
import com.example.data.model.AuthUser
import com.example.data.model.EvidenceItem
import com.example.ui.components.EvidenceVaultDialog
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleRed
import com.example.ui.theme.adaptive

@Composable
fun ProfileScreen(
    userPoints: Int,
    scanCount: Int,
    evidenceList: List<EvidenceItem> = emptyList(),
    analysisLogs: List<AnalysisLog> = emptyList(),
    authUser: AuthUser = AuthUser(),
    onOpenAuth: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteEvidence: (Long) -> Unit = {},
    onDeleteLog: (Long) -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateEmergency: () -> Unit = {},
    onOpenSimulator: () -> Unit = {}
) {
    var realTimeProtection by remember { mutableStateOf(true) }
    var smartAlerts by remember { mutableStateOf(true) }
    var showVaultDialog by remember { mutableStateOf(false) }

    if (showVaultDialog) {
        EvidenceVaultDialog(
            evidenceList = evidenceList,
            analysisLogs = analysisLogs,
            onDeleteEvidence = onDeleteEvidence,
            onDeleteLog = onDeleteLog,
            onDismiss = { showVaultDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Hero Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            ).adaptive(),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Real-time Profile & Auth Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (authUser.isAuthenticated) AppleGreen.copy(alpha = 0.15f)
                                else AppleBlue.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (authUser.isAuthenticated) {
                            Text(
                                text = (authUser.displayName?.firstOrNull() ?: authUser.email?.firstOrNull() ?: 'I').uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                                color = AppleGreen
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "User",
                                tint = AppleBlue,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (authUser.isAuthenticated) authUser.displayLabel else "Guest Investigator",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ).adaptive(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (authUser.isAuthenticated) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = AppleGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (authUser.isAuthenticated) "Badge: ${authUser.badgeTitle}" else "Status: Local Guest Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold).adaptive(),
                            color = if (authUser.isAuthenticated) AppleGreen else AppleBlue
                        )
                        Text(
                            text = "$scanCount Evidence Checks Completed • $userPoints XP",
                            style = MaterialTheme.typography.bodySmall.adaptive(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                if (!authUser.isAuthenticated) {
                    Text(
                        text = "Sign in to back up your forensic evidence, unlock verified community reporting, and earn detective achievements.",
                        style = MaterialTheme.typography.bodySmall.adaptive(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenAuth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .testTag("profile_login_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign In / Create Account",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold).adaptive(),
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = authUser.email ?: "Real-time Authenticated",
                                style = MaterialTheme.typography.bodySmall.adaptive(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Real-time Firebase Sync Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium).adaptive(),
                                color = AppleGreen
                            )
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("profile_sign_out_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = AppleRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.labelMedium.adaptive(),
                                color = AppleRed
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: PREFERENCES
        Text(
            text = "PREFERENCES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ).adaptive(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                AppleSettingsToggleRow(
                    icon = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    title = "Appearance",
                    subtitle = if (isDarkTheme) "Dark Theme" else "Light Theme",
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    modifier = Modifier.testTag("dark_mode_switch")
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AppleSettingsClickableRow(
                    icon = Icons.Outlined.PlayCircle,
                    title = "Scam Simulator",
                    subtitle = "Interactive scam call & fake SMS drill",
                    onClick = onOpenSimulator
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: NOTIFICATIONS
        Text(
            text = "NOTIFICATIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ).adaptive(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                AppleSettingsToggleRow(
                    icon = Icons.Outlined.Shield,
                    title = "Real-Time Protection",
                    subtitle = "Instant warnings on suspicious clipboard links",
                    checked = realTimeProtection,
                    onCheckedChange = { realTimeProtection = it }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AppleSettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Threat Alerts",
                    subtitle = "Push notifications for local scam outbreaks",
                    checked = smartAlerts,
                    onCheckedChange = { smartAlerts = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: PRIVACY
        Text(
            text = "PRIVACY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ).adaptive(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                AppleSettingsClickableRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "On-Device Forensics",
                    subtitle = "OCR and evidence text analyzed with strict privacy",
                    onClick = {}
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AppleSettingsClickableRow(
                    icon = Icons.Outlined.Phone,
                    title = "Emergency Helpline 1930",
                    subtitle = "National cyber financial fraud protocol",
                    onClick = onNavigateEmergency
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: LOCAL ROOM FORENSIC VAULT
        Text(
            text = "LOCAL ROOM FORENSIC VAULT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ).adaptive(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                AppleSettingsClickableRow(
                    icon = Icons.Outlined.Shield,
                    title = "Evidence Locker (${evidenceList.size})",
                    subtitle = if (evidenceList.isEmpty()) "No local evidence items recorded yet" else "${evidenceList.size} items cryptographically hashed (SHA-256)",
                    onClick = { showVaultDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                AppleSettingsClickableRow(
                    icon = Icons.Outlined.Info,
                    title = "Forensic Analysis Logs (${analysisLogs.size})",
                    subtitle = if (analysisLogs.isEmpty()) "No logs in Room database" else "${analysisLogs.size} logs stored on-device with threat intelligence",
                    onClick = { showVaultDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: ABOUT
        Text(
            text = "ABOUT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ).adaptive(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                AppleSettingsClickableRow(
                    icon = Icons.Outlined.Info,
                    title = "CrimeLens AI",
                    subtitle = "Production Release v2.0 (Real-Time Backend Enabled)",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AppleSettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AppleBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.adaptive(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppleBlue,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun AppleSettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AppleBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.adaptive(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

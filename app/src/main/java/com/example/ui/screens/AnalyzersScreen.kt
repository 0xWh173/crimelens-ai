package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.EvidenceType
import com.example.data.model.ScamAnalysisResult
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleOrange
import com.example.ui.theme.AppleRed
import com.example.ui.theme.adaptive
import com.example.ui.viewmodel.AnalysisState
import com.example.util.HapticFeedbackStyle
import com.example.util.MLKitAnalyzer
import com.example.util.VoiceRecorderHelper
import com.example.util.rememberHapticFeedbackHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnalyzersScreen(
    analysisState: AnalysisState,
    onAnalyze: (EvidenceType, String, String?) -> Unit,
    onResetState: () -> Unit,
    onTriggerEmergencyMode: () -> Unit,
    onOpenSimulator: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val hapticHelper = rememberHapticFeedbackHelper()
    val evidenceTypes = listOf(
        EvidenceType.SCREENSHOT,
        EvidenceType.URL,
        EvidenceType.QR_CODE,
        EvidenceType.VOICE_AUDIO,
        EvidenceType.EMAIL
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // iOS Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            evidenceTypes.forEachIndexed { index, type ->
                val isSelected = selectedTabIndex == index
                val label = when (type) {
                    EvidenceType.SCREENSHOT -> "Photo"
                    EvidenceType.URL -> "Link"
                    EvidenceType.QR_CODE -> "QR"
                    EvidenceType.VOICE_AUDIO -> "Voice"
                    EvidenceType.EMAIL -> "Email"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable {
                            hapticHelper.trigger(HapticFeedbackStyle.LIGHT_TAP)
                            selectedTabIndex = index
                            onResetState()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp.adaptive()
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            when (analysisState) {
                is AnalysisState.Idle -> {
                    AnalyzerInputForm(
                        evidenceType = evidenceTypes[selectedTabIndex],
                        onAnalyze = onAnalyze
                    )
                }
                is AnalysisState.Analyzing -> {
                    AppleAnalysisLoadingView()
                }
                is AnalysisState.Success -> {
                    AnalysisResultView(
                        result = analysisState.result,
                        onReset = onResetState,
                        onTriggerEmergency = onTriggerEmergencyMode
                    )
                }
                is AnalysisState.Error -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Unable to Analyze",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold).adaptive(),
                                color = AppleRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = analysisState.message,
                                style = MaterialTheme.typography.bodyLarge.adaptive(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    hapticHelper.trigger(HapticFeedbackStyle.KEYBOARD_PRESS)
                                    onResetState()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Try Again", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyzerInputForm(
    evidenceType: EvidenceType,
    onAnalyze: (EvidenceType, String, String?) -> Unit
) {
    var inputText by remember(evidenceType) { mutableStateOf("") }
    var capturedBitmap by remember(evidenceType) { mutableStateOf<Bitmap?>(null) }
    var mlKitStatus by remember(evidenceType) { mutableStateOf<String?>(null) }
    var isProcessingMLKit by remember(evidenceType) { mutableStateOf(false) }

    val context = LocalContext.current
    val hapticHelper = rememberHapticFeedbackHelper()
    val coroutineScope = rememberCoroutineScope()

    val voiceHelper = remember(context) { VoiceRecorderHelper(context) }
    val isRecordingVoice by voiceHelper.isRecording.collectAsState()
    val voiceTranscript by voiceHelper.transcript.collectAsState()
    val voiceStatus by voiceHelper.statusMessage.collectAsState()

    LaunchedEffect(voiceTranscript) {
        if (voiceTranscript.isNotBlank() && evidenceType == EvidenceType.VOICE_AUDIO) {
            inputText = voiceTranscript
        }
    }

    // Google ML Kit OCR & Barcode processing
    val processBitmapWithMLKit: (Bitmap) -> Unit = { bitmap ->
        capturedBitmap = bitmap
        isProcessingMLKit = true
        mlKitStatus = "Analyzing with Google ML Kit..."
        hapticHelper.trigger(HapticFeedbackStyle.KEYBOARD_PRESS)

        coroutineScope.launch {
            try {
                val output = MLKitAnalyzer.analyzeBitmap(bitmap)
                isProcessingMLKit = false

                val builder = StringBuilder()
                if (output.detectedQrCodes.isNotEmpty()) {
                    builder.append("QR Code Payload: ${output.detectedQrCodes.joinToString("\n")}\n\n")
                }
                if (output.detectedUrls.isNotEmpty()) {
                    builder.append("Extracted Links: ${output.detectedUrls.joinToString(", ")}\n\n")
                }
                if (output.extractedText.isNotBlank()) {
                    builder.append(output.extractedText)
                }

                val finalOutput = builder.toString().trim()
                if (finalOutput.isNotBlank()) {
                    hapticHelper.trigger(HapticFeedbackStyle.CONFIRM_SUCCESS)
                    inputText = finalOutput
                    mlKitStatus = "ML Kit: Extracted ${output.rawResultCount} evidence element(s)"
                } else {
                    hapticHelper.trigger(HapticFeedbackStyle.ALERT_WARNING)
                    mlKitStatus = "No text or QR code detected in the image. Try taking a clearer photo or paste text below."
                }
            } catch (e: Exception) {
                isProcessingMLKit = false
                mlKitStatus = "Image analysis error: ${e.localizedMessage}"
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processBitmapWithMLKit(bitmap)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                processBitmapWithMLKit(bitmap)
            } catch (e: Exception) {
                mlKitStatus = "Failed to load image from gallery: ${e.localizedMessage}"
            }
        }
    }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            hapticHelper.trigger(HapticFeedbackStyle.CONFIRM_SUCCESS)
            cameraLauncher.launch(null)
        } else {
            hapticHelper.trigger(HapticFeedbackStyle.REJECT_ERROR)
            mlKitStatus = "Camera permission denied."
        }
    }

    // Audio permission
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            hapticHelper.trigger(HapticFeedbackStyle.CONFIRM_SUCCESS)
            voiceHelper.startListening()
        } else {
            hapticHelper.trigger(HapticFeedbackStyle.REJECT_ERROR)
            mlKitStatus = "Microphone permission denied."
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        // Photo / QR / Voice Action Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Photo",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = AppleBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isProcessingMLKit) "ML Kit Extracting..." else "Google ML Kit Processed",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold).adaptive(),
                                color = if (isProcessingMLKit) AppleOrange else AppleGreen
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AppleBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (evidenceType) {
                                    EvidenceType.SCREENSHOT -> Icons.Outlined.PhotoCamera
                                    EvidenceType.URL -> Icons.Outlined.Link
                                    EvidenceType.QR_CODE -> Icons.Outlined.QrCodeScanner
                                    EvidenceType.VOICE_AUDIO -> Icons.Outlined.Mic
                                    EvidenceType.EMAIL -> Icons.Outlined.Email
                                },
                                contentDescription = "Select",
                                tint = AppleBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when (evidenceType) {
                                EvidenceType.SCREENSHOT -> "Scan Screenshot or Document"
                                EvidenceType.URL -> "Analyze Web Link / Domain"
                                EvidenceType.QR_CODE -> "Scan QR Code or Payment Payload"
                                EvidenceType.VOICE_AUDIO -> "Record Audio / Voice Call Transcript"
                                EvidenceType.EMAIL -> "Analyze Email Headers & Content"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Real-time Google ML Kit OCR & Threat Analysis",
                            style = MaterialTheme.typography.bodyMedium.adaptive(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (mlKitStatus != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleBlue.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mlKitStatus!!,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium).adaptive(),
                                color = AppleBlue,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (evidenceType == EvidenceType.VOICE_AUDIO) {
                        // Voice recorder controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    hapticHelper.trigger(HapticFeedbackStyle.KEYBOARD_PRESS)
                                    val hasMicPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasMicPermission) {
                                        if (isRecordingVoice) {
                                            voiceHelper.stopListening()
                                        } else {
                                            voiceHelper.startListening()
                                        }
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecordingVoice) AppleRed else AppleBlue
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecordingVoice) Icons.Outlined.Stop else Icons.Outlined.Mic,
                                    contentDescription = "Record",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRecordingVoice) "Stop Recording" else "Record Audio",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                                    color = Color.White
                                )
                            }
                        }

                        if (voiceStatus.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = voiceStatus,
                                style = MaterialTheme.typography.labelSmall.adaptive(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                    } else if (evidenceType == EvidenceType.SCREENSHOT || evidenceType == EvidenceType.QR_CODE) {
                        // Camera & Gallery action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    hapticHelper.trigger(HapticFeedbackStyle.KEYBOARD_PRESS)
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("camera_scan_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = AppleBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Camera",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                                    color = AppleBlue
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    hapticHelper.trigger(HapticFeedbackStyle.KEYBOARD_PRESS)
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("gallery_picker_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Collections,
                                    contentDescription = "Gallery",
                                    tint = AppleBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Gallery",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                                    color = AppleBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Evidence Text Input Area
        item {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 220.dp)
                    .testTag("evidence_input_field"),
                placeholder = {
                    Text(
                        text = when (evidenceType) {
                            EvidenceType.SCREENSHOT -> "OCR extracted text or paste message content..."
                            EvidenceType.URL -> "Enter website link (e.g. https://example.com)..."
                            EvidenceType.QR_CODE -> "Scanned QR decoded payload or UPI string..."
                            EvidenceType.VOICE_AUDIO -> "Recorded speech transcript or caller dialogue..."
                            EvidenceType.EMAIL -> "Paste email body, header, or sender address..."
                        },
                        style = MaterialTheme.typography.bodyMedium.adaptive(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Primary Action: Analyze Evidence
        item {
            Button(
                onClick = {
                    hapticHelper.trigger(HapticFeedbackStyle.LONG_PRESS)
                    if (inputText.isNotBlank()) {
                        onAnalyze(evidenceType, inputText, null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("run_ai_detective_btn"),
                enabled = inputText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Analyze",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyze Evidence",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AppleAnalysisLoadingView() {
    val loadingMessages = listOf(
        "Inspecting digital payload...",
        "Checking URL & domain reputation...",
        "Analyzing psychological manipulation...",
        "Evaluating threat indicators..."
    )
    var currentMessageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1200)
            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppleBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = AppleBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = loadingMessages[currentMessageIndex],
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ).adaptive(),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnalysisResultView(
    result: ScamAnalysisResult,
    onReset: () -> Unit,
    onTriggerEmergency: () -> Unit
) {
    val isScam = result.riskScore >= 60
    val titleText = if (isScam) "Potential Scam" else "Low Scam Risk"
    val scoreColor = if (isScam) AppleRed else AppleGreen

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        // Hero Result Typography
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ).adaptive(),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${result.riskScore}% Scam Threat",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ).adaptive(),
                    color = scoreColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.primarySummary,
                    style = MaterialTheme.typography.bodyLarge.adaptive(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // 4 Distinct Gemini Forensic Sections (Requirement 13)
        if (result.observedEvidence.isNotBlank() || result.inference.isNotBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (result.observedEvidence.isNotBlank()) {
                            ForensicDetailSection("Observed Evidence", result.observedEvidence, AppleBlue)
                        }
                        if (result.inference.isNotBlank()) {
                            ForensicDetailSection("Inference", result.inference, scoreColor)
                        }
                        if (result.externalIntelligence.isNotBlank()) {
                            ForensicDetailSection("External Intelligence", result.externalIntelligence, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (result.recommendation.isNotBlank()) {
                            ForensicDetailSection("Recommendation", result.recommendation, AppleGreen)
                        }
                    }
                }
            }
        }

        // Technical Details Map (e.g. decoded QR fields, URL protocol, TLD, IP host)
        if (result.technicalDetails.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Technical Anomaly Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            result.technicalDetails.entries.forEachIndexed { idx, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold).adaptive(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = entry.value,
                                        style = MaterialTheme.typography.bodyMedium.adaptive(),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (idx < result.technicalDetails.size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Red Flags
        if (result.redFlags.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Why we think this",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            result.redFlags.forEachIndexed { idx, flag ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = scoreColor,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Text(
                                        text = flag,
                                        style = MaterialTheme.typography.bodyLarge.adaptive(),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (idx < result.redFlags.size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recommended Actions
        if (result.recommendations.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "What should you do?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold).adaptive(),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            result.recommendations.forEachIndexed { idx, rec ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Action",
                                        tint = AppleBlue,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = rec,
                                        style = MaterialTheme.typography.bodyLarge.adaptive(),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (idx < result.recommendations.size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isScam) {
                    Button(
                        onClick = onTriggerEmergency,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Block & Report Scam",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold).adaptive(),
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = onReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Analyze Another Evidence",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold).adaptive(),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ForensicDetailSection(header: String, text: String, accentColor: Color) {
    Column {
        Text(
            text = header.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp).adaptive(),
            color = accentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.adaptive(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

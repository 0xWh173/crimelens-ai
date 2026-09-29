package com.example.util

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MLKitScanOutput(
    val extractedText: String,
    val detectedUrls: List<String>,
    val detectedQrCodes: List<String>,
    val detectedEmails: List<String>,
    val detectedPhones: List<String>,
    val rawResultCount: Int
)

object MLKitAnalyzer {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_EAN_13
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    /**
     * Process an image bitmap in real-time through Google ML Kit OCR and Barcode scanner
     */
    suspend fun analyzeBitmap(bitmap: Bitmap): MLKitScanOutput = withContext(Dispatchers.IO) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        var ocrText = ""
        val qrCodes = mutableListOf<String>()
        val urls = mutableListOf<String>()
        val emails = mutableListOf<String>()
        val phones = mutableListOf<String>()

        // 1. Run ML Kit Text Recognition
        try {
            val textTask = textRecognizer.process(inputImage)
            val result = Tasks.await(textTask)
            ocrText = result.text.trim()
        } catch (e: Exception) {
            ocrText = ""
        }

        // 2. Run ML Kit Barcode / QR Scanning
        try {
            val barcodeTask = barcodeScanner.process(inputImage)
            val barcodes = Tasks.await(barcodeTask)
            for (barcode in barcodes) {
                barcode.rawValue?.let { rawValue ->
                    qrCodes.add(rawValue)
                    if (rawValue.startsWith("http://") || rawValue.startsWith("https://")) {
                        urls.add(rawValue)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore barcode scanner errors
        }

        // 3. Extract URLs, Emails, Phone numbers using regex heuristics on OCR text
        val urlRegex = Regex("""(https?://[^\s]+)""", RegexOption.IGNORE_CASE)
        urlRegex.findAll(ocrText).forEach { match ->
            val u = match.value.trimEnd('.', ',', ';', ')')
            if (!urls.contains(u)) {
                urls.add(u)
            }
        }

        val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
        emailRegex.findAll(ocrText).forEach { match ->
            if (!emails.contains(match.value)) {
                emails.add(match.value)
            }
        }

        val phoneRegex = Regex("""(\+?\d{1,4}?[-.\s]?\(?\d{1,4}?\)?[-.\s]?\d{1,4}[-.\s]?\d{1,9})""")
        phoneRegex.findAll(ocrText).forEach { match ->
            val phone = match.value.trim()
            if (phone.length >= 10 && !phones.contains(phone)) {
                phones.add(phone)
            }
        }

        MLKitScanOutput(
            extractedText = ocrText,
            detectedUrls = urls,
            detectedQrCodes = qrCodes,
            detectedEmails = emails,
            detectedPhones = phones,
            rawResultCount = qrCodes.size + (if (ocrText.isNotBlank()) 1 else 0)
        )
    }
}

package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.EvidenceType
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamCategory
import com.example.data.model.ThreatIntelligenceRepository
import com.example.util.UrlAnalysisEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiRepository : ThreatIntelligenceRepository {

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    override suspend fun analyzeUrl(url: String): ScamAnalysisResult {
        return UrlAnalysisEngine.analyzeUrl(url)
    }

    override suspend fun analyzeEvidence(
        evidenceType: EvidenceType,
        content: String,
        base64Image: String?
    ): ScamAnalysisResult = withContext(Dispatchers.IO) {
        if (evidenceType == EvidenceType.URL) {
            return@withContext analyzeUrl(content)
        }

        val key = apiKey
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            // Fallback forensic engine for unconfigured or offline API key
            return@withContext analyzeWithRulesEngine(evidenceType, content)
        }

        try {
            val systemInstruction = """
                You are CrimeLens AI, an expert Cyber Crime Forensic AI.
                Analyze the input evidence (Screenshots, Texts, Voice transcripts, Emails, QR codes) for fraud/scam indicators.
                
                You MUST return ONLY a JSON object with this exact structure:
                {
                  "riskScore": 89,
                  "scamCategory": "BANK_IMPERSONATION",
                  "primarySummary": "High threat scam message impersonating bank to harvest credentials.",
                  "observedEvidence": "Contains statement 'SBI Netbanking blocked' and urgent 2-hour deadline.",
                  "inference": "This is a potential account-takeover credential harvesting attack.",
                  "externalIntelligence": "Domain reputation check returned no verified banking registration.",
                  "recommendation": "Do not share OTP or click links. Block contact.",
                  "redFlags": [
                    "Threatens account suspension within 2 hours",
                    "Impersonates bank with wrong domain link"
                  ],
                  "recommendations": [
                    "Do not click link or share OTP",
                    "Report on Cyber Crime helpline 1930"
                  ],
                  "extractedText": "Raw extracted text content"
                }

                Allowed scamCategory values:
                BANK_IMPERSONATION, UPI_FRAUD, JOB_OFFER, INVESTMENT_CRYPTO, LOTTERY_PRIZE,
                COURIER_PARCEL, OTP_HARVESTING, TECH_SUPPORT, ROMANCE, FAKE_POLICE, FAKE_KYC,
                GOVERNMENT_SCHEME, UNKNOWN
            """.trimIndent()

            val prompt = """
                Analyze evidence type: ${evidenceType.label}
                
                EVIDENCE CONTENT:
                $content
                
                Strictly separate Observed Evidence, Inference, External Intelligence, and Recommendation.
            """.trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            partsArray.put(JSONObject().put("text", prompt))

            if (!base64Image.isNullOrEmpty()) {
                partsArray.put(JSONObject().put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                }))
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val sysInstructionObj = JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            requestJson.put("systemInstruction", sysInstructionObj)

            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            }
            requestJson.put("generationConfig", generationConfig)

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 12000
            conn.doOutput = true

            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(requestJson.toString())
            wr.flush()

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val root = JSONObject(sb.toString())
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val cand = candidates.getJSONObject(0)
                    val contentRes = cand.optJSONObject("content")
                    val partsRes = contentRes?.optJSONArray("parts")
                    val responseText = partsRes?.optJSONObject(0)?.optString("text") ?: ""

                    return@withContext parseGeminiJsonResponse(responseText, content)
                }
            }
            return@withContext analyzeWithRulesEngine(evidenceType, content)

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext analyzeWithRulesEngine(evidenceType, content)
        }
    }

    private fun parseGeminiJsonResponse(jsonString: String, rawInput: String): ScamAnalysisResult {
        return try {
            val json = JSONObject(jsonString)
            val score = json.optInt("riskScore", 75)
            val catStr = json.optString("scamCategory", "BANK_IMPERSONATION")
            val summary = json.optString("primarySummary", "Detected suspicious fraud indicators.")
            val observed = json.optString("observedEvidence", "Evidence contains unverified financial or urgency requests.")
            val inference = json.optString("inference", "Potential fraudulent manipulation or account takeover attempt.")
            val extIntel = json.optString("externalIntelligence", "External Threat Lookup: No verified official registry found.")
            val rec = json.optString("recommendation", "Do not share sensitive credentials or enter UPI PIN.")

            val redFlagsArray = json.optJSONArray("redFlags")
            val redFlagsList = mutableListOf<String>()
            if (redFlagsArray != null) {
                for (i in 0 until redFlagsArray.length()) {
                    redFlagsList.add(redFlagsArray.getString(i))
                }
            }

            val recsArray = json.optJSONArray("recommendations")
            val recsList = mutableListOf<String>()
            if (recsArray != null) {
                for (i in 0 until recsArray.length()) {
                    recsList.add(recsArray.getString(i))
                }
            }

            val extracted = json.optString("extractedText", rawInput)
            val category = try { ScamCategory.valueOf(catStr) } catch (e: Exception) { ScamCategory.UNKNOWN }

            ScamAnalysisResult(
                riskScore = score,
                scamCategory = category,
                primarySummary = summary,
                observedEvidence = observed,
                inference = inference,
                externalIntelligence = extIntel,
                recommendation = rec,
                redFlags = if (redFlagsList.isEmpty()) listOf("High urgency pattern detected", "Unverified sender origin") else redFlagsList,
                recommendations = if (recsList.isEmpty()) listOf("Do not send money or share OTP", "Verify directly via official bank channel", "Block contact") else recsList,
                extractedContent = extracted
            )
        } catch (e: Exception) {
            analyzeWithRulesEngine(EvidenceType.SCREENSHOT, rawInput)
        }
    }

    fun analyzeWithRulesEngine(type: EvidenceType, text: String): ScamAnalysisResult {
        val lowerText = text.lowercase()
        var score = 15
        val flags = mutableListOf<String>()
        val recs = mutableListOf<String>()
        var category = ScamCategory.UNKNOWN
        var summary = "Initial security scan completed."

        val observedList = mutableListOf<String>()
        val inferenceList = mutableListOf<String>()

        if (lowerText.contains("sbi") || lowerText.contains("bank") || lowerText.contains("account suspended") || lowerText.contains("kyc")) {
            category = ScamCategory.BANK_IMPERSONATION
            score += 45
            flags.add("Threatens account suspension or block within tight timeframe")
            flags.add("Impersonates major financial institution (Bank/KYC)")
            observedList.add("Message references bank account suspension or immediate KYC requirement.")
            inferenceList.add("Classic credential harvesting tactic targeting netbanking users.")
        }

        if (lowerText.contains("upi") || lowerText.contains("paytm") || lowerText.contains("gpay") || lowerText.contains("pin") || lowerText.contains("receive money")) {
            if (category == ScamCategory.UNKNOWN) category = ScamCategory.UPI_FRAUD
            score += 35
            flags.add("Requests PIN entry to 'receive' money (classic UPI scam trap)")
            flags.add("Unverified VPA / UPI ID redirect")
            observedList.add("Prompts user to enter UPI PIN or scan QR code to receive funds.")
            inferenceList.add("Entering UPI PIN ALWAYS approves money deduction from your account, never a deposit.")
        }

        if (lowerText.contains("part-time") || lowerText.contains("telegram job") || lowerText.contains("like youtube videos") || lowerText.contains("daily earning")) {
            category = ScamCategory.JOB_OFFER
            score += 50
            flags.add("Offers unrealistic earnings for minimal task (Task/Job Scam)")
            flags.add("Directs communication to unverified Telegram/WhatsApp coordinator")
            observedList.add("Promises high daily income for liking videos or simple online tasks.")
            inferenceList.add("Scammers build trust with tiny initial payouts before trapping victims in prepaid tasks.")
        }

        if (lowerText.contains("customs") || lowerText.contains("fedex") || lowerText.contains("parcel seized") || lowerText.contains("drugs") || lowerText.contains("police")) {
            category = ScamCategory.FAKE_POLICE
            score += 65
            flags.add("Uses severe fear & authority impersonation (Fake Police / Customs scam)")
            flags.add("Demands immediate clearance payment to avoid arrest")
            observedList.add("Claims illegal drugs or parcel seized with threat of immediate digital arrest.")
            inferenceList.add("Law enforcement agency impersonation designed to induce panic payment.")
        }

        if (lowerText.contains("otp") || lowerText.contains("verification code")) {
            if (category == ScamCategory.UNKNOWN) category = ScamCategory.OTP_HARVESTING
            score += 30
            flags.add("Requests sharing 6-digit confidential OTP")
            observedList.add("Requests 6-digit one-time verification password.")
            inferenceList.add("OTP sharing enables immediate unauthorized transaction authorization.")
        }

        score = score.coerceIn(10, 98)

        val observed = if (observedList.isEmpty()) "Content contains unverified digital request." else observedList.joinToString(" ")
        val inference = if (inferenceList.isEmpty()) "Analyzed for psychological pressure and unauthorized financial handles." else inferenceList.joinToString(" ")
        val extIntel = "External Lookup: Intelligence Provider unavailable. Evaluated using local forensic engine."
        val rec = if (score >= 60) "Do NOT share OTP, enter UPI PIN, or pay money. Block sender." else "Verify directly with official bank customer care."

        if (score >= 70) {
            summary = "HIGH SCAM THREAT: Evidence exhibits severe fear tactics, fraudulent impersonation, and immediate financial risk."
            recs.add("Do NOT click links, pay money, or share any OTP")
            recs.add("Immediately block and report this contact")
            recs.add("File a report on National Cyber Crime Portal (1930)")
        } else if (score >= 40) {
            summary = "MODERATE RISK: Caution advised. Unverified sources or unusual urgency patterns detected."
            recs.add("Verify via official bank website or customer service number")
            recs.add("Never enter your UPI PIN to receive money")
        } else {
            summary = "LOW RISK: No high-severity malicious scam indicators detected in initial evaluation."
            recs.add("Always maintain caution when dealing with unexpected links")
        }

        if (flags.isEmpty()) flags.add("Standard digital content scan executed")

        return ScamAnalysisResult(
            riskScore = score,
            scamCategory = category,
            primarySummary = summary,
            observedEvidence = observed,
            inference = inference,
            externalIntelligence = extIntel,
            recommendation = rec,
            redFlags = flags,
            recommendations = recs,
            extractedContent = text
        )
    }
}

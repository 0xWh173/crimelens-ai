package com.example.util

import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamCategory
import java.net.URI

object UrlAnalysisEngine {

    private val SUSPICIOUS_TLDS = setOf(
        "top", "xyz", "site", "club", "work", "click", "link", "online",
        "vip", "win", "cc", "cf", "gq", "ml", "tk", "ga", "biz", "download", "shop"
    )

    private val URL_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "cutt.ly", "shorturl.at", "rb.gy", "owl.ly", "v.gd"
    )

    private val KNOWN_BRANDS = listOf(
        "sbi", "hdfc", "icici", "axis", "paytm", "gpay", "phonepe", "customs",
        "fedex", "indiapost", "police", "cybercrime", "amazon", "flipkart", "netflix", "telegram", "whatsapp"
    )

    private val SUSPICIOUS_PATH_KEYWORDS = listOf(
        "kyc", "blocked", "verify", "otp", "refund", "claim", "reward", "lottery",
        "urgent", "passcode", "suspend", "token", "auth", "login", "netbanking"
    )

    fun analyzeUrl(urlString: String): ScamAnalysisResult {
        val rawInput = urlString.trim()
        val normalizedUrl = if (!rawInput.startsWith("http://", ignoreCase = true) &&
            !rawInput.startsWith("https://", ignoreCase = true)
        ) {
            "http://$rawInput"
        } else {
            rawInput
        }

        val redFlags = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        val technicalDetails = mutableMapOf<String, String>()
        var score = 10
        var category = ScamCategory.UNKNOWN

        try {
            val uri = URI(normalizedUrl)
            val scheme = uri.scheme?.lowercase() ?: "http"
            val host = uri.host?.lowercase() ?: ""
            val path = uri.path?.lowercase() ?: ""
            val query = uri.query?.lowercase() ?: ""

            technicalDetails["Parsed Scheme"] = scheme.uppercase()
            technicalDetails["Hostname"] = if (host.isBlank()) "Unknown / Invalid Host" else host
            technicalDetails["Threat Intelligence Provider"] = "Local Real-Time Forensic Engine (External API Key Unconfigured)"

            // Signal 1: Protocol Check
            if (scheme == "http") {
                score += 25
                redFlags.add("Non-Secure Protocol (HTTP): Lacks SSL encryption, exposing credentials to interception.")
                technicalDetails["SSL Status"] = "Insecure (HTTP)"
            } else {
                technicalDetails["SSL Status"] = "Encrypted (HTTPS)"
            }

            // Signal 2: Raw IP Address Host
            val ipRegex = Regex("""^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$""")
            if (ipRegex.matches(host)) {
                score += 40
                redFlags.add("Host is a Raw IP Address: Legitimate financial and government portals use registered domain names, not raw IP addresses.")
                technicalDetails["IP Host Detected"] = "YES"
            }

            // Signal 3: Punycode Detection
            if (host.contains("xn--")) {
                score += 45
                redFlags.add("Punycode / Homograph Attack: Domain uses internationalized characters to impersonate legitimate brand names.")
                technicalDetails["Punycode Detected"] = "YES"
            }

            // Signal 4: Suspicious TLD
            val tld = host.substringAfterLast(".", "")
            if (SUSPICIOUS_TLDS.contains(tld)) {
                score += 30
                redFlags.add("High-Risk Scam Top-Level Domain (.$tld): High statistical correlation with phishing infrastructure.")
                technicalDetails["Top-Level Domain"] = ".$tld (High-Risk TLD)"
            }

            // Signal 5: URL Shortener
            if (URL_SHORTENERS.contains(host)) {
                score += 20
                redFlags.add("URL Shortener Detected ($host): Hides final destination URL to obscure malicious phishing endpoints.")
                technicalDetails["URL Shortener"] = "YES ($host)"
            }

            // Signal 6: Brand Impersonation & Typosquatting
            for (brand in KNOWN_BRANDS) {
                if (host.contains(brand)) {
                    val isOfficial = when (brand) {
                        "sbi" -> host.endsWith("sbi.co.in") || host == "onlinesbi.sbi" || host == "sbi"
                        "hdfc" -> host.endsWith("hdfcbank.com") || host.endsWith("hdfc.com")
                        "icici" -> host.endsWith("icicibank.com")
                        "axis" -> host.endsWith("axisbank.com")
                        "paytm" -> host.endsWith("paytm.com") || host.endsWith("paytm.in")
                        "gpay", "google" -> host.endsWith("google.com") || host.endsWith("pay.google.com")
                        "phonepe" -> host.endsWith("phonepe.com")
                        "amazon" -> host.endsWith("amazon.in") || host.endsWith("amazon.com")
                        "flipkart" -> host.endsWith("flipkart.com")
                        "fedex" -> host.endsWith("fedex.com")
                        "indiapost" -> host.endsWith("indiapost.gov.in")
                        "police", "cybercrime" -> host.endsWith("gov.in") || host.endsWith("nic.in")
                        else -> false
                    }

                    if (!isOfficial) {
                        score += 45
                        category = when (brand) {
                            "sbi", "hdfc", "icici", "axis" -> ScamCategory.BANK_IMPERSONATION
                            "paytm", "gpay", "phonepe" -> ScamCategory.UPI_FRAUD
                            "customs", "fedex", "indiapost" -> ScamCategory.COURIER_PARCEL
                            "police", "cybercrime" -> ScamCategory.FAKE_POLICE
                            else -> ScamCategory.BANK_IMPERSONATION
                        }
                        redFlags.add("Brand Typosquatting / Impersonation: Domain '$host' incorporates brand '$brand' but is NOT an official domain.")
                        technicalDetails["Brand Impersonation Target"] = brand.uppercase()
                        break
                    }
                }
            }

            // Signal 7: Suspicious Keywords in Path/Query
            val combinedPathQuery = "$path $query"
            val matchedKeywords = SUSPICIOUS_PATH_KEYWORDS.filter { combinedPathQuery.contains(it) }
            if (matchedKeywords.isNotEmpty()) {
                score += 20
                redFlags.add("Phishing Path Keywords Detected: Uses sensitive terms (${matchedKeywords.joinToString(", ")}) designed to trick users into submitting credentials.")
                technicalDetails["Matched Suspicious Keywords"] = matchedKeywords.joinToString(", ")
            }

        } catch (e: Exception) {
            redFlags.add("Malformed URL Structure: Input URL could not be safely parsed by security standards.")
            score += 25
            technicalDetails["URL Parser Error"] = e.localizedMessage ?: "Invalid URL"
        }

        score = score.coerceIn(5, 98)

        val observed = if (redFlags.isEmpty()) {
            "URL structure inspected. Standard domain pattern with valid structure."
        } else {
            "Observed ${redFlags.size} security anomaly/anomalies in URL payload."
        }

        val inference = if (score >= 60) {
            "High probability of malicious credential harvesting, brand impersonation, or phishing redirect."
        } else if (score >= 35) {
            "Moderate risk detected. Caution advised when accessing unverified links."
        } else {
            "Low risk structure detected. Standard web link characteristics."
        }

        val extIntel = "External Intelligence Provider: Unavailable (Evaluated strictly via local structural signals)."

        val rec = if (score >= 60) {
            "Do NOT open link or submit credentials. Block sender and report phishing URL."
        } else {
            "Verify destination domain carefully before logging in or making payments."
        }

        if (score >= 70) {
            recommendations.add("Do NOT click or open this link on any browser.")
            recommendations.add("Never enter bank account, OTP, or UPI details on this site.")
            recommendations.add("Report phishing link to National Cyber Crime Portal (1930).")
        } else {
            recommendations.add("Verify official website URL manually from search engines.")
            recommendations.add("Ensure browser displays valid SSL lock icon.")
        }

        return ScamAnalysisResult(
            riskScore = score,
            scamCategory = category,
            primarySummary = if (score >= 60) "HIGH SCAM RISK: Suspicious URL structure with brand impersonation and security red flags." else "LOW/MODERATE RISK: Web link evaluated.",
            observedEvidence = observed,
            inference = inference,
            externalIntelligence = extIntel,
            recommendation = rec,
            redFlags = if (redFlags.isEmpty()) listOf("No severe structural anomalies detected") else redFlags,
            recommendations = recommendations,
            extractedContent = rawInput,
            technicalDetails = technicalDetails
        )
    }
}

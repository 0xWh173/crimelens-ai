package com.example.data.remote

import com.example.data.model.CommunityReport
import com.example.data.model.ScamCategory
import com.example.data.model.ScamReportRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreScamRepository : ScamReportRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    override fun getCommunityReports(): Flow<List<CommunityReport>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("CommunityReports")
            .orderBy("reportDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fail gracefully without crashing
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reports = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val title = doc.getString("scamTitle") ?: doc.getString("title") ?: ""
                            val categoryStr = doc.getString("category") ?: "UNKNOWN"
                            val city = doc.getString("locationCity") ?: doc.getString("city") ?: ""
                            val timestamp = doc.getLong("reportDate") ?: doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val desc = doc.getString("description") ?: ""
                            val upvotes = doc.getLong("upvotes")?.toInt() ?: 0
                            val verified = doc.getBoolean("verifiedScam") ?: doc.getBoolean("verified") ?: true
                            val risk = doc.getString("riskLevel") ?: "HIGH"
                            val severity = doc.getString("severity") ?: "HIGH"
                            val source = doc.getString("source") ?: "User Report"

                            val category = try { ScamCategory.valueOf(categoryStr) } catch (e: Exception) { ScamCategory.UNKNOWN }

                            CommunityReport(
                                id = id,
                                scamTitle = title,
                                category = category,
                                locationCity = city,
                                reportDate = timestamp,
                                description = desc,
                                upvotes = upvotes,
                                verifiedScam = verified,
                                riskLevel = risk,
                                severity = severity,
                                source = source
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(reports)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun addCommunityReport(report: CommunityReport) {
        val db = firestore ?: return
        try {
            val docData = hashMapOf(
                "scamTitle" to report.scamTitle,
                "category" to report.category.name,
                "locationCity" to report.locationCity,
                "reportDate" to report.reportDate,
                "description" to report.description,
                "upvotes" to report.upvotes,
                "verifiedScam" to report.verifiedScam,
                "riskLevel" to report.riskLevel,
                "severity" to report.severity,
                "source" to report.source
            )
            db.collection("CommunityReports").document(report.id).set(docData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun upvoteReport(reportId: String) {
        val db = firestore ?: return
        try {
            db.collection("CommunityReports").document(reportId)
                .update("upvotes", FieldValue.increment(1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

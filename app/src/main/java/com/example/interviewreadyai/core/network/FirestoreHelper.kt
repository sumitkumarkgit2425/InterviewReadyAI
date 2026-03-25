package com.example.interviewreadyai.core.network

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class CloudSession(
    val timestamp: Long = 0,
    val userUid: String = "",
    val overallScore: Double = 0.0,
    val clarityScore: Double = 0.0,
    val confidenceScore: Double = 0.0,
    val technicalScore: Double = 0.0,
    val totalQuestions: Int = 0,
    val performanceSummary: String = "",
    val targetRole: String = "",
    val jdText: String = "",
    val evaluations: List<CloudEvaluation> = emptyList()
)

data class CloudEvaluation(
    val questionIndex: Int = 0,
    val questionText: String = "",
    val userAnswer: String = "",
    val score: Int = 0,
    val clarityScore: Int = 0,
    val confidenceScore: Int = 0,
    val technicalScore: Int = 0,
    val weakAreas: String = "",
    val recommendedKeywords: String = "",
    val starRewriteSituation: String = "",
    val starRewriteTask: String = "",
    val starRewriteAction: String = "",
    val starRewriteResult: String = "",
    val feedbackSummary: String = "",
    val detectedSkills: String = ""
)

object FirestoreHelper {
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    
    suspend fun uploadSession(userUid: String, session: CloudSession): Boolean {
        return try {
            val documentId = session.timestamp.toString()
            firestore.collection("users").document(userUid).collection("sessions")
                .document(documentId)
                .set(session)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    
    suspend fun downloadAllSessions(userUid: String): List<CloudSession> {
        return try {
            val snapshot = firestore.collection("users").document(userUid).collection("sessions")
                .get()
                .await()
            
            val sessions = mutableListOf<CloudSession>()
            for (doc in snapshot.documents) {
                val session = doc.toObject(CloudSession::class.java)
                if (session != null) {
                    sessions.add(session)
                }
            }
            sessions
        } catch (e: Exception) {
            emptyList()
        }
    }
}


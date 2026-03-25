package com.example.interviewreadyai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interview_sessions")
data class InterviewSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userUid: String = "", // Added for account isolation
    val isSynced: Boolean = false, // Added for cloud sync tracking
    val timestamp: Long = System.currentTimeMillis(),
    val overallScore: Double = 0.0,
    val clarityScore: Double = 0.0,
    val confidenceScore: Double = 0.0,
    val technicalScore: Double = 0.0,
    val totalQuestions: Int = 0,
    val performanceSummary: String = "",
    val targetRole: String = "",
    val jdText: String = ""
)

@Entity(tableName = "question_evaluations")
data class QuestionEvaluationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val questionIndex: Int,
    val questionText: String,
    val userAnswer: String,
    val score: Int,
    val clarityScore: Int = 0,
    val confidenceScore: Int = 0,
    val technicalScore: Int = 0,
    val weakAreas: String, // Delimited string
    val recommendedKeywords: String, // Comma separated
    val starRewriteSituation: String,
    val starRewriteTask: String,
    val starRewriteAction: String,
    val starRewriteResult: String,
    val feedbackSummary: String,
    val detectedSkills: String = "" // Delimited string
)


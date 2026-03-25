package com.example.interviewreadyai.data.repository

import com.example.interviewreadyai.data.local.InterviewDao
import com.example.interviewreadyai.data.local.InterviewSessionEntity
import com.example.interviewreadyai.data.local.QuestionEvaluationEntity
import com.example.interviewreadyai.feature.interview.viewmodel.EvaluationData
import com.example.interviewreadyai.core.network.CloudSession
import com.example.interviewreadyai.core.network.CloudEvaluation
import com.example.interviewreadyai.core.network.FirestoreHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

class InterviewRepository(private val dao: InterviewDao) {

    
    suspend fun saveSession(
            userUid: String,
            evaluations: List<EvaluationData>,
            performanceSummary: String,
            targetRole: String,
            jdText: String
    ): Long {
        if (evaluations.isEmpty()) return -1

        var totalScore = 0.0
        var totalClarity = 0.0
        var totalConfidence = 0.0
        var totalTechnical = 0.0
        val size = evaluations.size

        for (i in 0 until size) {
            val eval = evaluations[i]
            totalScore += eval.score
            totalClarity += eval.clarityScore
            totalConfidence += eval.confidenceScore
            totalTechnical += eval.technicalScore
        }

        val session =
                InterviewSessionEntity(
                        overallScore = totalScore / size,
                        clarityScore = totalClarity / size,
                        confidenceScore = totalConfidence / size,
                        technicalScore = totalTechnical / size,
                        totalQuestions = size,
                        performanceSummary = performanceSummary,
                        targetRole = targetRole,
                        jdText = jdText,
                        userUid = userUid,
                        isSynced = false
                )

        val sessionId = dao.insertSession(session)
        val entities = mutableListOf<QuestionEvaluationEntity>()

        for (i in 0 until size) {
            val eval = evaluations[i]
            entities.add(
                    QuestionEvaluationEntity(
                            sessionId = sessionId,
                            questionIndex = i,
                            questionText = eval.questionText,
                            userAnswer = eval.userAnswer,
                            score = eval.score,
                            clarityScore = eval.clarityScore,
                            confidenceScore = eval.confidenceScore,
                            technicalScore = eval.technicalScore,
                            weakAreas = eval.weakAreas.joinToString("|"),
                            recommendedKeywords = eval.recommendedKeywords.joinToString(","),
                            starRewriteSituation = eval.starRewrite.situation,
                            starRewriteTask = eval.starRewrite.task,
                            starRewriteAction = eval.starRewrite.action,
                            starRewriteResult = eval.starRewrite.result,
                            feedbackSummary = eval.feedbackSummary,
                            detectedSkills = eval.detectedSkills.joinToString("|")
                    )
            )
        }

        dao.insertEvaluations(entities)
        return sessionId
    }

    
    suspend fun getAnalyticsSummary(userUid: String): AnalyticsSummary {
        val last10Sessions = dao.getRecentSessions(userUid, 10)
        val sessionCount = last10Sessions.size

        
        var sumClarity = 0.0
        var sumConfidence = 0.0
        var sumTechnical = 0.0
        for (i in 0 until sessionCount) {
            sumClarity += last10Sessions[i].clarityScore
            sumConfidence += last10Sessions[i].confidenceScore
            sumTechnical += last10Sessions[i].technicalScore
        }

        val masteryClarity =
                if (sessionCount > 0) (sumClarity / sessionCount.toDouble() * 10.0).toInt() else 0
        val masteryConfidence =
                if (sessionCount > 0) (sumConfidence / sessionCount.toDouble() * 10.0).toInt()
                else 0
        val masteryTechnical =
                if (sessionCount > 0) (sumTechnical / sessionCount.toDouble() * 10.0).toInt() else 0

        
        val trendPoints = mutableListOf<Float>()
        val trendLimit = if (sessionCount < 7) sessionCount else 7
        for (i in trendLimit - 1 downTo 0) {
            trendPoints.add(last10Sessions[i].overallScore.toFloat())
        }

        
        val allSessions = dao.getRecentSessions(userUid, 50)
        var currentStreak = 0
        if (allSessions.isNotEmpty()) {
            val localDates = mutableListOf<LocalDate>()
            val zoneId = ZoneId.systemDefault()

            for (i in 0 until allSessions.size) {
                val date =
                        Instant.ofEpochMilli(allSessions[i].timestamp).atZone(zoneId).toLocalDate()

                var alreadyExists = false
                for (j in 0 until localDates.size) {
                    if (localDates[j] == date) {
                        alreadyExists = true
                        break
                    }
                }
                if (!alreadyExists) {
                    localDates.add(date)
                }
            }

            if (localDates.isNotEmpty()) {
                val today = LocalDate.now(zoneId)
                var checkDate: LocalDate? = null

                if (localDates[0] == today) {
                    checkDate = today
                } else if (localDates[0] == today.minusDays(1)) {
                    checkDate = today.minusDays(1)
                }

                if (checkDate != null) {
                    for (i in 0 until localDates.size) {
                        if (localDates[i] == checkDate) {
                            currentStreak++
                            checkDate = checkDate.minusDays(1)
                        } else {
                            break
                        }
                    }
                }
            }
        }

        
        val rawWeakAreas = dao.getRecentWeakAreas(userUid, 10)
        val freqMap = mutableMapOf<String, Int>()
        for (i in 0 until rawWeakAreas.size) {
            val areas = rawWeakAreas[i].split("|")
            for (j in 0 until areas.size) {
                val normalized = areas[j].trim().lowercase()
                if (normalized.isNotEmpty()) {
                    freqMap[normalized] = (freqMap[normalized] ?: 0) + 1
                }
            }
        }

        val sortedEntries = freqMap.entries.sortedByDescending { it.value }
        val focusAreas = mutableListOf<String>()
        val focusLimit = if (sortedEntries.size < 3) sortedEntries.size else 3
        for (i in 0 until focusLimit) {
            val raw = sortedEntries[i].key
            val words = raw.split(" ")
            var titleCased = ""
            for (w in 0 until words.size) {
                val word = words[w]
                if (word.isNotEmpty()) {
                    titleCased += word[0].uppercaseChar() + word.substring(1)
                    if (w < words.size - 1) titleCased += " "
                }
            }
            focusAreas.add(titleCased)
        }

        return AnalyticsSummary(
                masteryClarity = masteryClarity,
                masteryConfidence = masteryConfidence,
                masteryTechnical = masteryTechnical,
                performanceTrend = trendPoints,
                streakDayCount = currentStreak,
                topFocusAreas = focusAreas,
                totalPracticeMinutes = (dao.getSessionCount(userUid) * 10) // Placeholder logic
        )
    }

    suspend fun getMetricDeltas(userUid: String): MetricDeltas? {
        val sessions = dao.getLastTwoSessions(userUid)
        if (sessions.size < 2) return null

        val current = sessions[0]
        val previous = sessions[1]

        fun delta(curr: Double, prev: Double): Int {
            if (prev == 0.0) return 0
            return ((curr - prev) / prev * 100).toInt()
        }

        return MetricDeltas(
                clarityDelta = delta(current.clarityScore, previous.clarityScore),
                confidenceDelta = delta(current.confidenceScore, previous.confidenceScore),
                technicalDelta = delta(current.technicalScore, previous.technicalScore)
        )
    }

    fun getAllSessions(userUid: String): Flow<List<InterviewSessionEntity>> = dao.getAllSessions(userUid)
    suspend fun getRecentSessions(userUid: String, limit: Int): List<InterviewSessionEntity> =
            dao.getRecentSessions(userUid, limit)
    suspend fun getSessionCount(userUid: String): Int = dao.getSessionCount(userUid)
    
    
    suspend fun getUnsyncedSessions(userUid: String) = dao.getUnsyncedSessions(userUid)
    suspend fun markSessionsAsSynced(sessionIds: List<Long>) = dao.markSessionsAsSynced(sessionIds)
    suspend fun clearOtherUserSessions(currentUserUid: String) = dao.clearOtherUserSessions(currentUserUid)
    suspend fun resetSyncStatus(userUid: String) = dao.resetSyncStatus(userUid)

    suspend fun syncWithCloud(userUid: String) {
        if (userUid.isBlank() || userUid == "user@example.com") return

        val unsynced = dao.getUnsyncedSessions(userUid)
        if (unsynced.isNotEmpty()) {
            val syncedIds = mutableListOf<Long>()
            for (session in unsynced) {
                val evals = dao.getEvaluationsForSessionOnce(session.id)
                val cloudEvals = evals.map { e ->
                    CloudEvaluation(
                        e.questionIndex, e.questionText, e.userAnswer, e.score, e.clarityScore, e.confidenceScore, e.technicalScore, e.weakAreas, e.recommendedKeywords, e.starRewriteSituation, e.starRewriteTask, e.starRewriteAction, e.starRewriteResult, e.feedbackSummary, e.detectedSkills
                    )
                }
                val cloudSession = CloudSession(
                    session.timestamp, userUid, session.overallScore, session.clarityScore, session.confidenceScore, session.technicalScore, session.totalQuestions, session.performanceSummary, session.targetRole, session.jdText, cloudEvals
                )
                
                val uploadSuccess = FirestoreHelper.uploadSession(userUid, cloudSession)
                if (uploadSuccess) {
                    syncedIds.add(session.id)
                }
            }
            if (syncedIds.isNotEmpty()) {
                dao.markSessionsAsSynced(syncedIds)
            }
        }

        
        val cloudSessions = FirestoreHelper.downloadAllSessions(userUid)
        for (cs in cloudSessions) {
            val existing = dao.getSessionByTimestamp(userUid, cs.timestamp)
            if (existing == null) {
                val newSession = InterviewSessionEntity(
                    userUid = cs.userUid,
                    isSynced = true,
                    timestamp = cs.timestamp,
                    overallScore = cs.overallScore,
                    clarityScore = cs.clarityScore,
                    confidenceScore = cs.confidenceScore,
                    technicalScore = cs.technicalScore,
                    totalQuestions = cs.totalQuestions,
                    performanceSummary = cs.performanceSummary,
                    targetRole = cs.targetRole,
                    jdText = cs.jdText
                )
                val newId = dao.insertSession(newSession)
                
                val newEvals = cs.evaluations.map { ce ->
                    QuestionEvaluationEntity(
                        sessionId = newId,
                        questionIndex = ce.questionIndex,
                        questionText = ce.questionText,
                        userAnswer = ce.userAnswer,
                        score = ce.score,
                        clarityScore = ce.clarityScore,
                        confidenceScore = ce.confidenceScore,
                        technicalScore = ce.technicalScore,
                        weakAreas = ce.weakAreas,
                        recommendedKeywords = ce.recommendedKeywords,
                        starRewriteSituation = ce.starRewriteSituation,
                        starRewriteTask = ce.starRewriteTask,
                        starRewriteAction = ce.starRewriteAction,
                        starRewriteResult = ce.starRewriteResult,
                        feedbackSummary = ce.feedbackSummary,
                        detectedSkills = ce.detectedSkills
                    )
                }
                dao.insertEvaluations(newEvals)
            }
        }
    }
}

data class AnalyticsSummary(
        val masteryClarity: Int,
        val masteryConfidence: Int,
        val masteryTechnical: Int,
        val performanceTrend: List<Float>,
        val streakDayCount: Int,
        val topFocusAreas: List<String>,
        val totalPracticeMinutes: Int
)

data class MetricDeltas(val clarityDelta: Int, val confidenceDelta: Int, val technicalDelta: Int)


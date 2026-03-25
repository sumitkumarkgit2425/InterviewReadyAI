package com.example.interviewreadyai.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {

    
    @Insert
    suspend fun insertSession(session: InterviewSessionEntity): Long

    @Query("SELECT * FROM interview_sessions WHERE userUid = :userUid ORDER BY timestamp DESC")
    fun getAllSessions(userUid: String): Flow<List<InterviewSessionEntity>>

    @Query("SELECT * FROM interview_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): InterviewSessionEntity?

    @Query("SELECT * FROM interview_sessions WHERE userUid = :userUid AND timestamp = :timestamp LIMIT 1")
    suspend fun getSessionByTimestamp(userUid: String, timestamp: Long): InterviewSessionEntity?

    @Query("SELECT * FROM interview_sessions WHERE userUid = :userUid ORDER BY timestamp DESC LIMIT 2")
    suspend fun getLastTwoSessions(userUid: String): List<InterviewSessionEntity>

    
    @Insert
    suspend fun insertEvaluations(evaluations: List<QuestionEvaluationEntity>)

    @Query("SELECT * FROM question_evaluations WHERE sessionId = :sessionId ORDER BY questionIndex")
    fun getEvaluationsForSession(sessionId: Long): Flow<List<QuestionEvaluationEntity>>

    @Query("SELECT * FROM question_evaluations WHERE sessionId = :sessionId ORDER BY questionIndex")
    suspend fun getEvaluationsForSessionOnce(sessionId: Long): List<QuestionEvaluationEntity>

    
    @Query("SELECT AVG(overallScore) FROM interview_sessions WHERE userUid = :userUid")
    suspend fun getAverageOverallScore(userUid: String): Double?

    @Query("SELECT * FROM interview_sessions WHERE userUid = :userUid ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSessions(userUid: String, limit: Int): List<InterviewSessionEntity>

    @Query("SELECT weakAreas FROM question_evaluations WHERE sessionId IN (SELECT id FROM interview_sessions WHERE userUid = :userUid ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun getRecentWeakAreas(userUid: String, limit: Int): List<String>

    @Query("SELECT COUNT(*) FROM interview_sessions WHERE userUid = :userUid")
    suspend fun getSessionCount(userUid: String): Int

    
    @Query("SELECT * FROM interview_sessions WHERE userUid = :userUid AND isSynced = 0")
    suspend fun getUnsyncedSessions(userUid: String): List<InterviewSessionEntity>

    @Query("UPDATE interview_sessions SET isSynced = 1 WHERE id IN (:sessionIds)")
    suspend fun markSessionsAsSynced(sessionIds: List<Long>)

    @Query("UPDATE interview_sessions SET isSynced = 0 WHERE userUid = :userUid")
    suspend fun resetSyncStatus(userUid: String)

    @Query("DELETE FROM interview_sessions WHERE userUid != :currentUserUid")
    suspend fun clearOtherUserSessions(currentUserUid: String)
}


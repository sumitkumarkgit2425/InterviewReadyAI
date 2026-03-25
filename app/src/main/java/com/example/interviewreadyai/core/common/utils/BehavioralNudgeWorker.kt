package com.example.interviewreadyai.core.common.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.interviewreadyai.data.local.InterviewDatabase
import java.util.concurrent.TimeUnit

class BehavioralNudgeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val tips = listOf(
        "Quick Tip: Use the 'Result' in STAR to mention a 10% improvement you made.",
        "Quick Tip: Maintain eye contact and a steady pace during your technical answers.",
        "Quick Tip: When stuck on a coding problem, explain your thought process out loud.",
        "Quick Tip: Research the company's core values and weave them into your behavioral answers.",
        "Quick Tip: Ask insightful questions at the end of the interview to show your interest."
    )

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", "user@example.com") ?: "user@example.com"
        
        val dao = InterviewDatabase.getDatabase(applicationContext).interviewDao()
        val recent = dao.getRecentSessions(userEmail, 1)
        val latestSession = if (recent.isNotEmpty()) recent[0] else null
        
        val currentTime = System.currentTimeMillis()
        val lastInterviewTime = latestSession?.timestamp ?: 0L
        val diffHours = TimeUnit.MILLISECONDS.toHours(currentTime - lastInterviewTime)

        if (diffHours >= 24) {
            
            NotificationHelper.showNotification(
                applicationContext,
                "Practice Reminder",
                "Ready for a 5-minute mock? Your streak is at risk! 🔥",
                101
            )
        } else {
            
            val randomTip = tips.random()
            NotificationHelper.showNotification(
                applicationContext,
                "Interview Skill Tip",
                randomTip,
                102
            )
        }

        return Result.success()
    }
}


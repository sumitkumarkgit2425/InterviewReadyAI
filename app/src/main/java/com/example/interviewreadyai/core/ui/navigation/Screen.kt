package com.example.interviewreadyai.core.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth?autoBiometric={autoBiometric}") {
        fun createRoute(autoBiometric: Boolean) = "auth?autoBiometric=$autoBiometric"
    }
    object Dashboard : Screen("dashboard")
    object RecentSessions : Screen("recent_sessions")
    object Practice : Screen("practice")
    object MatchResult : Screen("match_result")
    object ProgressTracker : Screen("progress_tracker")
    object Profile : Screen("profile")
    object MockInterview : Screen("mock_interview")
    object InterviewSummary : Screen("interview_summary")
    object InterviewReport : Screen("interview_report/{sessionId}") {
        fun createRoute(sessionId: Long) = "interview_report/$sessionId"
    }
    object QuestionDetail : Screen("question_detail/{index}") {
        fun createRoute(index: Int) = "question_detail/$index"
    }
}


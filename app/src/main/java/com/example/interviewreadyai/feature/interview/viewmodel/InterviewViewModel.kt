package com.example.interviewreadyai.feature.interview.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.interviewreadyai.core.ai.GeminiHelper
import com.example.interviewreadyai.core.common.utils.ResumeParser
import com.example.interviewreadyai.core.network.MatchRequest
import com.example.interviewreadyai.core.network.MatchResponse
import com.example.interviewreadyai.core.network.RetrofitClient
import com.example.interviewreadyai.data.local.InterviewDatabase
import com.example.interviewreadyai.data.local.InterviewSessionEntity
import com.example.interviewreadyai.data.repository.AnalyticsSummary
import com.example.interviewreadyai.data.repository.InterviewRepository
import com.example.interviewreadyai.data.repository.MetricDeltas
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay as coroutineDelay

data class StarRewrite(
        @SerializedName("Situation") val situation: String,
        @SerializedName("Task") val task: String,
        @SerializedName("Action") val action: String,
        @SerializedName("Result") val result: String
)

data class EvaluationData(
        val score: Int = 0,
        @SerializedName("question") val questionText: String = "",
        @SerializedName("answer") val userAnswer: String = "",
        @SerializedName("clarity_score") val clarityScore: Int,
        @SerializedName("confidence_score") val confidenceScore: Int,
        @SerializedName("technical_score") val technicalScore: Int,
        @SerializedName("feedback") val feedbackSummary: String,
        @SerializedName("weak_areas") val weakAreas: List<String>,
        @SerializedName("detected_skills") val detectedSkills: List<String>,
        @SerializedName("recommended_keywords") val recommendedKeywords: List<String>,
        @SerializedName("star_rewrite") val starRewrite: StarRewrite,
)

data class BatchEvaluationResponse(
        @SerializedName("performance_summary") val performanceSummary: String,
        @SerializedName("evaluations") val evaluations: List<EvaluationData>
)

enum class Sender {
    AI,
    USER
}

data class InterviewMessage(
        val sender: Sender,
        val text: String,
        val evaluation: EvaluationData? = null
)

sealed class InterviewState {
    object Idle : InterviewState()
    object Connecting : InterviewState()
    object GeneratingQuestions : InterviewState()
    data class AiSpeaking(val text: String, val evaluation: EvaluationData? = null) :
            InterviewState()
    data class AiStreaming(val partialText: String, val latestSentence: String? = null) :
            InterviewState()
    object UserListening : InterviewState()
    data class Processing(val userText: String) : InterviewState()
    object Evaluating : InterviewState()
    object Matching : InterviewState()
    data class MatchResult(val result: MatchResponse) : InterviewState()
    data class Error(val message: String) : InterviewState()
    data class Finished(
            val evaluations: List<EvaluationData>,
            val performanceSummary: String,
            val metricDeltas: MetricDeltas? = null
    ) : InterviewState()
}

class InterviewViewModel(application: Application) : AndroidViewModel(application) {

    private val _targetRole = MutableStateFlow("Android Developer")
    val targetRoleState: StateFlow<String> = _targetRole.asStateFlow()
    var targetRole: String
        get() = _targetRole.value
        set(value) {
            _targetRole.value = value
        }

    private val _lastMatchResult = MutableStateFlow<MatchResponse?>(null)
    val lastMatchResult: StateFlow<MatchResponse?> = _lastMatchResult.asStateFlow()

    private var currentQuestionIndex = 0
    private val transcripts = mutableListOf<Pair<String, String>>()

    private val _uiState = MutableStateFlow<InterviewState>(InterviewState.Idle)
    val uiState: StateFlow<InterviewState> = _uiState.asStateFlow()

    private val _ttsFlow = MutableSharedFlow<String>(replay = 0)
    val ttsFlow: SharedFlow<String> = _ttsFlow.asSharedFlow()

    private val _messages = MutableStateFlow<List<InterviewMessage>>(emptyList())
    val messages: StateFlow<List<InterviewMessage>> = _messages.asStateFlow()

    private val _analytics = MutableStateFlow<AnalyticsSummary?>(null)
    val analytics: StateFlow<AnalyticsSummary?> = _analytics.asStateFlow()

    private val _resumeText = MutableStateFlow("")
    val resumeTextState: StateFlow<String> = _resumeText.asStateFlow()

    private var _jdText = MutableStateFlow("")
    val jdTextState: StateFlow<String> = _jdText.asStateFlow()

    private val _selectedInputType = MutableStateFlow("Resume")
    val selectedInputTypeState: StateFlow<String> = _selectedInputType.asStateFlow()
    var selectedInputType: String
        get() = _selectedInputType.value
        set(value) {
            _selectedInputType.value = value
        }

    var resumeText: String
        get() = _resumeText.value
        set(value) {
            _resumeText.value = value
            prefs.edit().putString("persisted_resume_text", value).apply()
        }

    var jdText: String
        get() = _jdText.value
        set(value) {
            _jdText.value = value
            prefs.edit().putString("persisted_jd_text", value).apply()
        }

    private val _interviewMode = MutableStateFlow("Mixed")
    val interviewModeState: StateFlow<String> = _interviewMode.asStateFlow()
    var interviewMode: String
        get() = _interviewMode.value
        set(value) {
            _interviewMode.value = value
        }

    private val _difficulty = MutableStateFlow("Mid")
    val difficultyState: StateFlow<String> = _difficulty.asStateFlow()
    var difficulty: String
        get() = _difficulty.value
        set(value) {
            _difficulty.value = value
        }

    private val _questionCount = MutableStateFlow(5)
    val questionCountState: StateFlow<Int> = _questionCount.asStateFlow()
    var questionCount: Int
        get() = _questionCount.value
        set(value) {
            _questionCount.value = value
        }

    private val _recentSessions = MutableStateFlow<List<InterviewSessionEntity>>(emptyList())
    val recentSessions: StateFlow<List<InterviewSessionEntity>> = _recentSessions.asStateFlow()

    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    private val _profilePicPath = MutableStateFlow<String?>(null)
    val profilePicPath: StateFlow<String?> = _profilePicPath.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _resumeUri = MutableStateFlow<String?>(null)
    val resumeUri: StateFlow<String?> = _resumeUri.asStateFlow()

    private val _resumeFileName = MutableStateFlow<String?>(null)
    val resumeFileName: StateFlow<String?> = _resumeFileName.asStateFlow()

    private val _userName = MutableStateFlow("Candidate")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("user@example.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl: StateFlow<String?> = _userPhotoUrl.asStateFlow()

    private val prefs by lazy {
        application.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    }

    private val repository: InterviewRepository by lazy {
        val dao = InterviewDatabase.getDatabase(application).interviewDao()
        InterviewRepository(dao)
    }

    private val gson = Gson()

    init {
        _profilePicPath.value = prefs.getString("profile_pic", null)
        _notificationsEnabled.value = prefs.getBoolean("notifications_enabled", true)
        _resumeUri.value = prefs.getString("resume_uri", null)
        _resumeFileName.value = prefs.getString("resume_file_name", null)
        _resumeText.value = prefs.getString("persisted_resume_text", "") ?: ""
        _jdText.value = prefs.getString("persisted_jd_text", "") ?: ""
        _isLoggedIn.value = prefs.getBoolean("is_logged_in", false)
        _isBiometricEnabled.value = prefs.getBoolean("biometric_enabled", false)
        _userName.value = prefs.getString("user_name", "Candidate") ?: "Candidate"
        _userEmail.value = prefs.getString("user_email", "user@example.com") ?: "user@example.com"
        _userPhotoUrl.value = prefs.getString("user_photo_url", null)

        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            _analytics.value = repository.getAnalyticsSummary(userEmail.value)
            _recentSessions.value = repository.getRecentSessions(userEmail.value, 5)
            _sessionCount.value = repository.getSessionCount(userEmail.value)
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _profilePicPath.value = null
        _notificationsEnabled.value = true
        _resumeUri.value = null
        _resumeFileName.value = null
        _resumeText.value = ""
        _jdText.value = ""
        _isLoggedIn.value = false
        _userName.value = "Candidate"
        _userEmail.value = "user@example.com"
        _userPhotoUrl.value = null
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun updateUserProfile(profile: com.example.interviewreadyai.core.auth.UserProfile) {
        _userName.value = profile.name ?: "Candidate"
        _userEmail.value = profile.email
        _userPhotoUrl.value = profile.photoUrl
        prefs.edit()
                .putString("user_name", _userName.value)
                .putString("user_email", _userEmail.value)
                .putString("user_photo_url", _userPhotoUrl.value)
                .apply()
        loadAnalytics()

        viewModelScope.launch {
            try {
                repository.resetSyncStatus(_userEmail.value)
                repository.syncWithCloud(_userEmail.value)
                loadAnalytics()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun saveProfilePicture(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val file = File(getApplication<Application>().filesDir, "profile_pic.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
                val path = file.absolutePath
                _profilePicPath.value = path
                prefs.edit().putString("profile_pic", path).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun toggleBiometric(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun updateResumeDetails(uri: String?, fileName: String?) {
        _resumeUri.value = uri
        _resumeFileName.value = fileName
        prefs.edit().putString("resume_uri", uri).putString("resume_file_name", fileName).apply()
    }

    fun parseResumeAndSave(uri: Uri, fileName: String?) {
        viewModelScope.launch {
            _uiState.value = InterviewState.Evaluating
            try {
                val context = getApplication<Application>()
                val mimeType = context.contentResolver.getType(uri)
                ResumeParser.extractTextFromUri(
                        context = context,
                        uri = uri,
                        mimeType = mimeType,
                        onSuccess = { text ->
                            resumeText = text
                            updateResumeDetails(uri.toString(), fileName)
                            _uiState.value = InterviewState.Idle
                        },
                        onError = { _uiState.value = InterviewState.Idle }
                )
            } catch (e: Exception) {
                _uiState.value = InterviewState.Idle
            }
        }
    }

    fun loadHistoricalSession(sessionId: Long) {
        viewModelScope.launch {
            _uiState.value = InterviewState.Evaluating
            try {
                val dao = InterviewDatabase.getDatabase(getApplication()).interviewDao()
                val session = dao.getSessionById(sessionId)
                val evaluationEntities = dao.getEvaluationsForSessionOnce(sessionId)

                if (session != null) {
                    val evaluations =
                            evaluationEntities.map { entity ->
                                EvaluationData(
                                        questionText = entity.questionText,
                                        userAnswer = entity.userAnswer,
                                        score = entity.score,
                                        clarityScore = entity.clarityScore,
                                        confidenceScore = entity.confidenceScore,
                                        technicalScore = entity.technicalScore,
                                        feedbackSummary = entity.feedbackSummary,
                                        weakAreas =
                                                entity.weakAreas.split("|").filter {
                                                    it.isNotEmpty()
                                                },
                                        detectedSkills =
                                                entity.detectedSkills.split("|").filter {
                                                    it.isNotEmpty()
                                                },
                                        recommendedKeywords =
                                                entity.recommendedKeywords.split(",").filter {
                                                    it.isNotEmpty()
                                                },
                                        starRewrite =
                                                StarRewrite(
                                                        situation = entity.starRewriteSituation,
                                                        task = entity.starRewriteTask,
                                                        action = entity.starRewriteAction,
                                                        result = entity.starRewriteResult
                                                )
                                )
                            }

                    _uiState.value =
                            InterviewState.Finished(
                                    evaluations = evaluations,
                                    performanceSummary = session.performanceSummary,
                                    metricDeltas = null
                            )
                } else {
                    _uiState.value = InterviewState.Error("Session not found.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value =
                        InterviewState.Error(
                                "Failed to load historical session: ${e.localizedMessage}"
                        )
            }
        }
    }

    fun startLiveInterview() {
        if (resumeText.isBlank() || jdText.isBlank()) {
            _uiState.value = InterviewState.Error("Resume or Job Description is missing.")
            return
        }

        viewModelScope.launch {
            _uiState.value = InterviewState.Connecting
            currentQuestionIndex = 0
            transcripts.clear()
            _messages.value = emptyList()

            try {
                _uiState.value = InterviewState.GeneratingQuestions
                
                var fullQuestion = ""
                var lastSpokenIndex = 0
                
                // Add an empty AI message to start the stream
                val initialMessages = _messages.value + InterviewMessage(Sender.AI, "")
                _messages.value = initialMessages

                GeminiHelper.generateFirstQuestionStream(
                    resumeText,
                    jdText,
                    interviewMode,
                    difficulty
                )
                .collect { chunk ->
                    fullQuestion += chunk
                    
                    // Update the message list with the latest partial text
                    val updatedMessages = _messages.value.toMutableList()
                    if (updatedMessages.isNotEmpty() && updatedMessages.last().sender == Sender.AI) {
                        updatedMessages[updatedMessages.size - 1] = 
                            updatedMessages.last().copy(text = fullQuestion)
                    }
                    _messages.value = updatedMessages

                    // Detect complete sentences for TTS using punctuation markers
                    val punctuationMarkers = CharArray(3) { '.' }
                    punctuationMarkers[0] = '.'
                    punctuationMarkers[1] = '?'
                    punctuationMarkers[2] = '!'
                    
                    val lastPunctPos = fullQuestion.findLastAnyOf(listOf(".", "?", "!"))?.first ?: -1
                    if (lastPunctPos > lastSpokenIndex) {
                        val sentenceToSpeak = fullQuestion.substring(lastSpokenIndex, lastPunctPos + 1)
                        viewModelScope.launch {
                            _ttsFlow.emit(sentenceToSpeak.trim())
                        }
                        lastSpokenIndex = lastPunctPos + 1
                    }
                    _uiState.value = InterviewState.AiStreaming(fullQuestion, null)
                }

                // After stream ends, speak any remaining text
                if (lastSpokenIndex < fullQuestion.length) {
                    val remainingText = fullQuestion.substring(lastSpokenIndex).trim()
                    if (remainingText.isNotEmpty()) {
                        _ttsFlow.emit(remainingText)
                    }
                }

                if (fullQuestion.isNotEmpty()) {
                    _uiState.value = InterviewState.AiSpeaking(fullQuestion)
                } else {
                    _uiState.value = InterviewState.Error("Failed to generate questions.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value =
                        InterviewState.Error(e.localizedMessage ?: "Failed to connect to AI.")
            }
        }
    }

    fun answerQuestion(userText: String) {
        if (userText.isBlank()) return

        _messages.value += InterviewMessage(Sender.USER, userText)
        
        // Find the last AI question in the messages list
        val lastAiQuestion = _messages.value.lastOrNull { it.sender == Sender.AI }?.text ?: ""
        transcripts.add(lastAiQuestion to userText)
        currentQuestionIndex++

        viewModelScope.launch {
            _uiState.value = InterviewState.Processing(userText)

            if (currentQuestionIndex < questionCount) {
                var fullNextQ = ""
                var lastSpokenIndex = 0
                
                // Add an empty AI message for the next question
                _messages.value += InterviewMessage(Sender.AI, "")

                try {
                    GeminiHelper.generateFollowUpQuestionStream(
                        resumeText,
                        jdText,
                        transcripts,
                        interviewMode,
                        difficulty
                    )
                    .collect { chunk ->
                        fullNextQ += chunk
                        
                        val updatedMessages = _messages.value.toMutableList()
                        if (updatedMessages.isNotEmpty() && updatedMessages.last().sender == Sender.AI) {
                            updatedMessages[updatedMessages.size - 1] = 
                                updatedMessages.last().copy(text = fullNextQ)
                        }
                        _messages.value = updatedMessages

                        // Detect complete sentences for TTS
                        val lastPunctPos = fullNextQ.findLastAnyOf(listOf(".", "?", "!"))?.first ?: -1
                        if (lastPunctPos > lastSpokenIndex) {
                            val sentenceToSpeak = fullNextQ.substring(lastSpokenIndex, lastPunctPos + 1)
                            viewModelScope.launch {
                                _ttsFlow.emit(sentenceToSpeak.trim())
                            }
                            lastSpokenIndex = lastPunctPos + 1
                        }
                        _uiState.value = InterviewState.AiStreaming(fullNextQ, null)
                    }

                    if (lastSpokenIndex < fullNextQ.length) {
                        val remainingText = fullNextQ.substring(lastSpokenIndex).trim()
                        if (remainingText.isNotEmpty()) {
                            _ttsFlow.emit(remainingText)
                        }
                    }
                    
                    if (fullNextQ.isNotEmpty()) {
                        _uiState.value = InterviewState.AiSpeaking(fullNextQ)
                    }
                } catch (e: Exception) {
                    _uiState.value = InterviewState.Error("Failed to generate follow-up: ${e.message}")
                }
            } else {
                val conclusion = "That concludes our interview! Please wait while I analyze your performance."
                _uiState.value = InterviewState.AiSpeaking(conclusion)
                _messages.value += InterviewMessage(Sender.AI, "Interview Complete! Analyzing...")
            }
        }
    }

    fun finishInterview() {
        viewModelScope.launch {
            _uiState.value = InterviewState.Evaluating
            try {
                val jsonResponse = GeminiHelper.evaluateFullInterview(transcripts)
                if (jsonResponse != null) {
                    val batchResponse =
                            gson.fromJson(jsonResponse, BatchEvaluationResponse::class.java)

                    val evaluationsWithContext = mutableListOf<EvaluationData>()
                    val evalSize = batchResponse.evaluations.size
                    for (i in 0 until evalSize) {
                        val eval = batchResponse.evaluations[i]
                        val (q, a) = if (i < transcripts.size) transcripts[i] else ("" to "")
                        evaluationsWithContext.add(
                                eval.copy(
                                        questionText = q,
                                        userAnswer = a,
                                        score =
                                                (eval.clarityScore +
                                                        eval.confidenceScore +
                                                        eval.technicalScore) / 3
                                )
                        )
                    }

                    repository.saveSession(
                            userUid = userEmail.value,
                            evaluations = evaluationsWithContext,
                            performanceSummary = batchResponse.performanceSummary,
                            targetRole = targetRole,
                            jdText = jdText
                    )

                    viewModelScope.launch {
                        try {
                            repository.syncWithCloud(userEmail.value)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    loadAnalytics()
                    val deltas = repository.getMetricDeltas(userEmail.value)

                    _uiState.value =
                            InterviewState.Finished(
                                    evaluations = evaluationsWithContext,
                                    performanceSummary = batchResponse.performanceSummary,
                                    metricDeltas = deltas
                            )

                    com.example.interviewreadyai.core.common.utils.NotificationHelper
                            .showNotification(
                                    getApplication<Application>(),
                                    "Interview Evaluated!",
                                    "Your mock interview has been successfully processed. Check out your detailed report.",
                                    1005
                            )
                } else {
                    _uiState.value = InterviewState.Error("Failed to obtain evaluation from AI.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = InterviewState.Error("Evaluation failed: ${e.localizedMessage}")
            }
        }
    }

    fun performResumeMatch(resume: String, jd: String, role: String = "") {
        if (role.isNotBlank()) {
            this.targetRole = role
        }

        this.resumeText = resume
        this.jdText = jd

        if (resume.isBlank() || jd.isBlank()) {
            _uiState.value = InterviewState.Error("Resume or Job Description is missing.")
            return
        }

        viewModelScope.launch {
            _uiState.value = InterviewState.Matching
            try {
                val response = RetrofitClient.apiService.getMatchScore(MatchRequest(resume, jd))

                if (response.isSuccessful && response.body() != null) {
                    val matchResult = response.body()!!
                    _lastMatchResult.value = matchResult
                    _uiState.value = InterviewState.MatchResult(matchResult)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown server error"
                    _uiState.value = InterviewState.Error("Render.com Error: $errorMsg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value =
                        InterviewState.Error("Backend Connection Failed: ${e.localizedMessage}")
            }
        }
    }

    fun setUiState(newState: InterviewState) {
        _uiState.value = newState
    }

    fun retryConnection() {
        _messages.value = emptyList()
        transcripts.clear()
        questionCount = 0
        startLiveInterview()
    }
}

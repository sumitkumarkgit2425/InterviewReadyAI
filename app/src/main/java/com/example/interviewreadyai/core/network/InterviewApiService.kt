package com.example.interviewreadyai.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface InterviewApiService {
    @POST("/match")
    suspend fun getMatchScore(@Body request: MatchRequest): Response<MatchResponse>
}

data class MatchRequest(
    @SerializedName("resume_text") val resume: String,
    @SerializedName("jd_text") val jd: String
)

data class MatchResponse(
    @SerializedName(value = "match_score", alternate = ["matchScore", "score", "match_percentage"]) 
    val matchScore: Double?,
    
    @SerializedName(value = "missing_keywords", alternate = ["missingKeywords", "missing_skills", "keywords_missing"]) 
    val missingKeywords: List<String>?,
    
    @SerializedName(value = "analysis_summary", alternate = ["analysisSummary", "summary", "analysis"]) 
    val analysisSummary: String?
)




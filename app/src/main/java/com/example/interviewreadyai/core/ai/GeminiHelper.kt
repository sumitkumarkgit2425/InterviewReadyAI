package com.example.interviewreadyai.core.ai

import com.example.interviewreadyai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiHelper {

    // Text model for streaming questions (supports TTS)
    private val model = GenerativeModel(
        modelName = "gemini-3.1-flash-lite-preview",
        apiKey = BuildConfig.GEMINI_API_KEY,
        requestOptions = RequestOptions(apiVersion = "v1beta"),
        generationConfig = generationConfig {
            temperature = 0.7f
        }
    )

    // Structured model for evaluations and resume matching (JSON mode)
    private val structuredModel = GenerativeModel(
        modelName = "gemini-3.1-flash-lite-preview",
        apiKey = BuildConfig.GEMINI_API_KEY,
        requestOptions = RequestOptions(apiVersion = "v1beta"),
        generationConfig = generationConfig {
            temperature = 0.7f
            responseMimeType = "application/json"
        }
    )

    suspend fun generateInterviewQuestions(
            resume: String,
            jd: String,
            mode: String = "Mixed",
            difficulty: String = "Mid",
            count: Int = 5
    ): List<String> =
            withContext(Dispatchers.IO) {
                val prompt =
                        """
            You are an expert interviewer specializing in both technical and behavioral assessments. 
            Based on the following Candidate Resume and Job Description, generate exactly $count relevant interview questions.
            
            Focus heavily on these parameters for generation:
            - Interview Mode: $mode (If 'Technical', focus only on technical/coding/architecture questions. If 'Behavioral', focus only on soft skills/STAR method. If 'Mixed', provide a balance of both).
            - Difficulty Level: $difficulty
            
            RESUME: $resume
            JOB DESCRIPTION: $jd
            
            Return ONLY the questions, each on a new line, without any numbering or additional text.
        """.trimIndent()

                val response = model.generateContent(prompt)
                response.text?.split("\n")?.filter { it.isNotBlank() }?.take(count) ?: emptyList()
            }

    fun generateFirstQuestionStream(
            resume: String,
            jd: String,
            mode: String = "Mixed",
            difficulty: String = "Mid"
    ): kotlinx.coroutines.flow.Flow<String> =
            kotlinx.coroutines.flow.flow {
                val prompt =
                        """
            You are an expert interviewer. Based on the Resume and JD below, generate the FIRST question for a $mode interview at $difficulty difficulty.
            
            RESUME: $resume
            JOB DESCRIPTION: $jd
            
            CRITICAL: Return ONLY the question text. No extra text, no conversational filler.
        """.trimIndent()
                model.generateContentStream(prompt).collect { chunk -> chunk.text?.let { emit(it) } }
            }

    fun generateFollowUpQuestionStream(
            resume: String,
            jd: String,
            transcript: List<Pair<String, String>>,
            mode: String = "Mixed",
            difficulty: String = "Mid"
    ): kotlinx.coroutines.flow.Flow<String> =
            kotlinx.coroutines.flow.flow {
                val history = transcript.joinToString("\n") { (q, a) -> "Q: $q\nA: $a" }
                val prompt =
                        """
            You are an expert interviewer. Based on the Resume, JD, and the conversation history below, generate the NEXT relevant interview question for a $mode role at $difficulty difficulty.
            Ensure the question is adaptive and probes deeper into the candidate's previous answers if necessary. 
            
            RESUME: $resume
            JOB DESCRIPTION: $jd
            HISTORY:
            $history
            
            CRITICAL: Return ONLY the next question text. No conversational filler like "Great answer" or "Next question is".
        """.trimIndent()
                model.generateContentStream(prompt).collect { chunk -> chunk.text?.let { emit(it) } }
            }

    suspend fun evaluateFullInterview(transcripts: List<Pair<String, String>>): String? =
            withContext(Dispatchers.IO) {
                val transcriptText =
                        transcripts.joinToString("\n\n") { (q, a) -> "QUESTION: $q\nANSWER: $a" }

                val prompt =
                        """
            You are an expert interviewer. Evaluate the following 5 Question-Answer pairs from an interview session.
            The interview may contain a mix of technical, conceptual, and behavioral questions.
            
            Provide a comprehensive analysis in a single JSON block with this strict schema:

            {
              "performance_summary": "...",
              "evaluations": [
                {
                  "question": "...",
                  "answer": "...",
                  "clarity_score": 9,
                  "confidence_score": 7,
                  "technical_score": 8,
                  "feedback": "...",
                  "weak_areas": ["..."],
                  "detected_skills": ["..."],
                  "recommended_keywords": ["..."],
                  "star_rewrite": {
                     "Situation": "...",
                     "Action": "...",
                     "Result": "..."
                  }
                }
              ]
            }

            CRITICAL GUIDELINES FOR EVALUATION:
            1. For Behavioral/Conceptual Questions: The 'technical_score' should represent the depth of 'Knowledge or Competency' shown in the answer. NEVER return "n/a" or non-numeric values.
            2. Sample Answer (STAR Rewrite): You MUST provide a comprehensive, high-quality sample answer for EVERY question, including technical ones. This acts as the 'ideal' response the user should have given. 
               - For Behavioral: Use the STAR format strictly.
               - For Technical: Use the STAR format to explain the problem-solving approach (e.g., Situation: The problem constraints; Action: The algorithm/logic applied; Result: Time/Space complexity optimization).
            3. Accuracy: Ensure the feedback is constructive and addresses the user directly as "You".
            4. Format: Return ONLY the raw JSON block. No markdown, no backticks.

            TRANSCRIPT:
            $transcriptText
        """.trimIndent()

                val response = structuredModel.generateContent(prompt)
                response.text?.trim()
            }

    suspend fun evaluateResumeMatch(resume: String, jd: String): String? =
            withContext(Dispatchers.IO) {
                val prompt =
                        """
            You are an expert recruitment automation agent. Compare the following Resume and Job Description.
            Extract an overall match percentage and identify missing keywords/skills from the resume that are required by the JD.
            Provide the result in a single JSON block with this strict schema:

            {
              "match_score": 85,
              "missing_keywords": ["Kotlin Flow", "Dependency Injection", "Unit Testing"],
              "analysis_summary": "A brief 2-sentence explanation of the match."
            }

            RESUME:
            $resume

            JOB DESCRIPTION:
            $jd

            CRITICAL: Return ONLY the raw JSON block. No markdown formatting, no backticks.
        """.trimIndent()

                val response = structuredModel.generateContent(prompt)
                response.text?.trim()
            }
}

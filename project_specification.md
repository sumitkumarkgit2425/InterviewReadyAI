# InterviewReady AI – Project Specification (Demo-Ready, ₹0 Cost Target)

## 1. Project Overview
InterviewReady AI is an Android-based AI-powered mock interview preparation tool that provides resume-aware interview question generation, answer evaluation, STAR-based answer rewriting, and performance analytics. The system uses Gemini API minimally for core AI tasks while keeping all other components local to ensure ₹0 operational cost for development and interview demos.

## 2. Objectives
- Provide ATS-like resume to job description matching.
- Generate personalized interview questions based on resume and JD.
- Conduct mock interviews using voice input.
- Evaluate answers with explainable feedback.
- Rewrite responses in STAR format.
- Track improvement across sessions using analytics.

## 3. Core Features

### a) Resume Upload & OCR
- Upload resume via PDF or camera.
- Extract text using ML Kit Text Recognition (offline).

### b) Resume–JD Matching
- Local TF-IDF + Cosine Similarity via Flask server.
- Generate Match Percentage.
- Identify Missing Keywords.

### c) Resume-Aware Question Generator
- Gemini API (gemini-1.5-flash).
- Single API call to generate 5 personalized questions.

### d) Mock Interview (Voice)
- Android SpeechRecognizer for Speech-to-Text.
- Real-time transcript display.

### e) Answer Evaluation + STAR Rewrite
- Gemini API (single call per answer).
- Returns: 
  - Score (/10) 
  - Weak Areas 
  - STAR-structured Rewrite

### f) Progress Tracker
- Session history stored in Room DB.
- Weekly score trends visualized using Vico Charts.

### g) Export Feature
- Generate PDF report for Before vs After responses.

## 4. Tech Stack

**Frontend:**
- Kotlin
- Jetpack Compose (Material 3)
- MVVM Architecture
- Navigation Compose

**AI & Processing:**
- Gemini API (gemini-1.5-flash) – minimal usage
- ML Kit OCR
- Android SpeechRecognizer

**Matching Engine:**
- TF-IDF (scikit-learn) via Local Flask API
- Cosine Similarity

**Storage & Analytics:**
- Room Database
- Vico Charts

**Networking & File Handling:**
- Retrofit
- Apache PDFBox (PDF Parsing)

## 5. Gemini API Usage Strategy
**Per Interview Session:**
- Question Generation: 1 API Call
- Answer Evaluation (5 Questions): 5 API Calls
- **Total: 6 API Calls per session**

**Free Tier Limits:**
- ~1500 Requests per day
- **Estimated Usage:** 10 sessions/day ≈ 60 calls -> **Within free quota (₹0 cost)**
- *Note:* Billing must be enabled to activate free tier. Charges apply only after exceeding free quota.

## 6. Architecture Flow
1. Resume Upload → OCR → Text Extraction
2. JD Input
3. TF-IDF Matching (Local Flask)
4. Gemini API → Generate Questions
5. Mock Interview (SpeechRecognizer) → Transcript
6. Gemini API → Score + Feedback + STAR Rewrite
7. Room DB → Store Session
8. Vico Charts → Weekly Trend

## 7. Implementation Timeline (4 Weeks)

**Week 1:**
- Resume Upload + OCR
- JD Input UI
- TF-IDF Matching Setup

**Week 2:**
- Question Generator (Gemini)
- Mock Interview UI + STT

**Week 3:**
- Answer Evaluation + STAR Rewrite (Gemini)
- Room DB Integration

**Week 4:**
- Analytics (Vico)
- PDF Export
- UI Polishing

## 8. Risk & Mitigation

- **Risk:** Gemini API cost
  - **Mitigation:** Strict call limits, Cache results in Room DB

- **Risk:** Local server dependency
  - **Mitigation:** Run Flask only during demo

- **Risk:** STT inaccuracies
  - **Mitigation:** Manual transcript edit option

## 9. Future Scope
- Deploy tf-idf on render.com
- Deploy backend to cloud.
- Multi-user support.
- Advanced analytics dashboard.
- Recruiter-facing evaluation reports.

## 10. Project Outcome
- Resume-ready AI project.
- Demo-friendly.
- ₹0 operational cost within free tier.
- Interview-safe architecture.

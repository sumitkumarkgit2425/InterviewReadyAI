# InterviewReadyAI 🚀

**InterviewReadyAI** is a cutting-edge Android application designed to revolutionize mock interviews. Powered by the latest **Gemini 3.1 Flash Lite** model, it provides an adaptive, low-latency, and highly realistic interviewing experience.

---

## ✨ Key Features

- **Adaptive AI Streaming**: Real-time question generation that adapts to your resume, job description, and previous answers on-the-fly.
- **Synchronized TTS (Text-To-Speech)**: High-speed, punctuation-aware voice engine that starts speaking as soon as the first sentence is generated.
- **Granular Evaluation**: Structured JSON-based performance analysis with STAR-method rewrites for every answer.
- **Smart Resume-JD Matching**: Instant match-score analysis with identified skill gaps.
- **Automated CI/CD**: Fully integrated GitHub Actions pipeline for automated builds, linting, and unit testing.

---

## 🛠️ Tech Stack

- **UI**: Jetpack Compose (Modern Declartive UI)
- **Language**: Kotlin
- **Asynchronous Flow**: Kotlin Coroutines & Flow (Iterative Streaming)
- **AI Engine**: Google Gemini SDK (Adaptive Generative AI)
- **Voice**: Android Text-to-Speech (Punctuation-aware Queueing)
- **Architecture**: MVVM + Clean Architecture
- **Automation**: GitHub Actions (CI/CD)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or later.
- A **Gemini API Key** from [Google AI Studio](https://aistudio.google.com/).

### Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/[username]/InterviewReadyAI.git
   ```
2. **Configure API Key**:
   Create a `local.properties` file in the root directory and add:
   ```properties
   GEMINI_API_KEY=YOUR_API_KEY_HERE
   ```
3. **Run the App**:
   Open in Android Studio and click **Run**.

### CI/CD Setup
To enable automated builds, add your `GEMINI_API_KEY` to your GitHub repository's **Secrets**:
`Settings > Secrets and variables > Actions > New repository secret`.

---

## 📈 Performance Improvements
- **Startup Latency**: Reduced from **~15s** to **~2s** (85% improvement) using iterative AI streaming.
- **Data Efficiency**: Optimized for token consumption to maintain Free Tier stability.

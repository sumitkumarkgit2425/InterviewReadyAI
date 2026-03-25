package com.example.interviewreadyai.feature.interview.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.interviewreadyai.feature.interview.viewmodel.InterviewState
import com.example.interviewreadyai.feature.interview.viewmodel.InterviewViewModel
import com.example.interviewreadyai.feature.interview.viewmodel.Sender
import com.example.interviewreadyai.core.ui.theme.*
import java.util.Locale

@Composable
fun MockInterviewScreen(
        resumeText: String,
        jdText: String,
        viewModel: InterviewViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToSummary: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val bgDark = BackgroundDark
    val primaryColor = PrimaryBlue
    val purpleColor = SecondaryPurple

    var permissionGranted by remember {
        mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
        )
    }
    var isRecording by remember { mutableStateOf(false) }
    var currentSpokenText by remember { mutableStateOf("") }
    var isTtsReady by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val permissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        permissionGranted = isGranted
                        if (!isGranted) {
                            Toast.makeText(
                                            context,
                                            "Audio permission is required for the live interview.",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                    }
            )

    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech =
                TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        isTtsReady = true
                        tts.value?.language = Locale.US
                        tts.value?.setSpeechRate(0.85f)
                        tts.value?.setPitch(1.1f)

                        val voices = tts.value?.voices
                        if (voices != null) {
                            val bestVoice =
                                    voices
                                            .filter {
                                                it.locale.language == "en" &&
                                                        !it.isNetworkConnectionRequired
                                            }
                                            .sortedByDescending { it.quality }
                                            .firstOrNull()
                            if (bestVoice != null) {
                                tts.value?.voice = bestVoice
                            }
                        }
                    }
                }

        textToSpeech.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "FINAL_SPEECH") {
                            viewModel.finishInterview()
                        } else {
                            viewModel.setUiState(InterviewState.UserListening)
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        viewModel.setUiState(InterviewState.UserListening)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        viewModel.setUiState(InterviewState.UserListening)
                    }
                }
        )

        tts.value = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechRecognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
            putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    10000L
            )
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L)
        }
    }

    DisposableEffect(context) {
        speechRecognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isRecording = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isRecording = false
                    }
                    override fun onError(error: Int) {
                        isRecording = false
                    }
                    override fun onResults(results: Bundle?) {
                        val matches =
                                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        currentSpokenText = text
                        isRecording = false
                        if (text.isNotBlank()) {
                            viewModel.answerQuestion(text)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches =
                                partialResults?.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                )
                        currentSpokenText = matches?.firstOrNull() ?: currentSpokenText
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                }
        )
        onDispose { speechRecognizer.destroy() }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is InterviewState.AiStreaming -> {
                // Now handled by ttsFlow collector for reliability
            }
            is InterviewState.AiSpeaking -> {
                val textToSpeak = state.text
                currentSpokenText = ""
                val utteranceId =
                        if (textToSpeak.contains("concludes our interview", ignoreCase = true))
                                "FINAL_SPEECH"
                        else "AI_SPEECH"
                
                if (utteranceId == "FINAL_SPEECH") {
                    tts.value?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    // Marker to signal end of AI turn
                    tts.value?.speak("", TextToSpeech.QUEUE_ADD, null, utteranceId)
                }
            }
            is InterviewState.Finished -> {
                onNavigateToSummary()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState, isTtsReady) {
        if (uiState is InterviewState.Idle && isTtsReady) {
            viewModel.startLiveInterview()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.ttsFlow.collect { sentence ->
            tts.value?.speak(sentence, TextToSpeech.QUEUE_ADD, null, "CHUNK")
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        viewModel.resumeText = resumeText
        viewModel.jdText = jdText
    }

    LaunchedEffect(messages.size, uiState) {
        kotlinx.coroutines.delay(100)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    LaunchedEffect(currentSpokenText) {
        if (currentSpokenText.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    val isAiSpeaking = uiState is InterviewState.AiSpeaking || uiState is InterviewState.AiStreaming
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulsate by
            infiniteTransition.animateFloat(
                    initialValue = 120f,
                    targetValue = 180f,
                    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                    label = "pulsating_size"
            )
    val currentOrbSize = if (isAiSpeaking || isRecording) pulsate else 120f

    val micScaleAnimatable = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                micScaleAnimatable.animateTo(1.2f, tween(400, easing = FastOutSlowInEasing))
                micScaleAnimatable.animateTo(1.0f, tween(400, easing = FastOutSlowInEasing))
            }
        } else {
            micScaleAnimatable.animateTo(1f, tween(200))
        }
    }
    val micScale = micScaleAnimatable.value

    Box(modifier = Modifier.fillMaxSize().background(bgDark)) {
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(
                                                                primaryColor.copy(alpha = 0.15f),
                                                                Color.Transparent
                                                        ),
                                                center = androidx.compose.ui.geometry.Offset(0f, 0f),
                                                radius = 1000f
                                        )
                                )
        )
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(
                                                                purpleColor.copy(alpha = 0.1f),
                                                                Color.Transparent
                                                        ),
                                                center =
                                                        androidx.compose.ui.geometry.Offset(
                                                                1000f,
                                                                0f
                                                        ),
                                                radius = 1000f
                                        )
                                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(
                                            top = 16.dp,
                                            start = 24.dp,
                                            end = 24.dp,
                                            bottom = 24.dp
                                    ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                        onClick = onNavigateBack,
                        modifier =
                                Modifier.size(40.dp)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                            text = "SESSION ACTIVE",
                            fontSize = 10.sp,
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = "LIVE INTERVIEW",
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                    )
                }

                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                                Modifier.background(
                                                primaryColor.copy(alpha = 0.1f),
                                                RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                                1.dp,
                                                primaryColor.copy(alpha = 0.2f),
                                                RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val indicatorColor = if (isRecording) Color.Red else primaryColor
                    Box(modifier = Modifier.size(8.dp).background(indicatorColor, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            if (isRecording) "REC" else "LIVE",
                            color = indicatorColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    val centerGradient =
                            if (isRecording) {
                                Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
                            } else {
                                Brush.linearGradient(
                                        listOf(primaryColor, Color(0xFF7C3AED), purpleColor)
                                )
                            }
                    Box(
                            modifier =
                                    Modifier.size(currentOrbSize.dp)
                                            .background(centerGradient, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).weight(1f)) {
                    Column(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .background(
                                                    Color.White.copy(alpha = 0.03f),
                                                    RoundedCornerShape(24.dp)
                                            )
                                            .border(
                                                    1.dp,
                                                    Color.White.copy(alpha = 0.08f),
                                                    RoundedCornerShape(24.dp)
                                            )
                                            .padding(24.dp)
                                            .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        messages.forEachIndexed { index, message ->
                            if (message.sender == Sender.AI) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                            text =
                                                    if (index == 0) "AI INTERVIEWER"
                                                    else if (message.text.contains(
                                                                    "Interview Complete",
                                                                    ignoreCase = true
                                                            )
                                                    )
                                                            "FINISHING UP"
                                                    else "FEEDBACK & NEXT QUESTION",
                                            color = primaryColor,
                                            fontSize = 10.sp,
                                            letterSpacing = 2.sp,
                                            fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                            text = message.text,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 26.sp,
                                            modifier = Modifier.fillMaxWidth()
                                    )

                                    message.evaluation?.let { eval ->
                                        Spacer(modifier = Modifier.height(16.dp))
                                        EvaluationCard(
                                                score = eval.score,
                                                weakAreas = eval.weakAreas,
                                                starRewrite = eval.starRewrite,
                                                primaryColor = primaryColor
                                        )
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                            modifier =
                                                    Modifier.fillMaxWidth(0.3f)
                                                            .height(1.dp)
                                                            .background(
                                                                    Color.White.copy(alpha = 0.1f)
                                                            )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                            text = "YOU",
                                            color = purpleColor,
                                            fontSize = 10.sp,
                                            letterSpacing = 2.sp,
                                            fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                            text = "\"${message.text}\"",
                                            color = Color.LightGray,
                                            fontSize = 16.sp,
                                            fontStyle = FontStyle.Italic,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (!isTtsReady) {
                            Text(
                                    "Initializing Voice Engine...",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontStyle = FontStyle.Italic
                            )
                        } else if (uiState is InterviewState.Connecting) {
                            Text(
                                    "Connecting to Gemini Engine...",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                            )
                        } else if (uiState is InterviewState.Idle) {
                             Text(
                                    "Starting interview...",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                            )
                        } else if (uiState is InterviewState.GeneratingQuestions) {
                            Text(
                                    "Generating personalized questions...",
                                    color = primaryColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                            )
                        } else if (uiState is InterviewState.Processing) {
                            Text(
                                    "Processing your answer...",
                                    color = primaryColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                            )
                        } else if (uiState is InterviewState.Evaluating) {
                            Text(
                                    "Analyzing your full performance...",
                                    color = primaryColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                            )
                        } else if (uiState is InterviewState.Error) {
                            Text(
                                    text = "Error: ${(uiState as InterviewState.Error).message}",
                                    color = Color.Red,
                                    fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                    onClick = { viewModel.retryConnection() },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    containerColor = primaryColor
                                            )
                            ) { Text("Retry Connection") }
                        }

                        if (isRecording && currentSpokenText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    text = "Recording...",
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                            )
                            Text(
                                    text = "\"$currentSpokenText\"",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center
                            )
                        } else if (uiState is InterviewState.UserListening && !isRecording) {
                            Text(
                                    text = "Tap the microphone to reply...",
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(bottom = 40.dp, start = 32.dp, end = 32.dp)
            ) {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                Color.White.copy(alpha = 0.03f),
                                                RoundedCornerShape(50)
                                        )
                                        .border(
                                                1.dp,
                                                Color.White.copy(alpha = 0.1f),
                                                RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                    Modifier.clickable {
                                        tts.value?.stop()
                                        onNavigateBack()
                                    }
                    ) {
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                                                .border(1.dp, Color.Red.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "End",
                                    tint = Color.Red
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                "END",
                                color = Color.Red.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                        )
                    }

                    val isMicEnabled =
                            permissionGranted &&
                                    uiState !is InterviewState.Connecting &&
                                    uiState !is InterviewState.Processing &&
                                    uiState !is InterviewState.AiSpeaking &&
                                    uiState !is InterviewState.AiStreaming
                    Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                    Modifier.clickable(enabled = isMicEnabled) {
                                        if (isRecording) {
                                            speechRecognizer.stopListening()
                                        } else {
                                            currentSpokenText = ""
                                            tts.value?.stop()
                                            speechRecognizer.startListening(speechRecognizerIntent)
                                        }
                                    }
                    ) {
                        val micBgColor = if (isRecording) Color.Red else primaryColor
                        Box(
                                modifier =
                                        Modifier.size(80.dp)
                                                .background(micBgColor, CircleShape)
                                                .border(4.dp, bgDark, CircleShape)
                                                .graphicsLayer {
                                                    scaleX = micScale
                                                    scaleY = micScale
                                                },
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mic",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                    Modifier.clickable {
                                        if (uiState is InterviewState.AiSpeaking ||
                                                        uiState is InterviewState.UserListening ||
                                                        uiState is InterviewState.AiStreaming
                                        ) {
                                            tts.value?.stop()
                                            currentSpokenText = ""
                                            viewModel.answerQuestion(
                                                    "Please move on to the next question."
                                            )
                                        }
                                    }
                    ) {
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .background(
                                                        Color.White.copy(alpha = 0.05f),
                                                        CircleShape
                                                )
                                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                "NEXT",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationCard(
        score: Int,
        weakAreas: List<String>,
        starRewrite: com.example.interviewreadyai.feature.interview.viewmodel.StarRewrite,
        primaryColor: Color
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                            modifier =
                                    Modifier.size(32.dp)
                                            .background(primaryColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = score.toString(),
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                            text = "Score: $score/10",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                    )
                }

                Surface(
                        color =
                                if (score >= 7) SuccessGreen.copy(alpha = 0.2f)
                                else WarningAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                            text = if (score >= 8) "EXCELLENT" else if (score >= 6) "GOOD" else "NEEDS WORK",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (score >= 7) Color(0xFF22C55E) else Color(0xFFEAB308)
                    )
                }
            }

            if (weakAreas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                        text = "WEAK AREAS",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                )
                Text(
                        text = weakAreas.joinToString("\n• ", prefix = "• "),
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    contentColor = Color.White
                            ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                        text = if (isExpanded) "Hide STAR Rewrite" else "View Optimized STAR Answer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = "OPTIMIZED (S.T.A.R) VERSION",
                        color = Color(0xFFA855F7),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = "Situation: ${starRewrite.situation}\nTask: ${starRewrite.task}\nAction: ${starRewrite.action}\nResult: ${starRewrite.result}",
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Normal
                )
            }
        }
    }
}

package com.example.interviewreadyai.feature.practice.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interviewreadyai.core.common.utils.FileUtils
import com.example.interviewreadyai.core.ui.theme.*
import com.example.interviewreadyai.feature.interview.viewmodel.InterviewState
import com.example.interviewreadyai.feature.interview.viewmodel.InterviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
        viewModel: InterviewViewModel,
        onNavigateToInterview: (String, String, String) -> Unit = { _, _, _ -> },
        onNavigateToMatchResult: () -> Unit = {},
        onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val resumeTextState by viewModel.resumeTextState.collectAsState()

    val jdText by viewModel.jdTextState.collectAsState()
    val targetRole by viewModel.targetRoleState.collectAsState()
    val selectedMode by viewModel.interviewModeState.collectAsState()
    val selectedDifficulty by viewModel.difficultyState.collectAsState()
    val questionCount by viewModel.questionCountState.collectAsState()
    val selectedFileName by viewModel.resumeFileName.collectAsState()
    val selectedInputType by viewModel.selectedInputTypeState.collectAsState()

    var isParsing by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val filePickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri
                ->
                uri?.let {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val (name, size) = FileUtils.getFileNameAndSize(context, it)
                    if (size > 5 * 1024 * 1024) {
                        Toast.makeText(context, "File > 5MB", Toast.LENGTH_SHORT).show()
                    } else {
                        isParsing = true
                        viewModel.parseResumeAndSave(it, name)
                    }
                }
            }

    LaunchedEffect(uiState) {
        if (uiState is InterviewState.MatchResult) {
            onNavigateToMatchResult()
            viewModel.setUiState(InterviewState.Idle)
        }

        if (uiState is InterviewState.Idle || uiState is InterviewState.Error) {
            isParsing = false
        }
    }

    LaunchedEffect(resumeTextState) {
        if (resumeTextState.isNotEmpty()) {
            isParsing = false
        }
    }

    val primaryColor = PrimaryBlue
    val purpleColor = SecondaryPurple

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
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
                                                center =
                                                        androidx.compose.ui.geometry.Offset(0f, 0f),
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

        Scaffold(containerColor = Color.Transparent, topBar = { PracticeHeader(onBack = onBack) }) {
                paddingValues ->
            LazyColumn(
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    InputTypeToggle(
                            selectedType = selectedInputType,
                            onTypeSelected = { viewModel.selectedInputType = it }
                    )
                }

                item {
                    if (selectedInputType == "Resume") {
                        ResumeSection(
                                selectedFileName = selectedFileName,
                                matchScore = null,
                                isParsing = isParsing,
                                onUploadClick = { filePickerLauncher.launch("*/*") }
                        )
                    } else {
                        OutlinedTextField(
                                value = jdText,
                                onValueChange = { viewModel.jdText = it },
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                placeholder = {
                                    Text("Paste requirements here...", color = Color.Gray)
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors =
                                        OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor =
                                                        Color.White.copy(alpha = 0.05f),
                                                unfocusedContainerColor =
                                                        Color.White.copy(alpha = 0.05f),
                                                unfocusedBorderColor =
                                                        Color.White.copy(alpha = 0.1f),
                                                focusedBorderColor = Purple40
                                        )
                        )
                    }
                }

                item {
                    val canCheck = resumeTextState.isNotBlank() && jdText.isNotBlank()
                    val isMatching = uiState is InterviewState.Matching

                    CheckMatchButton(
                            isEnabled = canCheck,
                            isMatching = isMatching,
                            onClick = {
                                viewModel.performResumeMatch(resumeTextState, jdText, targetRole)
                            }
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                                text = "Target Role",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                        )
                        OutlinedTextField(
                                value = targetRole,
                                onValueChange = { viewModel.targetRole = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("e.g. Senior Android Engineer", color = Color.Gray)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                        OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor =
                                                        Color.White.copy(alpha = 0.05f),
                                                unfocusedContainerColor =
                                                        Color.White.copy(alpha = 0.05f),
                                                unfocusedBorderColor =
                                                        Color.White.copy(alpha = 0.1f),
                                                focusedBorderColor = Purple40
                                        )
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                                text = "Interview Mode",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModeCard(
                                    title = "Behavioral",
                                    subText =
                                            "Focus on soft skills, leadership, and STAR method responses.",
                                    icon = Icons.AutoMirrored.Filled.Chat,
                                    isSelected = selectedMode == "Behavioral",
                                    onClick = { viewModel.interviewMode = "Behavioral" },
                                    modifier = Modifier.weight(1f)
                            )
                            ModeCard(
                                    title = "Technical",
                                    subText =
                                            "Deep dive into architecture, coding, and system design logic.",
                                    icon = Icons.Default.Code,
                                    isSelected = selectedMode == "Technical",
                                    onClick = { viewModel.interviewMode = "Technical" },
                                    modifier = Modifier.weight(1f)
                            )
                        }
                        ModeCard(
                                title = "Mixed Mode",
                                subText = "A holistic blend of technical depth and behavioral fit.",
                                icon = Icons.Default.AutoAwesome,
                                isSelected = selectedMode == "Mixed",
                                onClick = { viewModel.interviewMode = "Mixed" },
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    DifficultySelector(
                            selectedDifficulty = selectedDifficulty,
                            onDifficultySelected = { viewModel.difficulty = it }
                    )
                }

                item {
                    QuestionCountToggle(
                            selectedCount = questionCount,
                            onCountSelected = { viewModel.questionCount = it }
                    )
                }

                item {
                    val isEnabled =
                            (resumeTextState.isNotBlank() || jdText.isNotBlank()) &&
                                    selectedMode != null
                    StartButton(
                            isEnabled = isEnabled,
                            onClick = { onNavigateToInterview(resumeTextState, jdText, targetRole) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResumeSection(
        selectedFileName: String?,
        matchScore: Int?,
        isParsing: Boolean,
        onUploadClick: () -> Unit
) {
    if (selectedFileName == null) {

        OutlinedCard(
                onClick = onUploadClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.outlinedCardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                        ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = Purple40,
                        modifier = Modifier.size(32.dp)
                )
                Text(text = "Upload Resume", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "PDF or DOCX supported", color = Color.Gray, fontSize = 12.sp)
            }
        }
    } else {
        OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.outlinedCardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                        ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                            modifier =
                                    Modifier.size(48.dp)
                                            .background(
                                                    Purple40.copy(alpha = 0.2f),
                                                    RoundedCornerShape(12.dp)
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        if (isParsing) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Purple40,
                                    strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Purple40
                            )
                        }
                    }
                    Column {
                        Text(
                                text = selectedFileName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                        )
                        Text(
                                text =
                                        if (isParsing) "Extracting skills..."
                                        else "Ready for session",
                                color = if (isParsing) Purple40 else Color.Gray,
                                fontSize = 12.sp
                        )
                    }
                }
                if (matchScore != null && !isParsing) {
                    Box(
                            modifier =
                                    Modifier.background(
                                                    ScoreGreenBackground.copy(alpha = 0.2f),
                                                    RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                    1.dp,
                                                    ScoreGreenText.copy(alpha = 0.5f),
                                                    RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                                text = "$matchScore% Match",
                                color = ScoreGreenText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onUploadClick) {
                    Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckMatchButton(isEnabled: Boolean, isMatching: Boolean, onClick: () -> Unit) {
    val gradient =
            Brush.horizontalGradient(
                    colors =
                            if (isEnabled) listOf(Purple40, Color(0xFF2563EB))
                            else
                                    listOf(
                                             Color.White.copy(alpha = 0.05f),
                                             Color.White.copy(alpha = 0.1f)
                                    )
            )

    Button(
            onClick = onClick,
            enabled = isEnabled && !isMatching,
            modifier =
                    Modifier.fillMaxWidth()
                            .height(56.dp)
                            .background(gradient, RoundedCornerShape(16.dp)),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                    ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues()
    ) {
        if (isMatching) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                )
                Text(
                        text = "Connecting to AI...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                )
            }
        } else {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isEnabled) Color.White else Color.Gray
                )
                Text(
                        text = "Check Resume Match",
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color.White else Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchAnalysisSection(missingKeywords: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
                text = "Resume Insights",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
        )
        OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.outlinedCardColors(
                                containerColor = Color.White.copy(alpha = 0.02f)
                        ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                        text = "MISSING KEYWORDS",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                )
                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    missingKeywords.forEach { keyword ->
                        Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Red.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                        ) {
                            Text(
                                    text = keyword,
                                    color = Color(0xFFFF5252),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Text(
                        text = "Boost your match score by adding these skills if applicable.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ModeCard(
        title: String,
        subText: String,
        icon: ImageVector,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val backgroundColor by
            animateColorAsState(
                    if (isSelected) Color(0xFF258CF4).copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.05f),
                    label = "mode_bg_anim"
            )
    val borderColor by
            animateColorAsState(
                    if (isSelected) Color(0xFF258CF4) else Color.White.copy(alpha = 0.1f),
                    label = "mode_border_anim"
            )

    OutlinedCard(
            modifier = modifier.clip(RoundedCornerShape(20.dp)).clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor),
            border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .background(
                                                if (isSelected) Color(0xFF258CF4)
                                                else Color.White.copy(alpha = 0.05f),
                                                RoundedCornerShape(10.dp)
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color(0xFF258CF4),
                            modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                )
                Text(text = subText, color = Color.Gray, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun DifficultySelector(selectedDifficulty: String, onDifficultySelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
                text = "Difficulty Level",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
        )
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Junior", "Mid", "Senior").forEach { level ->
                val isSelected = selectedDifficulty == level
                Box(
                        modifier =
                                Modifier.weight(1f)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Purple40 else Color.Transparent)
                                        .clickable { onDifficultySelected(level) }
                                        .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = level,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCountToggle(selectedCount: Int, onCountSelected: (Int) -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                    text = "Question Count",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
            )
            Text(text = "Length of the session", color = Color.Gray, fontSize = 14.sp)
        }
        Row(
                modifier =
                        Modifier.background(
                                        Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CountChip(count = 3, isSelected = selectedCount == 3, onClick = { onCountSelected(3) })
            CountChip(count = 5, isSelected = selectedCount == 5, onClick = { onCountSelected(5) })
        }
    }
}

@Composable
fun CountChip(count: Int, isSelected: Boolean, onClick: () -> Unit) {

    Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) Purple40 else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Gray
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (count == 3)
                    Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                    )
            Text(text = "$count Qs", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun StartButton(isEnabled: Boolean, onClick: () -> Unit) {
    val gradient =
            Brush.horizontalGradient(
                    colors =
                            if (isEnabled) listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                            else
                                    listOf(
                                            Color.White.copy(alpha = 0.05f),
                                            Color.White.copy(alpha = 0.1f)
                                    )
            )

    Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier =
                    Modifier.fillMaxWidth()
                            .height(60.dp)
                            .background(gradient, RoundedCornerShape(30.dp)),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                    ),
            shape = RoundedCornerShape(30.dp),
            contentPadding = PaddingValues()
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                    text = "START AI SESSION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isEnabled) Color.White else Color.Gray,
                    letterSpacing = 1.sp
            )
            Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isEnabled) Color.White else Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeHeader(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tailor your AI interviewer",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "Refine your session for elite performance",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.statusBarsPadding()
    )
}

@Composable
fun InputTypeToggle(selectedType: String, onTypeSelected: (String) -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(4.dp)
    ) {
        listOf("Resume", "Job Description").forEach { type ->
            val isSelected = selectedType == type
            Box(
                    modifier =
                            Modifier.weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Purple40 else Color.Transparent)
                                    .clickable { onTypeSelected(type) }
                                    .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = type,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                )
            }
        }
    }
}

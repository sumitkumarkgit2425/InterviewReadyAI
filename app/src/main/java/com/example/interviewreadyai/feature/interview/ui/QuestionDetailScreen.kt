package com.example.interviewreadyai.feature.interview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interviewreadyai.feature.interview.viewmodel.EvaluationData
import com.example.interviewreadyai.feature.interview.viewmodel.StarRewrite
import com.example.interviewreadyai.core.ui.theme.*



@Composable
fun QuestionDetailScreen(
    evaluation: EvaluationData,
    questionIndex: Int,
    totalQuestions: Int = 5,
    onNavigateBack: () -> Unit,
    onNextQuestion: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    
    val animatedScore by animateFloatAsState(
        targetValue = evaluation.score.toFloat(),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "score_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 900f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(SecondaryPurple.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(1200f, 600f),
                        radius = 700f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "REFINED STAR FEEDBACK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                
                Spacer(modifier = Modifier.size(40.dp))
            }

            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                
                PerformanceScoreCard(
                    questionIndex = questionIndex,
                    totalQuestions = totalQuestions,
                    score = animatedScore,
                    clarityScore = evaluation.clarityScore,
                    confidenceScore = evaluation.confidenceScore,
                    technicalScore = evaluation.technicalScore
                )

                
                SectionLabel("CURRENT QUESTION")
                Text(
                    text = evaluation.questionText.ifBlank { "Interview Question" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 30.sp
                )

                
                SectionLabel("YOUR ORIGINAL ANSWER")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = evaluation.userAnswer.ifBlank { "No response recorded." },
                        modifier = Modifier.padding(16.dp),
                        fontSize = 15.sp,
                        color = Color.LightGray,
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Normal
                    )
                }

                
                ImpactAuditorTipCard(tip = evaluation.feedbackSummary)

                
                AnswerOptimizationDiff(
                    originalAnswer = evaluation.userAnswer,
                    starRewrite = evaluation.starRewrite
                )

                
                KeywordAuditSection(
                    detectedSkills = evaluation.detectedSkills,
                    recommendedKeywords = evaluation.recommendedKeywords
                )

                
                StarBreakdownSection(starRewrite = evaluation.starRewrite)

                Spacer(modifier = Modifier.height(24.dp))
            }

            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(bottom = 16.dp)
            ) {
                OutlinedButton(
                    onClick = { onNextQuestion?.invoke() ?: onNavigateBack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "Next Question",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}



@Composable
private fun PerformanceScoreCard(
    questionIndex: Int,
    totalQuestions: Int,
    score: Float,
    clarityScore: Int,
    confidenceScore: Int,
    technicalScore: Int
) {
    val isImproving = score >= 6f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderWhite.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PERFORMANCE SCORE",
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", score),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "/10",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                        )
                    }
                }
                
                Surface(
                    color = if (isImproving) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isImproving) SuccessGreen.copy(alpha = 0.35f) else WarningAmber.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = if (isImproving) "IMPROVING" else "NEEDS WORK",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isImproving) SuccessGreen else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SubScorePill("Clarity", clarityScore, ScoreBlueText)
                SubScorePill("Confidence", confidenceScore, SecondaryPurple)
                SubScorePill("Knowledge", technicalScore, SuccessGreen)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Question $questionIndex of $totalQuestions",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SubScorePill(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
        ) {
            Text(
                text = "$score/10",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ImpactAuditorTipCard(tip: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(WarningAmber.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "IMPACT AUDITOR TIP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = WarningAmber
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tip.ifBlank { "Boost your score by quantifying your output and using measurable results." },
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun AnswerOptimizationDiff(originalAnswer: String, starRewrite: StarRewrite) {
    SectionLabel("ANSWER OPTIMIZATION DIFF")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, BorderWhite, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        Column {
            Text(
                text = "ORIGINAL DRAFT",
                fontSize = 9.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444).copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"${originalAnswer.take(200)}${if (originalAnswer.length > 200) "..." else ""}\"",
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 20.sp,
                fontStyle = FontStyle.Italic
            )
        }

        
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(PrimaryBlue.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        
        Column {
            Text(
                text = "AI OPTIMISED VERSION",
                fontSize = 9.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"${starRewrite.action.take(200)}${if (starRewrite.action.length > 200) "..." else ""}\"",
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun KeywordAuditSection(
    detectedSkills: List<String>,
    recommendedKeywords: List<String>
) {
    SectionLabel("KEYWORD AUDIT")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, BorderWhite, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        
        if (detectedSkills.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(SuccessGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Detected Skills in User's Answer",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                FlowRowChips(
                    keywords = detectedSkills,
                    chipColor = SuccessGreen,
                    chipBg = SuccessGreen.copy(alpha = 0.12f)
                )
            }
        }

        
        if (recommendedKeywords.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(WarningAmber, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recommended Keywords",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                FlowRowChips(
                    keywords = recommendedKeywords,
                    chipColor = SecondaryPurple,
                    chipBg = SecondaryPurple.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowChips(keywords: List<String>, chipColor: Color, chipBg: Color) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        keywords.forEach { keyword ->
            Surface(
                color = chipBg,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, chipColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = keyword,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = chipColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StarBreakdownSection(starRewrite: StarRewrite) {
    SectionLabel("STAR BREAKDOWN")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StarCard(
            prefix = "S",
            label = "SITUATION",
            text = starRewrite.situation,
            accentColor = PrimaryBlue
        )
        StarCard(
            prefix = "T",
            label = "TASK",
            text = starRewrite.task,
            accentColor = SecondaryPurple
        )
        StarCard(
            prefix = "A",
            label = "ACTION",
            text = starRewrite.action,
            accentColor = SuccessGreen
        )
        StarCard(
            prefix = "R",
            label = "RESULT",
            text = starRewrite.result,
            accentColor = WarningAmber
        )
    }
}

@Composable
private fun StarCard(
    prefix: String,
    label: String,
    text: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, BorderWhite, RoundedCornerShape(14.dp))
    ) {
        
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor, accentColor.copy(alpha = 0.3f))
                    )
                )
        )
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = prefix,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HighlightedText(text = text)
        }
    }
}


@Composable
private fun HighlightedText(text: String) {
    val annotated = buildAnnotatedString {
        val regex = Regex("""\b(\d+%?[\w]*)\b""")
        var lastIndex = 0
        regex.findAll(text).forEach { match ->
            append(text.substring(lastIndex, match.range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        if (lastIndex < text.length) append(text.substring(lastIndex))
    }

    Text(
        text = annotated,
        fontSize = 14.sp,
        color = Color.LightGray,
        lineHeight = 22.sp
    )
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        color = Color.Gray
    )
}


package com.example.interviewreadyai.feature.interview.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interviewreadyai.data.repository.MetricDeltas
import com.example.interviewreadyai.feature.interview.viewmodel.EvaluationData
import com.example.interviewreadyai.core.ui.theme.*

@Composable
fun InterviewSummaryScreen(
    evaluations: List<EvaluationData>,
    performanceSummary: String = "",
    metricDeltas: MetricDeltas? = null,
    onQuestionClick: (Int) -> Unit,
    onTakeAnotherInterview: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val bgDark = BackgroundDark
    val primaryColor = PrimaryBlue
    val purpleColor = SecondaryPurple
    val greenColor = SuccessGreen
    val amberColor = WarningAmber

    val overallScore = if (evaluations.isNotEmpty()) {
        evaluations.map { it.score }.average()
    } else 0.0

    val avgClarity = if (evaluations.isNotEmpty()) evaluations.map { it.clarityScore }.average() else 0.0
    val avgConfidence = if (evaluations.isNotEmpty()) evaluations.map { it.confidenceScore }.average() else 0.0
    val avgTechnical = if (evaluations.isNotEmpty()) evaluations.map { it.technicalScore }.average() else 0.0

    val performanceLabel = when {
        overallScore >= 8.0 -> "Excellent Performance"
        overallScore >= 6.0 -> "Good Performance"
        overallScore >= 4.0 -> "Average Performance"
        else -> "Needs Improvement"
    }

    val performanceLabelColor = when {
        overallScore >= 8.0 -> greenColor
        overallScore >= 6.0 -> primaryColor
        overallScore >= 4.0 -> amberColor
        else -> Color.Red
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (overallScore / 10.0).toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "score_arc"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgDark)
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(100f, 200f),
                        radius = 800f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(purpleColor.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(800f, 400f),
                        radius = 600f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Interview Report Card",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(40.dp))
            }

            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(180.dp)
                        .drawBehind {
                            drawArc(
                                color = Color.White.copy(alpha = 0.08f),
                                startAngle = -225f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
                                topLeft = Offset(20f, 20f),
                                size = Size(size.width - 40f, size.height - 40f)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(primaryColor, purpleColor, primaryColor)
                                ),
                                startAngle = -225f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
                                topLeft = Offset(20f, 20f),
                                size = Size(size.width - 40f, size.height - 40f)
                            )
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", overallScore),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "/10",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "OVERALL SCORE",
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Performance Level", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = performanceLabel,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = performanceLabelColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                
                if (performanceSummary.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "OVERALL ANALYSIS",
                                fontSize = 10.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = performanceSummary,
                                color = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                
                Text(
                    text = "QUESTION ANALYSIS",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                evaluations.forEachIndexed { index, eval ->
                    QuestionCard(
                        index = index + 1,
                        evaluation = eval,
                        primaryColor = primaryColor,
                        amberColor = amberColor,
                        greenColor = greenColor,
                        onClick = { onQuestionClick(index) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        label = "CLARITY",
                        value = String.format("%.1f", avgClarity),
                        suffix = "/10",
                        delta = metricDeltas?.clarityDelta,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "CONFIDENCE",
                        value = String.format("%.1f", avgConfidence),
                        suffix = "/10",
                        delta = metricDeltas?.confidenceDelta,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "KNOWLEDGE",
                        value = String.format("%.1f", avgTechnical),
                        suffix = "/10",
                        delta = metricDeltas?.technicalDelta,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = onTakeAnotherInterview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "Take Another Mock Interview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    suffix: String,
    delta: Int?,
    modifier: Modifier = Modifier
) {
    val greenColor = Color(0xFF22C55E)
    val redColor = Color(0xFFEF4444)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (suffix.isNotEmpty()) {
                    Text(
                        text = suffix,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            if (delta != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val deltaText = if (delta >= 0) "+$delta%" else "$delta%"
                val deltaColor = if (delta >= 0) greenColor else redColor
                Text(
                    text = deltaText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = deltaColor
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    index: Int,
    evaluation: EvaluationData,
    primaryColor: Color,
    amberColor: Color,
    greenColor: Color,
    onClick: () -> Unit
) {
    val scoreColor = when {
        evaluation.score >= 8 -> greenColor
        evaluation.score >= 6 -> primaryColor
        else -> amberColor
    }
    val scoreLabel = when {
        evaluation.score >= 8 -> "Excellent"
        evaluation.score >= 6 -> "Good"
        else -> "Needs Work"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = scoreColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "SCORE: ${evaluation.score}/10",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = scoreLabel,
                        fontSize = 11.sp,
                        color = scoreColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View Details",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = evaluation.questionText.ifBlank { "Question $index" },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (evaluation.weakAreas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "WEAK AREAS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = amberColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = evaluation.weakAreas.joinToString("\n• ", prefix = "• "),
                    color = Color.LightGray.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


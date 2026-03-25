package com.example.interviewreadyai.feature.interview.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interviewreadyai.data.local.InterviewSessionEntity
import com.example.interviewreadyai.data.repository.AnalyticsSummary
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.example.interviewreadyai.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@Composable
fun ProgressTrackerScreen(
        analytics: AnalyticsSummary?,
        recentSessions: List<InterviewSessionEntity>,
        onNavigateBack: () -> Unit,
        onSessionClick: (Long) -> Unit = {}
) {
    val data = analytics

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.1f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(100f, 200f),
                        radius = 800f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(PurpleAccent.copy(alpha = 0.08f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(800f, 400f),
                        radius = 800f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(onNavigateBack = onNavigateBack)
            
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                
                item { PerformanceTrendCard(data) }

                
                item { QuickStatsRow(data) }

                
                item { SkillMasterySection(data) }

                
                item {
                    if (data != null && data.topFocusAreas.isNotEmpty()) {
                        AreasToFocusCard(data.topFocusAreas)
                    }
                }

                
                item {
                    Text(
                            text = "Recent Sessions",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                    )
                }

                if (recentSessions.isEmpty()) {
                    item {
                        Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text =
                                            "No interview sessions yet.\nComplete your first mock interview!",
                                    color = TextGray,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    val displayCount = if (recentSessions.size < 5) recentSessions.size else 5
                    for (i in 0 until displayCount) {
                        item { 
                            RecentSessionCard(
                                session = recentSessions[i], 
                                index = i,
                                onClick = { onSessionClick(recentSessions[i].id) }
                            ) 
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun HeaderSection(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
                text = "Progress Tracker",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        
        Box(modifier = Modifier.size(48.dp))
    }
}


@Composable
private fun PerformanceTrendCard(data: AnalyticsSummary?) {
    val trendPoints = data?.performanceTrend ?: emptyList()
    val avgScore =
            if (trendPoints.isNotEmpty()) {
                var sum = 0f
                for (i in 0 until trendPoints.size) {
                    sum += trendPoints[i]
                }
                sum / trendPoints.size
            } else 0f

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Performance Trend",
                        color = TextGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                )
                if (trendPoints.size >= 2) {
                    val lastVal = trendPoints[trendPoints.size - 1]
                    val firstVal = trendPoints[0]
                    val pctChange =
                            if (firstVal > 0f) ((lastVal - firstVal) / firstVal * 100).toInt()
                            else 0
                    val sign = if (pctChange >= 0) "+" else ""
                    Surface(
                            shape = RoundedCornerShape(12.dp),
                            color =
                                    if (pctChange >= 0) AccentGreen.copy(alpha = 0.15f)
                                    else Color.Red.copy(alpha = 0.15f)
                    ) {
                        Text(
                                text = "${sign}${pctChange}% vs first",
                                color = if (pctChange >= 0) SuccessGreen else Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                        text = String.format("%.1f", avgScore),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                )
                Text(
                        text = "/10",
                        color = TextGray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            
            if (trendPoints.size >= 2) {
                PerformanceLineChart(trendPoints)
            } else {
                Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "Complete 2+ sessions to see trends",
                            color = TextGray,
                            fontSize = 13.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun PerformanceLineChart(trendPoints: List<Float>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(trendPoints) {
        val yValues = mutableListOf<Number>()
        for (i in 0 until trendPoints.size) {
            yValues.add(trendPoints[i])
        }
        modelProducer.runTransaction { lineSeries { series(yValues) } }
    }

    val dayLabels = listOf("S1", "S2", "S3", "S4", "S5", "S6", "S7")
    val bottomFormatter = CartesianValueFormatter { _, x, _ ->
        val idx = x.toInt()
        if (idx >= 0 && idx < dayLabels.size) dayLabels[idx] else ""
    }

    CartesianChartHost(
            chart =
                    rememberCartesianChart(
                            rememberLineCartesianLayer(
                                    lineProvider =
                                            LineCartesianLayer.LineProvider.series(
                                                    LineCartesianLayer.rememberLine(
                                                            fill =
                                                                    LineCartesianLayer.LineFill
                                                                            .single(
                                                                                    fill(PrimaryBlue)
                                                                            ),
                                                    )
                                            )
                            ),
                            startAxis =
                                    VerticalAxis.rememberStart(
                                            label = null,
                                            tick = null,
                                            guideline = null,
                                    ),
                            bottomAxis =
                                    HorizontalAxis.rememberBottom(
                                            valueFormatter = bottomFormatter,
                                            guideline = null,
                                            tick = null,
                                    )
                    ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(140.dp)
    )
}


@Composable
private fun QuickStatsRow(data: AnalyticsSummary?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalFireDepartment,
                label = "STREAK",
                value = "${data?.streakDayCount ?: 0} Days",
                borderColor = AccentOrange,
                iconTint = AccentOrange
        )
        QuickStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timer,
                label = "PRACTICE",
                value = "${data?.totalPracticeMinutes ?: 0}m",
                borderColor = AccentPurple,
                iconTint = AccentPurple
        )
        QuickStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.TrackChanges,
                label = "GOAL",
                value =
                        if (data != null) {
                            val avg =
                                    (data.masteryClarity +
                                            data.masteryConfidence +
                                            data.masteryTechnical) / 3
                            "${avg}%"
                        } else "0%",
                borderColor = PrimaryBlue,
                iconTint = PrimaryBlue
        )
    }
}

@Composable
private fun QuickStatCard(
        modifier: Modifier = Modifier,
        icon: ImageVector,
        label: String,
        value: String,
        borderColor: Color,
        iconTint: Color
) {
    Card(
            modifier =
                    modifier.border(
                            width = 1.dp,
                            color = borderColor.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                    ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                        text = label,
                        color = TextGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
private fun SkillMasterySection(data: AnalyticsSummary?) {
    Column {
        Text(
                text = "Skill Mastery",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MasteryCircle(
                    label = "Clarity",
                    percentage = data?.masteryClarity ?: 0,
                    color = SuccessGreen
            )
            MasteryCircle(
                    label = "Confidence",
                    percentage = data?.masteryConfidence ?: 0,
                    color = AccentOrange
            )
            MasteryCircle(
                    label = "Knowledge",
                    percentage = data?.masteryTechnical ?: 0,
                    color = AccentCyan
            )
        }
    }
}

@Composable
private fun MasteryCircle(label: String, percentage: Int, color: Color) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by
            animateFloatAsState(
                    targetValue = if (animationTriggered) percentage / 100f else 0f,
                    animationSpec = tween(durationMillis = 1000),
                    label = "mastery_anim"
            )

    LaunchedEffect(Unit) { animationTriggered = true }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                
                drawArc(
                        color = color.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                
                drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                    text = "${percentage}%",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = TextGray, fontSize = 12.sp)
    }
}


@Composable
private fun AreasToFocusCard(focusAreas: List<String>) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = "Areas to Focus",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until focusAreas.size) {
                    Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AccentOrange.copy(alpha = 0.12f),
                            modifier =
                                    Modifier.border(
                                            1.dp,
                                            AccentOrange.copy(alpha = 0.35f),
                                            RoundedCornerShape(20.dp)
                                    )
                    ) {
                        Text(
                                text = focusAreas[i],
                                color = AccentOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun RecentSessionCard(session: InterviewSessionEntity, index: Int, onClick: () -> Unit = {}) {
    val borderColor = if (index % 2 == 0) AccentGreen else AccentBlue
    val dateFormat = SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(session.timestamp))
    val scoreVal = String.format("%.1f", session.overallScore)

    Card(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable { onClick() }
                            .border(
                                    width = 1.dp,
                                    color = borderColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                            ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                    modifier =
                            Modifier.size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(borderColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text =
                                if (session.targetRole.isNotEmpty()) session.targetRole
                                else if (session.performanceSummary.isNotEmpty()) {
                                    val words = session.performanceSummary.split(" ")
                                    val preview = StringBuilder()
                                    var taken = 0
                                    for (w in 0 until words.size) {
                                        if (taken >= 3) break
                                        if (taken > 0) preview.append(" ")
                                        preview.append(words[w])
                                        taken++
                                    }
                                    preview.toString()
                                } else "Interview Session",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
                Text(text = dateStr, color = TextGray, fontSize = 12.sp)
            }
            
            Surface(shape = CircleShape, color = borderColor) {
                Text(
                        text = scoreVal,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}


package com.example.interviewreadyai.feature.dashboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import coil.compose.rememberAsyncImagePainter
import com.example.interviewreadyai.core.ui.theme.*
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHeader(
        userName: String,
        userPhotoUrl: String?,
        profilePicPath: String?,
        scrollBehavior: TopAppBarScrollBehavior,
        hasUnreadNotifications: Boolean,
        onNotificationClick: () -> Unit
) {
    TopAppBar(
            title = {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                            modifier =
                                    Modifier.size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    Brush.linearGradient(
                                                            listOf(Purple40, AccentBlue)
                                                    ),
                                                    CircleShape
                                            )
                                            .border(
                                                    1.5.dp,
                                                    Color.White.copy(alpha = 0.2f),
                                                    CircleShape
                                            ),
                            contentAlignment = Alignment.Center
                    ) {
                        if (profilePicPath != null) {
                            Image(
                                    painter = rememberAsyncImagePainter(File(profilePicPath)),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else if (userPhotoUrl != null) {
                            Image(
                                    painter = rememberAsyncImagePainter(userPhotoUrl),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        val greeting =
                                when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                                    in 5..11 -> "Good Morning"
                                    in 12..16 -> "Good Afternoon"
                                    in 17..20 -> "Good Evening"
                                    else -> "Hello"
                                }

                        Text(
                                text = "$greeting, ${userName.split(" ").firstOrNull() ?: "User"}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color.White
                        )
                        Text(
                            text = "Ready for Placements",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            },
            actions = {
                IconButton(
                        onClick = onNotificationClick,
                        modifier =
                                Modifier.size(42.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Box {
                        Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                        )
                        if (hasUnreadNotifications) {
                            Box(
                                    modifier =
                                            Modifier.size(10.dp)
                                                    .background(Color.Red, CircleShape)
                                                    .border(2.dp, BackgroundDark, CircleShape)
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 1.dp, y = (-1).dp)
                            )
                        }
                    }
                }
            },
            colors =
                    TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                    ),
            modifier = Modifier,
            scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    userPhotoUrl: String?,
    profilePicPath: String?,
    onStartInterview: () -> Unit = {},
    recentSessions: List<com.example.interviewreadyai.data.local.InterviewSessionEntity> = emptyList(),
    onSessionClick: (Long) -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var hasUnreadNotifications by rememberSaveable { mutableStateOf(true) }

    val notifications = remember(recentSessions) {
        val list = mutableListOf<NotificationData>()
        if (recentSessions.isNotEmpty()) {
            val latest = recentSessions.first()
            val scoreFormat = String.format("%.1f/10", latest.overallScore)
            list.add(
                NotificationData(
                    "Interview Evaluated",
                    "You scored $scoreFormat on your last mock interview.",
                    "Latest"
                )
            )
            if (recentSessions.size > 1) {
                list.add(
                    NotificationData(
                        "Great Consistency!",
                        "You've completed ${recentSessions.size} interviews. Keep up the momentum!",
                        "Recent"
                    )
                )
            }
        } else {
            list.add(
                NotificationData(
                    "Welcome to Ready AI!",
                    "Start your first mock interview to get tailored feedback.",
                    "Just now"
                )
            )
        }
        list.add(
            NotificationData(
                "Profile Synced",
                "Your profile and settings are successfully synced to this device.",
                "System"
            )
        )
        list
    }

    val primaryColor = PrimaryBlue
    val purpleColor = SecondaryPurple

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
                        colors = listOf(
                            primaryColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ), center = Offset(0f, 0f), radius = 1000f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            purpleColor.copy(alpha = 0.1f),
                            Color.Transparent
                        ), center = Offset(1000f, 0f), radius = 1000f
                    )
                )
        )

        Scaffold(
            topBar = {
                DashboardHeader(
                    userName = userName,
                    userPhotoUrl = userPhotoUrl,
                    profilePicPath = profilePicPath,
                    scrollBehavior = scrollBehavior,
                    hasUnreadNotifications = hasUnreadNotifications,
                    onNotificationClick = { 
                        showNotificationsDialog = true 
                        hasUnreadNotifications = false
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {


            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                            PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
            item { PrecisionHeroCard(onStartInterview) }
            item { PrecisionWeeklyProgress(recentSessions, onNavigateToProgress) }
            item { PrecisionRecentSessions(recentSessions, onSessionClick, onSeeAllClick) }
        }
        } // closes inner Box
        } // closes Scaffold

        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                containerColor = CardDark,
                title = {
                    Text(text = "Notifications", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        notifications.forEach { notif ->
                            NotificationItem(
                                title = notif.title,
                                message = notif.message,
                                time = notif.time
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Close", color = AccentBlue)
                    }
                }
            )
        }
    } // closes Box
}

data class NotificationData(val title: String, val message: String, val time: String)

@Composable
fun NotificationItem(title: String, message: String, time: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = time, color = Color.Gray, fontSize = 11.sp)
        }
        Text(text = message, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun PrecisionHeroCard(onStartInterview: () -> Unit) {
    val meshGradient =
            Brush.linearGradient(
                    colors = listOf(AccentBlue, Purple40, NavyDark),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )

    Card(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(200.dp)
                            .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(28.dp)
                            ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(meshGradient)) {
            
            Box(
                    modifier =
                            Modifier.size(180.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 40.dp, y = (-40).dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            )
            Box(
                    modifier =
                            Modifier.size(120.dp)
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-30).dp, y = 30.dp)
                                    .background(AccentBlue.copy(alpha = 0.2f), CircleShape)
            )

            Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                                text = "AI CAREER COACH",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                            text = "Ready to Ace\nYour Next Interview?",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            lineHeight = 32.sp
                    )
                }

                Button(
                        onClick = onStartInterview,
                        modifier = Modifier.height(48.dp).graphicsLayer(shadowElevation = 8f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                                text = "Start Training",
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                        )
                        Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = BackgroundDark,
                                modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrecisionWeeklyProgress(
    sessions: List<com.example.interviewreadyai.data.local.InterviewSessionEntity>,
    onNavigateToProgress: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "Performance Status",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
            )
            Surface(
                    onClick = onNavigateToProgress,
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                        text = "Insights",
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.height(130.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val labels = listOf("M", "T", "W", "T", "F", "S", "S")

                    
                    val currentWeekSessions = BooleanArray(7) { false }
                    val scoresPerDay = FloatArray(7) { 0f }

                    val calendar = Calendar.getInstance()
                    
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    
                    if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        calendar.add(Calendar.DATE, (Calendar.MONDAY - calendar.get(Calendar.DAY_OF_WEEK) + 7) % 7)
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startOfWeekTime = calendar.timeInMillis

                    sessions.forEach { session ->
                        val sessionCal = Calendar.getInstance()
                        sessionCal.timeInMillis = session.timestamp

                        
                        if (session.timestamp >= startOfWeekTime && session.timestamp < startOfWeekTime + (7 * 24 * 60 * 60 * 1000L)) {
                            
                            var dayIndex = sessionCal.get(Calendar.DAY_OF_WEEK) - 2
                            if (dayIndex < 0) dayIndex = 6 // Sunday wraps to 6
                            if (dayIndex in 0..6) {
                                currentWeekSessions[dayIndex] = true
                                scoresPerDay[dayIndex] = maxOf(scoresPerDay[dayIndex], session.overallScore.toFloat())
                            }
                        }
                    }

                    labels.forEachIndexed { index, label ->
                        val hasSession = currentWeekSessions[index]
                        val barColor = if (hasSession) {
                            Brush.verticalGradient(listOf(AccentBlue, Purple40))
                        } else {
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.03f))
                            )
                        }

                        
                        val height = if (hasSession) {
                            0.4f + (0.06f * scoresPerDay[index]).coerceAtMost(0.6f) // Scale height up based on score
                        } else {
                            0.2f
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(height)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(barColor)
                            )
                            Text(
                                text = label,
                                color = if (hasSession) Color.White else Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrecisionRecentSessions(
        sessions: List<com.example.interviewreadyai.data.local.InterviewSessionEntity>,
        onSessionClick: (Long) -> Unit,
        onSeeAllClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "Recent Sessions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
            )
            Text(
                    text = "See All",
                    color = Purple40,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (sessions.isEmpty()) {
                Text(
                        text = "No recent sessions. Start an interview to see results here!",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                sessions.forEach { session ->
                    val dateFormatted =
                            java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                                    .format(java.util.Date(session.timestamp))

                    RecentSessionGlassCard(
                            title =
                                    if (session.targetRole.isNotEmpty()) session.targetRole
                                    else "Interview Session",
                            company = "Mock Interview",
                            date = dateFormatted,
                            score = String.format("%.1f/10", session.overallScore),
                            icon = Icons.Default.BarChart,
                            iconColor =
                                    if (session.overallScore >= 8) SuccessGreen
                                    else if (session.overallScore >= 6) WarningAmber
                                    else Color.Red,
                            onClick = { onSessionClick(session.id) }
                    )
                }
            }
        }
    }
}




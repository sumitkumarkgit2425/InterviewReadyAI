package com.example.interviewreadyai.feature.profile.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.interviewreadyai.core.ui.theme.*
import com.example.interviewreadyai.core.ui.navigation.Screen
import com.example.interviewreadyai.core.common.utils.NotificationHelper
import com.example.interviewreadyai.data.local.InterviewSessionEntity
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    userPhotoUrl: String?,
    analytics: com.example.interviewreadyai.data.repository.AnalyticsSummary?,
    sessionCount: Int,
    resumeText: String,
    resumeUri: String?,
    resumeFileName: String?,
    jdText: String,
    recentSessions: List<InterviewSessionEntity>,
    profilePicPath: String?,
    notificationsEnabled: Boolean,
    isBiometricEnabled: Boolean,
    onProfilePicSelected: (Uri) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onBiometricToggled: (Boolean) -> Unit,
    onUploadResume: (Uri) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetType by remember { mutableStateOf("none") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onProfilePicSelected(it) }
    }

    val resumePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onUploadResume(it) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.scheduleBehavioralNudges(context)
        }
        onNotificationsToggled(isGranted)
    }

    val handleNotificationToggle = { enabled: Boolean ->
        if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationHelper.createNotificationChannel(context)
                NotificationHelper.scheduleBehavioralNudges(context)
                onNotificationsToggled(true)
            }
        } else {
            if (enabled) {
                NotificationHelper.createNotificationChannel(context)
                NotificationHelper.scheduleBehavioralNudges(context)
            }
            onNotificationsToggled(enabled)
        }
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

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "User Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(48.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    ProfileInfoHeader(
                        userName = userName,
                        userEmail = userEmail,
                        userPhotoUrl = userPhotoUrl,
                        profilePicPath = profilePicPath,
                        onEditPhoto = { imagePickerLauncher.launch("image/*") }
                    )
                }

                item {
                    GrowthSummarySection(
                        analytics = analytics,
                        sessionCount = sessionCount,
                        onHistoryClick = onNavigateToHistory
                    )
                }

                item {
                    CareerDocumentsSection(
                        hasResume = resumeUri != null,
                        resumeFileName = resumeFileName,
                        onResumeClick = {
                            resumeUri?.let {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(it), "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open Resume"))
                                } catch (e: Exception) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.parse(it), "*/*")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Open Resume"))
                                    } catch (e2: Exception) {
                                        Toast.makeText(context, "No app to open this file", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onUpdateResume = { resumePickerLauncher.launch("application/pdf") },
                        onSavedJDsClick = {
                            bottomSheetType = "jd"
                            showBottomSheet = true
                        }
                    )
                }

                item {
                    AccountSettingsSection(
                        notificationsEnabled = notificationsEnabled,
                        isBiometricEnabled = isBiometricEnabled,
                        onNotificationsToggled = handleNotificationToggle,
                        onBiometricToggled = onBiometricToggled,
                        onItemClick = { type ->
                            bottomSheetType = type
                            showBottomSheet = true
                        }
                    )
                }

                item {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("Sign Out", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = CardDark,
                contentColor = Color.White
            ) {
                ProfileBottomSheetContent(
                    type = bottomSheetType,
                    jdText = jdText,
                    recentSessions = recentSessions,
                    onDismiss = {
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileInfoHeader(
    userName: String,
    userEmail: String,
    userPhotoUrl: String?,
    profilePicPath: String?,
    onEditPhoto: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clickable { onEditPhoto() }
                .drawBehind {
                    drawCircle(
                        color = Purple40.copy(alpha = 0.5f),
                        radius = size.minDimension / 2 + 4.dp.toPx(),
                        style = Stroke(width = 4.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp).clip(CircleShape),
                color = CardDark
            ) {
                if (profilePicPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(File(profilePicPath)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (userPhotoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(userPhotoUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.fillMaxSize().padding(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(userName, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(userEmail, fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

        Row(
            modifier = Modifier.padding(top = 8.dp).clickable { onEditPhoto() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Edit Profile Photo ", color = Purple40, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(Icons.Default.Edit, contentDescription = null, tint = Purple40, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun GrowthSummarySection(analytics: com.example.interviewreadyai.data.repository.AnalyticsSummary?, sessionCount: Int, onHistoryClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timeline, contentDescription = null, tint = Purple40, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Growth Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val avgScore = analytics?.let { (it.masteryClarity + it.masteryConfidence + it.masteryTechnical) / 30.0 } ?: 0.0
            GrowthCard("INTERVIEWS", sessionCount.toString(), null, Icons.Default.Star, Modifier.weight(1f).clickable { onHistoryClick() })
            GrowthCard("AVG SCORE", String.format("%.1f", avgScore), "+2%", Icons.Default.Cyclone, Modifier.weight(1f))
            GrowthCard("PEAK", "9.1", null, Icons.Default.EmojiEvents, Modifier.weight(1f))
        }
    }
}

@Composable
fun GrowthCard(label: String, value: String, subValue: String? = null, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (label == "PEAK") Color(0xFFFFD700) else Purple40, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (subValue != null) {
                    Text(" $subValue", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CareerDocumentsSection(hasResume: Boolean, resumeFileName: String?, onResumeClick: () -> Unit, onUpdateResume: () -> Unit, onSavedJDsClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your Career Documents", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        DocumentCard(
            title = if (hasResume) "Primary Resume" else "No Resume Uploaded",
            subtitle = if (hasResume) resumeFileName ?: "Resume uploaded" else "Start a mock interview to upload",
            actionText = if (hasResume) "Update" else "Upload",
            icon = Icons.Default.PictureAsPdf,
            onClick = onResumeClick,
            onActionClick = onUpdateResume
        )
        DocumentCard(
            title = "Saved JDs",
            subtitle = "Recent practice requirements",
            actionText = "View",
            icon = Icons.Default.BusinessCenter,
            showArrow = true,
            onClick = onSavedJDsClick,
            onActionClick = onSavedJDsClick
        )
    }
}

@Composable
fun DocumentCard(title: String, subtitle: String, actionText: String, icon: ImageVector, showArrow: Boolean = false, onClick: () -> Unit, onActionClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = CardDark.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, Purple40.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(actionText, color = Purple40, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (showArrow) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Purple40, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSettingsSection(
    notificationsEnabled: Boolean,
    isBiometricEnabled: Boolean,
    onNotificationsToggled: (Boolean) -> Unit,
    onBiometricToggled: (Boolean) -> Unit,
    onItemClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Account & Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Notification Preferences",
            subtitle = "Manage mock interview reminders",
            glowColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
            showSwitch = true,
            switchValue = notificationsEnabled,
            onSwitchToggle = onNotificationsToggled,
            onClick = {}
        )
        SettingsItem(
            icon = Icons.Default.Fingerprint,
            title = "App Lock",
            subtitle = "Require biometric scan on app launch",
            glowColor = Color(0xFF10B981).copy(alpha = 0.2f),
            showSwitch = true,
            switchValue = isBiometricEnabled,
            onSwitchToggle = onBiometricToggled,
            onClick = {}
        )
        SettingsItem(
            icon = Icons.Default.Terminal,
            title = "Tech Stack & Credits",
            subtitle = "Gemini AI, Room DB, Jetpack Compose",
            glowColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
            onClick = { onItemClick("tech") }
        )
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.Help,
            title = "Help & Feedback",
            subtitle = "Report bugs or suggest features",
            glowColor = Color(0xFF10B981).copy(alpha = 0.2f),
            onClick = { onItemClick("help") }
        )
        SettingsItem(
            icon = Icons.Default.Info,
            title = "About InterviewReady AI",
            subtitle = "Version 1.0.2 - Made by Sumit",
            glowColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
            onClick = { onItemClick("about") }
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    glowColor: Color,
    showSwitch: Boolean = false,
    switchValue: Boolean = false,
    onSwitchToggle: (Boolean) -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).drawBehind { drawCircle(color = glowColor) },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        if (showSwitch) {
            Switch(
                checked = switchValue,
                onCheckedChange = onSwitchToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Purple40,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ProfileBottomSheetContent(type: String, jdText: String, recentSessions: List<InterviewSessionEntity>, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (type) {
            "tech" -> {
                Text("Tech Stack", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("• Google Gemini AI (Interview Logic)\n• Room Database (Persistence)\n• Jetpack Compose (UI)\n• Kotlin Coroutines (Async Ops)\n• Material 3 (Design System)", color = Color.Gray)
            }
            "help" -> {
                Text("Help & Feedback", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Facing an issue? Contact our support at support@interviewready.ai or visit our GitHub repository to report a bug.", color = Color.Gray)
            }
            "about" -> {
                Text("About InterviewReady AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("InterviewReady AI is a premium mock interview assistant designed to help students and developers ace their dream jobs with AI-driven feedback.", color = Color.Gray)
            }
            "jd" -> {
                Text("Saved JD History", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                val allJDs = remember(jdText, recentSessions) {
                    val list = mutableListOf<String>()
                    if (jdText.isNotEmpty()) list.add(jdText)
                    recentSessions.forEach {
                        if (it.jdText.isNotEmpty() && it.jdText !in list) {
                            list.add(it.jdText)
                        }
                    }
                    list
                }
                if (allJDs.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allJDs.size) { index ->
                            val jd = allJDs[index]
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "JD #${allJDs.size - index}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple40,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(jd, color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    Text("No JD uploaded yet. Start a practice session to save a JD.", color = Color.Gray)
                }
            }
        }
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text("Close")
        }
    }
}

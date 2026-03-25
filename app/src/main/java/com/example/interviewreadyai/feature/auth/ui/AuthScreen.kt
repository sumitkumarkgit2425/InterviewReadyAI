package com.example.interviewreadyai.feature.auth.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interviewreadyai.R
import com.example.interviewreadyai.core.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuthScreen(
    autoTriggerBiometric: Boolean = false,
    onSignInSuccess: () -> Unit,
    onBiometricClick: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "auth_ambient")

    

    
    val blob1X by infiniteTransition.animateFloat(
        initialValue = -60f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1X"
    )
    val blob1Y by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = -80f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1Y"
    )

    
    val blob2X by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2X"
    )
    val blob2Y by infiniteTransition.animateFloat(
        initialValue = 160f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2Y"
    )

    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(400),
        label = "logoAlpha"
    )
    val brandAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500, delayMillis = 300),
        label = "brandAlpha"
    )
    val brandTranslateY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 16f,
        animationSpec = tween(550, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "brandY"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500, delayMillis = 600),
        label = "cardAlpha"
    )
    val cardTranslateY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 30f,
        animationSpec = tween(600, delayMillis = 600, easing = FastOutSlowInEasing),
        label = "cardY"
    )
    val footerAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(400, delayMillis = 900),
        label = "footerAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }
    
    LaunchedEffect(autoTriggerBiometric) {
        if (autoTriggerBiometric) {
            onBiometricClick()
        }
    }

    val accentBlue = PrimaryBlue
    val accentPurple = SecondaryPurple

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF060B14),
                        Color(0xFF030508)
                    )
                )
            )
    ) {
        

        Canvas(modifier = Modifier.fillMaxSize()) {
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentBlue.copy(alpha = 0.12f),
                        accentBlue.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(
                        size.width * 0.2f + blob1X.dp.toPx(),
                        size.height * 0.15f + blob1Y.dp.toPx()
                    ),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(
                    size.width * 0.2f + blob1X.dp.toPx(),
                    size.height * 0.15f + blob1Y.dp.toPx()
                )
            )

            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentPurple.copy(alpha = 0.10f),
                        accentPurple.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(
                        size.width * 0.8f + blob2X.dp.toPx(),
                        size.height * 0.75f + blob2Y.dp.toPx()
                    ),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(
                    size.width * 0.8f + blob2X.dp.toPx(),
                    size.height * 0.75f + blob2Y.dp.toPx()
                )
            )

            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.4f),
                    radius = size.width * 0.3f
                ),
                radius = size.width * 0.3f,
                center = Offset(size.width * 0.5f, size.height * 0.4f)
            )
        }

        

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentBlue.copy(alpha = 0.20f * pulseAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                
                Canvas(modifier = Modifier.size(150.dp)) {
                    val radius = size.minDimension / 2f - 4f
                    val sw = 1.5f.dp.toPx()

                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius,
                        style = Stroke(width = sw)
                    )

                    
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                accentBlue.copy(alpha = pulseAlpha * 0.3f),
                                accentBlue.copy(alpha = pulseAlpha * 0.8f),
                                accentBlue.copy(alpha = pulseAlpha * 0.3f),
                                Color.Transparent,
                            )
                        ),
                        startAngle = orbitRotation,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )

                    
                    val dotOffsets = listOf(0f, 90f, 180f, 270f)
                    val dotBrightness = listOf(1.0f, 0.65f, 0.40f, 0.65f) // leading → trailing fade
                    dotOffsets.forEachIndexed { i, offset ->
                        val dRad = (orbitRotation + 240f + offset) * PI.toFloat() / 180f
                        val dX = center.x + radius * cos(dRad)
                        val dY = center.y + radius * sin(dRad)
                        val brightness = dotBrightness[i]
                        
                        drawCircle(
                            color = accentBlue.copy(alpha = pulseAlpha * 0.25f * brightness),
                            radius = 8.dp.toPx(),
                            center = Offset(dX, dY)
                        )
                        
                        drawCircle(
                            color = accentBlue.copy(alpha = pulseAlpha * brightness),
                            radius = 3.5f.dp.toPx(),
                            center = Offset(dX, dY)
                        )
                    }
                }

                
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "InterviewReady AI Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            
            Text(
                text = "InterviewReady AI",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(brandAlpha)
                    .graphicsLayer { translationY = brandTranslateY }
            )

            Spacer(modifier = Modifier.weight(1f))

            
            Surface(
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                ),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlpha)
                    .graphicsLayer { translationY = cardTranslateY }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Welcome Back",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Secure your interview preparation\nwith premium AI tools.",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    
                    Button(
                        onClick = onSignInSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF4285F4)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Sign in with Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    
                    if (autoTriggerBiometric) {
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )
                            Text(
                                text = "  OR  ",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )
                        }

                        
                        OutlinedButton(
                            onClick = onBiometricClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                accentPurple.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = accentPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Use Biometric",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Text(
                            text = "Secure your preparation with biometric auth",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            
            Text(
                text = "Privacy First • Local Data Processing",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .alpha(footerAlpha)
                    .padding(bottom = 20.dp)
            )
        }
    }
}


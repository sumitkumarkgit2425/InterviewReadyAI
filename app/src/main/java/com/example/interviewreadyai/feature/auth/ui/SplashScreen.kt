package com.example.interviewreadyai.feature.auth.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interviewreadyai.R
import com.example.interviewreadyai.core.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin










@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    

    
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    

    
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.0f,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    
    val ringAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(600, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "ringAlpha"
    )

    
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )
    val titleTranslateY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 18f,
        animationSpec = tween(550, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "titleY"
    )

    
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(450, delayMillis = 950, easing = FastOutSlowInEasing),
        label = "subtitleAlpha"
    )
    val subtitleTranslateY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 14f,
        animationSpec = tween(500, delayMillis = 950, easing = FastOutSlowInEasing),
        label = "subtitleY"
    )

    
    val footerAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(400, delayMillis = 1200),
        label = "footerAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        
        Box(
            modifier = Modifier
                .size(480.dp)
                .offset(x = (-60).dp, y = (-140).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(360.dp)
                .offset(x = 110.dp, y = 180.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SecondaryPurple.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(210.dp)
            ) {
                
                Canvas(
                    modifier = Modifier
                        .size(210.dp)
                        .alpha(ringAlpha)
                ) {
                    val radius = size.minDimension / 2f - 6f
                    val sw = 2.dp.toPx()

                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = radius,
                        style = Stroke(width = sw)
                    )

                    
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                PrimaryBlue.copy(alpha = pulseAlpha * 0.35f),
                                SecondaryPurple.copy(alpha = pulseAlpha),
                                PrimaryBlue.copy(alpha = pulseAlpha * 0.35f),
                                Color.Transparent,
                            )
                        ),
                        startAngle = orbitRotation,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )

                    
                    val dotOffsets = listOf(0f, 90f, 180f, 270f)
                    val dotBrightness = listOf(1.0f, 0.65f, 0.40f, 0.65f) // leading → trailing
                    dotOffsets.forEachIndexed { i, offset ->
                        val dRad = (orbitRotation + 270f + offset) * PI.toFloat() / 180f
                        val dX = center.x + radius * cos(dRad)
                        val dY = center.y + radius * sin(dRad)
                        val brightness = dotBrightness[i]
                        
                        drawCircle(
                            color = SecondaryPurple.copy(alpha = pulseAlpha * 0.22f * brightness),
                            radius = 10.dp.toPx(),
                            center = Offset(dX, dY)
                        )
                        
                        drawCircle(
                            color = PrimaryBlue.copy(alpha = pulseAlpha * brightness),
                            radius = 4.5f.dp.toPx(),
                            center = Offset(dX, dY)
                        )
                    }
                }

                
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    
                    Box(
                        modifier = Modifier
                            .size(145.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryBlue.copy(alpha = 0.28f * pulseAlpha),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(108.dp)
                            .clip(RoundedCornerShape(26.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            
            Text(
                text = "InterviewReady AI",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .alpha(titleAlpha)
                    .offset(y = titleTranslateY.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            
            Text(
                text = "YOUR AI-POWERED CAREER COACH",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.8.sp,
                modifier = Modifier
                    .alpha(subtitleAlpha)
                    .offset(y = subtitleTranslateY.dp)
            )
        }

        
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(footerAlpha),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(0, 350, 700).forEach { delayMs ->
                val dotScale by infiniteTransition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = delayMs, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$delayMs"
                )
                val dotAlpha = 0.5f + dotScale * 0.5f
                Box(
                    modifier = Modifier
                        .size(5.5f.dp)
                        .scale(dotScale)
                        .background(
                            PrimaryBlue.copy(alpha = dotAlpha),
                            CircleShape
                        )
                )
            }
        }
    }
}


package com.example.interviewreadyai.core.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.interviewreadyai.core.auth.AuthManager
import com.example.interviewreadyai.feature.auth.ui.AuthScreen
import com.example.interviewreadyai.feature.auth.ui.SplashScreen
import com.example.interviewreadyai.feature.dashboard.ui.DashboardScreen
import com.example.interviewreadyai.feature.dashboard.ui.RecentSessionsScreen
import com.example.interviewreadyai.feature.interview.ui.InterviewSummaryScreen
import com.example.interviewreadyai.feature.interview.ui.MockInterviewScreen
import com.example.interviewreadyai.feature.interview.ui.ProgressTrackerScreen
import com.example.interviewreadyai.feature.interview.ui.QuestionDetailScreen
import com.example.interviewreadyai.feature.interview.viewmodel.InterviewState
import com.example.interviewreadyai.feature.interview.viewmodel.InterviewViewModel
import com.example.interviewreadyai.feature.practice.ui.MatchResultScreen
import com.example.interviewreadyai.feature.practice.ui.PracticeScreen
import com.example.interviewreadyai.feature.profile.ui.ProfileScreen
import kotlinx.coroutines.launch

private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
private const val TRANSITION_DURATION = 350

@Composable
fun NavGraph(
    navController: NavHostController,
    sharedViewModel: InterviewViewModel,
    authManager: AuthManager,
    activity: androidx.fragment.app.FragmentActivity
) {
    val scope = rememberCoroutineScope()
    val isLoggedIn by sharedViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            val topLevelRoutes = listOf(
                Screen.Dashboard.route,
                Screen.Practice.route,
                Screen.ProgressTracker.route,
                Screen.Profile.route
            )
            val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
            val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
            val isTabSwitch = initialRoute in topLevelRoutes && targetRoute in topLevelRoutes

            slideInHorizontally(
                initialOffsetX = { fullWidth ->
                    if (isTabSwitch) (fullWidth * 0.15).toInt()
                    else (fullWidth * 0.25).toInt()
                },
                animationSpec = tween(
                    if (isTabSwitch) 300 else TRANSITION_DURATION,
                    easing = EmphasizedDecelerate
                )
            ) + fadeIn(animationSpec = tween(if (isTabSwitch) 200 else 250))
        },
        exitTransition = {
            val topLevelRoutes = listOf(
                Screen.Dashboard.route,
                Screen.Practice.route,
                Screen.ProgressTracker.route,
                Screen.Profile.route
            )
            val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
            val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
            val isTabSwitch = initialRoute in topLevelRoutes && targetRoute in topLevelRoutes

            slideOutHorizontally(
                targetOffsetX = { fullWidth ->
                    if (isTabSwitch) -(fullWidth * 0.15).toInt()
                    else -(fullWidth * 0.10).toInt()
                },
                animationSpec = tween(
                    if (isTabSwitch) 300 else TRANSITION_DURATION,
                    easing = EmphasizedAccelerate
                )
            ) + fadeOut(animationSpec = tween(if (isTabSwitch) 200 else 200))
        },
        popEnterTransition = {
            val topLevelRoutes = listOf(
                Screen.Dashboard.route,
                Screen.Practice.route,
                Screen.ProgressTracker.route,
                Screen.Profile.route
            )
            val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
            val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
            val isTabSwitch = initialRoute in topLevelRoutes && targetRoute in topLevelRoutes

            slideInHorizontally(
                initialOffsetX = { fullWidth ->
                    if (isTabSwitch) -(fullWidth * 0.15).toInt()
                    else -(fullWidth * 0.10).toInt()
                },
                animationSpec = tween(
                    if (isTabSwitch) 300 else TRANSITION_DURATION,
                    easing = EmphasizedDecelerate
                )
            ) + fadeIn(animationSpec = tween(if (isTabSwitch) 200 else 250))
        },
        popExitTransition = {
            val topLevelRoutes = listOf(
                Screen.Dashboard.route,
                Screen.Practice.route,
                Screen.ProgressTracker.route,
                Screen.Profile.route
            )
            val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
            val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
            val isTabSwitch = initialRoute in topLevelRoutes && targetRoute in topLevelRoutes

            slideOutHorizontally(
                targetOffsetX = { fullWidth ->
                    if (isTabSwitch) (fullWidth * 0.15).toInt()
                    else (fullWidth * 0.25).toInt()
                },
                animationSpec = tween(
                    if (isTabSwitch) 300 else TRANSITION_DURATION,
                    easing = EmphasizedAccelerate
                )
            ) + fadeOut(animationSpec = tween(if (isTabSwitch) 200 else 250))
        }
    ) {
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = {
                fadeOut(animationSpec = tween(500, easing = EmphasizedAccelerate)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(500, easing = EmphasizedAccelerate))
            }
        ) {
            SplashScreen(
                onSplashFinished = {
                    val isBioEnabled = sharedViewModel.isBiometricEnabled.value
                    val destination = if (isLoggedIn) {
                        if (isBioEnabled && authManager.isBiometricAvailable()) {
                            Screen.Auth.createRoute(autoBiometric = true)
                        } else {
                            Screen.Dashboard.route
                        }
                    } else {
                        Screen.Auth.createRoute(autoBiometric = false)
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Screen.Auth.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400, easing = EmphasizedDecelerate)) +
                scaleIn(initialScale = 0.92f, animationSpec = tween(400, easing = EmphasizedDecelerate))
            }
        ) { backStackEntry ->
            val autoBiometric = backStackEntry.arguments?.getString("autoBiometric")?.toBoolean() ?: false
            AuthScreen(
                autoTriggerBiometric = autoBiometric,
                onSignInSuccess = {
                    scope.launch {
                        val profile = authManager.signInWithGoogle(activity)
                        if (profile != null) {
                            sharedViewModel.updateUserProfile(profile)
                            sharedViewModel.setLoggedIn(true)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Auth.createRoute(autoBiometric)) { inclusive = true }
                            }
                        }
                    }
                },
                onBiometricClick = {
                    authManager.showBiometricPrompt(
                        activity = activity,
                        onSuccess = {
                            sharedViewModel.setLoggedIn(true)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Auth.createRoute(autoBiometric)) { inclusive = true }
                            }
                        },
                        onError = {  }
                    )
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val sessionsState = sharedViewModel.recentSessions.collectAsState()
            val userName = sharedViewModel.userName.collectAsState()
            val userPhotoUrl = sharedViewModel.userPhotoUrl.collectAsState()
            val profilePicPath = sharedViewModel.profilePicPath.collectAsState()

            DashboardScreen(
                userName = userName.value,
                userPhotoUrl = userPhotoUrl.value,
                profilePicPath = profilePicPath.value,
                onStartInterview = { navController.navigate(Screen.Practice.route) },
                recentSessions = sessionsState.value,
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.InterviewReport.createRoute(sessionId))
                },
                onNavigateToProgress = { navController.navigate(Screen.ProgressTracker.route) },
                onSeeAllClick = { navController.navigate(Screen.RecentSessions.route) }
            )
        }

        composable(Screen.RecentSessions.route) {
            val sessionsState = sharedViewModel.recentSessions.collectAsState()
            RecentSessionsScreen(
                sessions = sessionsState.value,
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.InterviewReport.createRoute(sessionId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Practice.route) {
            PracticeScreen(
                viewModel = sharedViewModel,
                onNavigateToInterview = { resume, jd, role ->
                    sharedViewModel.resumeText = resume
                    sharedViewModel.jdText = jd
                    sharedViewModel.targetRole = role
                    navController.navigate(Screen.MockInterview.route)
                },
                onNavigateToMatchResult = { navController.navigate(Screen.MatchResult.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MatchResult.route) {
            val matchResult by sharedViewModel.lastMatchResult.collectAsState()
            matchResult?.let { result ->
                MatchResultScreen(
                    matchResult = result,
                    targetRole = sharedViewModel.targetRole,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.ProgressTracker.route) {
            val analyticsState = sharedViewModel.analytics.collectAsState()
            val sessionsState = sharedViewModel.recentSessions.collectAsState()

            ProgressTrackerScreen(
                analytics = analyticsState.value,
                recentSessions = sessionsState.value,
                onNavigateBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.InterviewReport.createRoute(sessionId))
                }
            )
        }

        composable(Screen.Profile.route) {
            val analyticsState = sharedViewModel.analytics.collectAsState()
            val sessionsState = sharedViewModel.recentSessions.collectAsState()
            val countState = sharedViewModel.sessionCount.collectAsState()
            val profilePicPath = sharedViewModel.profilePicPath.collectAsState()
            val notificationsEnabled = sharedViewModel.notificationsEnabled.collectAsState()
            val biometricEnabled = sharedViewModel.isBiometricEnabled.collectAsState()
            val userName = sharedViewModel.userName.collectAsState()
            val userEmail = sharedViewModel.userEmail.collectAsState()
            val userPhotoUrl = sharedViewModel.userPhotoUrl.collectAsState()
            val resumeUri = sharedViewModel.resumeUri.collectAsState()
            val resumeFileName = sharedViewModel.resumeFileName.collectAsState()

            ProfileScreen(
                userName = userName.value,
                userEmail = userEmail.value,
                userPhotoUrl = userPhotoUrl.value,
                analytics = analyticsState.value,
                sessionCount = countState.value,
                resumeText = sharedViewModel.resumeText,
                resumeUri = resumeUri.value,
                resumeFileName = resumeFileName.value,
                jdText = sharedViewModel.jdText,
                recentSessions = sessionsState.value,
                profilePicPath = profilePicPath.value,
                notificationsEnabled = notificationsEnabled.value,
                isBiometricEnabled = biometricEnabled.value,
                onProfilePicSelected = { uri -> sharedViewModel.saveProfilePicture(uri) },
                onNotificationsToggled = { enabled -> sharedViewModel.toggleNotifications(enabled) },
                onBiometricToggled = { enabled -> sharedViewModel.toggleBiometric(enabled) },
                onUploadResume = { uri ->
                    try {
                        activity.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val (name, _) = com.example.interviewreadyai.core.common.utils.FileUtils.getFileNameAndSize(activity, uri)
                    sharedViewModel.parseResumeAndSave(uri, name)
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(Screen.ProgressTracker.route) },
                onLogout = {
                    scope.launch {
                        authManager.signOut()
                        sharedViewModel.logout()
                        navController.navigate(Screen.Auth.createRoute(autoBiometric = false)) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.MockInterview.route) {
            MockInterviewScreen(
                resumeText = sharedViewModel.resumeText,
                jdText = sharedViewModel.jdText,
                viewModel = sharedViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSummary = {
                    navController.navigate(Screen.InterviewSummary.route) {
                        popUpTo(Screen.MockInterview.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.InterviewReport.route) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull() ?: -1L
            LaunchedEffect(sessionId) {
                if (sessionId != -1L) {
                    sharedViewModel.loadHistoricalSession(sessionId)
                }
            }

            val state = sharedViewModel.uiState.collectAsState()
            when (val currentState = state.value) {
                is InterviewState.Finished -> {
                    InterviewSummaryScreen(
                        evaluations = currentState.evaluations,
                        performanceSummary = currentState.performanceSummary,
                        metricDeltas = currentState.metricDeltas,
                        onQuestionClick = { index ->
                            navController.navigate(Screen.QuestionDetail.createRoute(index))
                        },
                        onTakeAnotherInterview = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                is InterviewState.Evaluating -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is InterviewState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = currentState.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                }
                else -> {}
            }
        }

        composable(Screen.InterviewSummary.route) {
            val state = sharedViewModel.uiState.collectAsState()
            val finishedState = state.value as? InterviewState.Finished
            val evaluations = finishedState?.evaluations ?: emptyList()
            val perfSummary = finishedState?.performanceSummary ?: ""
            val deltas = finishedState?.metricDeltas

            InterviewSummaryScreen(
                evaluations = evaluations,
                performanceSummary = perfSummary,
                metricDeltas = deltas,
                onQuestionClick = { index ->
                    navController.navigate(Screen.QuestionDetail.createRoute(index))
                },
                onTakeAnotherInterview = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QuestionDetail.route) { backStackEntry ->
            val state = sharedViewModel.uiState.collectAsState()
            val finishedState = state.value as? InterviewState.Finished
            val evaluations = finishedState?.evaluations ?: emptyList()
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0

            if (index < evaluations.size) {
                val evaluation = evaluations[index]
                QuestionDetailScreen(
                    evaluation = evaluation,
                    questionIndex = index + 1,
                    totalQuestions = evaluations.size,
                    onNavigateBack = { navController.popBackStack() },
                    onNextQuestion = if (index + 1 < evaluations.size) {
                        {
                            navController.navigate(Screen.QuestionDetail.createRoute(index + 1)) {
                                popUpTo(Screen.QuestionDetail.createRoute(index)) { inclusive = true }
                            }
                        }
                    } else null
                )
            }
        }
    }
}


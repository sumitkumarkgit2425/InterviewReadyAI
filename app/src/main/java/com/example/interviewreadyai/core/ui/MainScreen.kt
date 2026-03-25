package com.example.interviewreadyai.core.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.interviewreadyai.core.auth.AuthManager
import com.example.interviewreadyai.core.ui.designsystem.component.ReadyAiBottomNavigation
import com.example.interviewreadyai.core.ui.navigation.NavGraph
import com.example.interviewreadyai.core.ui.navigation.Screen
import com.example.interviewreadyai.feature.interview.viewmodel.InterviewViewModel

@Composable
fun MainScreen(
    activity: FragmentActivity,
    authManager: AuthManager
) {
    val navController = rememberNavController()
    val sharedViewModel: InterviewViewModel = viewModel()
    val context = LocalContext.current
    var lastBackPressTime by
            androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(0L)
            }

    
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute =
            currentBackStackEntry
                    ?.destination
                    ?.route
                    ?.substringBefore("?")
                    ?.substringBefore("/")
    val mainScreens = listOf(
        Screen.Dashboard.route,
        Screen.Practice.route,
        Screen.ProgressTracker.route,
        Screen.Profile.route
    )

    val isLoggedIn by sharedViewModel.isLoggedIn.collectAsState()

    
    BackHandler(enabled = isLoggedIn) {
        when {
            currentRoute != Screen.Dashboard.route && currentRoute in mainScreens -> {
                
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            currentRoute == Screen.Dashboard.route -> {
                
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000) {
                    activity.finish()
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(
                                    context,
                                    "Press back again to exit",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }
            else -> {
                
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentRoute in mainScreens && isLoggedIn) {
                    ReadyAiBottomNavigation(
                            selectedIndex =
                                    when (currentRoute) {
                                        Screen.Dashboard.route -> 0
                                        Screen.Practice.route -> 1
                                        Screen.ProgressTracker.route -> 2
                                        Screen.Profile.route -> 3
                                        else -> 0
                                    },
                            onItemSelected = { index ->
                                val targetScreen = when (index) {
                                    0 -> Screen.Dashboard
                                    1 -> Screen.Practice
                                    2 -> Screen.ProgressTracker
                                    3 -> Screen.Profile
                                    else -> Screen.Dashboard
                                }
                                
                                navController.navigate(targetScreen.route) {
                                    popUpTo(Screen.Dashboard.route) {
                                        if (index != 0) saveState = true
                                        else inclusive = true
                                    }
                                    launchSingleTop = true
                                    if (index != 0) restoreState = true
                                }
                            }
                    )
                }
            }
    ) { innerPadding ->
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavGraph(
                navController = navController,
                sharedViewModel = sharedViewModel,
                authManager = authManager,
                activity = activity
            )
        }
    }
}


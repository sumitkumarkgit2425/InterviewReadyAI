package com.example.interviewreadyai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.interviewreadyai.core.auth.AuthManager
import com.example.interviewreadyai.core.ui.theme.InterviewReadyAITheme
import com.example.interviewreadyai.core.ui.MainScreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.interviewreadyai.core.common.utils.NotificationHelper

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleBehavioralNudges(this)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        enableEdgeToEdge()
        val authManager = AuthManager(this)
        setContent {
            InterviewReadyAITheme {
                com.example.interviewreadyai.core.ui.MainScreen(
                    activity = this,
                    authManager = authManager
                )
            }
        }
    }
}


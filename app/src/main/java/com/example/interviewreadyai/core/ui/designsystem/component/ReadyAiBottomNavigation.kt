package com.example.interviewreadyai.core.ui.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interviewreadyai.core.ui.theme.BackgroundDark
import com.example.interviewreadyai.core.ui.theme.Purple40

@Composable
fun ReadyAiBottomNavigation(
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    Surface(
        color = Color(0xFF0D1117).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "HOME",
                isSelected = selectedIndex == 0,
                modifier = Modifier.weight(1f),
                onClick = { onItemSelected(0) }
            )
            BottomNavItem(
                icon = Icons.Default.School,
                label = "PRACTICE",
                isSelected = selectedIndex == 1,
                modifier = Modifier.weight(1f),
                onClick = { onItemSelected(1) }
            )
            BottomNavItem(
                icon = Icons.Default.BarChart,
                label = "ANALYTICS",
                isSelected = selectedIndex == 2,
                modifier = Modifier.weight(1f),
                onClick = { onItemSelected(2) }
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "PROFILE",
                isSelected = selectedIndex == 3,
                modifier = Modifier.weight(1f),
                onClick = { onItemSelected(3) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, 
            contentDescription = label, 
            tint = if (isSelected) Purple40 else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Purple40 else Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


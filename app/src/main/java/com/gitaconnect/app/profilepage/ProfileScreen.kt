package com.gitaconnect.app.profilepage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    ProfileBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // App Title / Heading
            Text(
                text = "GitaConnect",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Profile Header / Card
            ProfileHeaderCard(userProfile = userProfile)

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuItemCard(
                    title = "User Profile",
                    icon = Icons.Default.Person,
                    onClick = { viewModel.navigateTo(Screen.PERSONAL_INFO) }
                )

                MenuItemCard(
                    title = "Liked Verses",
                    icon = Icons.Default.Favorite,
                    onClick = { viewModel.navigateTo(Screen.LIKED) }
                )

                MenuItemCard(
                    title = "Spiritual Stats",
                    icon = Icons.Default.Star, // Representing stats/XP achievements
                    onClick = { viewModel.navigateTo(Screen.STATS) }
                )

                MenuItemCard(
                    title = "Notifications & Reminders",
                    icon = Icons.Default.Notifications,
                    onClick = { viewModel.navigateTo(Screen.NOTIFICATIONS) }
                )

                MenuItemCard(
                    title = "Accessibility Settings",
                    icon = Icons.Default.Settings,
                    onClick = { viewModel.navigateTo(Screen.ACCESSIBILITY) }
                )

                MenuItemCard(
                    title = "About GitaConnect",
                    icon = Icons.Default.Info,
                    onClick = { viewModel.navigateTo(Screen.ABOUT) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuItemCard(
                    title = "Log Out",
                    icon = Icons.Default.ExitToApp,
                    isDestructive = true,
                    onClick = { viewModel.logout() }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileHeaderCard(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    val initials = userProfile?.name?.let { getInitials(it) } ?: "GU"
    val displayName = userProfile?.name ?: "Guest User"
    val xp = userProfile?.totalXP ?: 0
    val levelText = if (userProfile != null) {
        getLevelText(xp)
    } else {
        "Log in to track your spiritual journey"
    }

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(GoldGradient)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = displayName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Level / XP
            Text(
                text = levelText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (userProfile != null) GoldAccent else TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getInitials(name: String): String {
    return name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.take(1) }
        .joinToString("")
        .uppercase()
}

private fun getLevelText(xp: Int): String {
    val lvl = when {
        xp <= 500 -> 1 to "Novice Seeker"
        xp <= 1000 -> 2 to "Dedicated Student"
        xp <= 2000 -> 3 to "Spiritual Voyager"
        xp <= 3000 -> 4 to "Enlightened Thinker"
        else -> 5 to "Gita Master"
    }
    return "Level ${lvl.first} - ${lvl.second} ($xp XP)"
}

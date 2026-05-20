package com.gitaconnect.app.profilepage

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val isBoldAndContrastEnabled by viewModel.isBoldAndContrastEnabled.collectAsState()
    val isDyslexiaFontEnabled by viewModel.isDyslexiaFontEnabled.collectAsState()
    val isKeepScreenAwakeEnabled by viewModel.isKeepScreenAwakeEnabled.collectAsState()

    ProfileBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Elegant Navigation
            TopAppBar(
                title = {
                    Text(
                        text = "Accessibility",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontFamily = FontFamily.Serif
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.PROFILE) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "VISUAL & SYSTEM PREFERENCES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 8.dp)
                )

                // Accessibility Settings List inside a single premium glass card
                GlassCard {
                    Column {
                        AccessibilityToggleRow(
                            title = "Bold & Contrast",
                            description = "Enhance readability with thicker font weights and higher color contrast.",
                            checked = isBoldAndContrastEnabled,
                            onCheckedChange = { viewModel.setBoldAndContrastEnabled(it) }
                        )

                        Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

                        AccessibilityToggleRow(
                            title = "Dyslexia friendly font",
                            description = "Apply a custom open-dyslexic style typography across the app screens.",
                            checked = isDyslexiaFontEnabled,
                            onCheckedChange = { viewModel.setDyslexiaFontEnabled(it) }
                        )

                        Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

                        AccessibilityToggleRow(
                            title = "Keep screen awake",
                            description = "Prevent the device screen from dimming or going to sleep while reading verses.",
                            checked = isKeepScreenAwakeEnabled,
                            onCheckedChange = { viewModel.setKeepScreenAwakeEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Note: Certain adjustments may require an app relaunch to take full visual effect across custom fonts.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AccessibilityToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GoldAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

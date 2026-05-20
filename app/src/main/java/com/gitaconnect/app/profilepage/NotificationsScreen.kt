package com.gitaconnect.app.profilepage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val masterSwitch by viewModel.masterNotificationSwitch.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val scrollState = rememberScrollState()

    // Dialog state for editing reminder time
    var activeReminderForEdit by remember { mutableStateOf<Reminder?>(null) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val tableAlpha by animateFloatAsState(targetValue = if (masterSwitch) 1f else 0.5f)

    ProfileBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications & Reminders",
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Master Notification Controller
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFF1E88E5),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Enable Notifications",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Global notification toggle",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Switch(
                            checked = masterSwitch,
                            onCheckedChange = { viewModel.setMasterNotificationSwitch(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GoldAccent,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SCHEDULED REMINDERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                )

                // List of Reminders
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(tableAlpha),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    reminders.forEach { reminder ->
                        ReminderRowCard(
                            reminder = reminder,
                            isInteractive = masterSwitch,
                            onToggle = { enabled ->
                                viewModel.toggleReminder(reminder.id, enabled)
                            },
                            onClick = {
                                if (masterSwitch) {
                                    activeReminderForEdit = reminder
                                    showTimePickerDialog = true
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog && activeReminderForEdit != null) {
        val reminder = activeReminderForEdit!!
        var hourVal by remember { mutableStateOf(reminder.hour) }
        var minuteVal by remember { mutableStateOf(reminder.minute) }

        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = {
                Text(
                    text = "Edit ${reminder.label} Time",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hour Selector (Mock layout)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Hour", fontSize = 12.sp, color = TextMuted)
                            OutlinedTextField(
                                value = hourVal.toString(),
                                onValueChange = { hourVal = it.toIntOrNull()?.coerceIn(0, 23) ?: hourVal },
                                modifier = Modifier.width(60.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Text(text = ":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        // Minute Selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Minute", fontSize = 12.sp, color = TextMuted)
                            OutlinedTextField(
                                value = String.format("%02d", minuteVal),
                                onValueChange = { minuteVal = it.toIntOrNull()?.coerceIn(0, 59) ?: minuteVal },
                                modifier = Modifier.width(60.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "24-hour format: input hours 0-23, minutes 0-59",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateReminderTime(reminder.id, hourVal, minuteVal)
                        showTimePickerDialog = false
                    }
                ) {
                    Text(text = "Save", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text(text = "Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun ReminderRowCard(
    reminder: Reminder,
    isInteractive: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isInteractive, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.timeString,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    color = if (reminder.isEnabled && isInteractive) TextDark else TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reminder.label} • ${reminder.repeatDaysString}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle,
                enabled = isInteractive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GoldAccent,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

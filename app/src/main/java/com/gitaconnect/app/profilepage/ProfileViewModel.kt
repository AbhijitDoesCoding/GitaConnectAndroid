package com.gitaconnect.app.profilepage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class Screen {
    PROFILE,
    PERSONAL_INFO,
    LIKED,
    STATS,
    ACCESSIBILITY,
    NOTIFICATIONS,
    ABOUT
}

enum class ReminderType(val value: String) {
    MANTRA("mantra"),
    DAILY_WISDOM("dailyWisdom"),
    MOOD_TRACKER("moodTracker")
}

data class Reminder(
    val id: UUID = UUID.randomUUID(),
    val type: ReminderType,
    val hour: Int,
    val minute: Int,
    val repeatDays: List<Int>, // 1 = Sunday, 2 = Monday, etc.
    val label: String,
    val isEnabled: Boolean = true
) {
    val timeString: String
        get() = String.format("%02d:%02d %s", 
            if (hour % 12 == 0) 12 else hour % 12, 
            minute, 
            if (hour >= 12) "PM" else "AM"
        )

    val repeatDaysString: String
        get() = when {
            repeatDays.isEmpty() -> "Never"
            repeatDays.size == 7 -> "Every day"
            else -> {
                val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                repeatDays.sorted().map { dayNames[it - 1] }.joinToString(", ")
            }
        }
}

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val profileImageUrl: String?,
    val dateOfBirth: String,
    val gender: String,
    val totalXP: Int = 1250 // Mock XP
)

class ProfileViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow(Screen.PROFILE)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(
        UserProfile(
            name = "Abhinav Kumar",
            email = "abhinav@gitaconnect.com",
            phone = "+91 98765 43210",
            profileImageUrl = null,
            dateOfBirth = "15-08-2002",
            gender = "Male",
            totalXP = 1340
        )
    )
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Accessibility state
    private val _isBoldAndContrastEnabled = MutableStateFlow(false)
    val isBoldAndContrastEnabled: StateFlow<Boolean> = _isBoldAndContrastEnabled.asStateFlow()

    private val _isDyslexiaFontEnabled = MutableStateFlow(false)
    val isDyslexiaFontEnabled: StateFlow<Boolean> = _isDyslexiaFontEnabled.asStateFlow()

    private val _isKeepScreenAwakeEnabled = MutableStateFlow(false)
    val isKeepScreenAwakeEnabled: StateFlow<Boolean> = _isKeepScreenAwakeEnabled.asStateFlow()

    // Notifications state
    private val _masterNotificationSwitch = MutableStateFlow(true)
    val masterNotificationSwitch: StateFlow<Boolean> = _masterNotificationSwitch.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(
        listOf(
            Reminder(type = ReminderType.MANTRA, hour = 8, minute = 0, repeatDays = listOf(1, 2, 3, 4, 5, 6, 7), label = "Mantra Ritual"),
            Reminder(type = ReminderType.DAILY_WISDOM, hour = 9, minute = 30, repeatDays = listOf(1, 2, 3, 4, 5, 6, 7), label = "Daily Wisdom"),
            Reminder(type = ReminderType.MOOD_TRACKER, hour = 20, minute = 0, repeatDays = listOf(1, 2, 3, 4, 5, 6, 7), label = "Mood Tracker")
        )
    )
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun updateProfile(name: String, email: String, phone: String, dateOfBirth: String, gender: String) {
        _userProfile.value = _userProfile.value?.copy(
            name = name,
            email = email,
            phone = phone,
            dateOfBirth = dateOfBirth,
            gender = gender
        )
    }

    fun updateProfileImage(url: String?) {
        _userProfile.value = _userProfile.value?.copy(profileImageUrl = url)
    }

    fun setBoldAndContrastEnabled(enabled: Boolean) {
        _isBoldAndContrastEnabled.value = enabled
    }

    fun setDyslexiaFontEnabled(enabled: Boolean) {
        _isDyslexiaFontEnabled.value = enabled
    }

    fun setKeepScreenAwakeEnabled(enabled: Boolean) {
        _isKeepScreenAwakeEnabled.value = enabled
    }

    fun setMasterNotificationSwitch(enabled: Boolean) {
        _masterNotificationSwitch.value = enabled
    }

    fun toggleReminder(reminderId: UUID, enabled: Boolean) {
        _reminders.value = _reminders.value.map {
            if (it.id == reminderId) it.copy(isEnabled = enabled) else it
        }
    }

    fun updateReminderTime(reminderId: UUID, hour: Int, minute: Int) {
        _reminders.value = _reminders.value.map {
            if (it.id == reminderId) it.copy(hour = hour, minute = minute) else it
        }
    }

    fun logout() {
        _userProfile.value = null
    }

    fun login() {
        _userProfile.value = UserProfile(
            name = "Abhinav Kumar",
            email = "abhinav@gitaconnect.com",
            phone = "+91 98765 43210",
            profileImageUrl = null,
            dateOfBirth = "15-08-2002",
            gender = "Male",
            totalXP = 1340
        )
    }
}

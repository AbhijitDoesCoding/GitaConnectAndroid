@file:Suppress("SpellCheckingInspection")
package com.gitaconnect.app.profilepage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.postgrest
import com.gitaconnect.app.supabasecentral.SupabaseManager

enum class Screen {
    PROFILE,
    PERSONAL_INFO,
    LIKED,
    STATS,
    ACCESSIBILITY,
    NOTIFICATIONS,
    ABOUT,
    LOGIN
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

@Serializable
data class DbProfile(
    @SerialName("user_id") val userId: String,
    val name: String? = "",
    @SerialName("display_name") val displayName: String? = "",
    val email: String? = "",
    val phone: String? = "",
    @SerialName("date_of_birth") val dateOfBirth: String? = "",
    val gender: String? = "",
    @SerialName("profile_image_url") val profileImageUrl: String? = null
)

@Serializable
data class DbUserLevelData(
    @SerialName("user_id") val userId: String,
    @SerialName("total_xp") val totalXp: Int = 0,
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("longest_streak") val longestStreak: Int = 0
)

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val profileImageUrl: String?,
    val dateOfBirth: String,
    val gender: String,
    val totalXP: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

class ProfileViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow(Screen.PROFILE)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Auth States
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private val _lastOtpSentTime = MutableStateFlow<Long?>(null)
    val lastOtpSentTime: StateFlow<Long?> = _lastOtpSentTime.asStateFlow()

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

    init {
        checkActiveSession()
    }

    fun checkActiveSession() {
        viewModelScope.launch {
            try {
                val sessionUser = SupabaseManager.client.auth.currentUserOrNull()
                if (sessionUser != null) {
                    _isAuthenticated.value = true
                    fetchUserProfile(sessionUser.id)
                } else {
                    _isAuthenticated.value = false
                    _userProfile.value = null
                }
            } catch (e: Exception) {
                _isAuthenticated.value = false
                _userProfile.value = null
            }
        }
    }

    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch basic profile
                val profileResult = SupabaseManager.client.postgrest.from("profiles")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeSingleOrNull<DbProfile>()

                // Fetch level data
                val levelResult = SupabaseManager.client.postgrest.from("user_level_data")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeSingleOrNull<DbUserLevelData>()

                if (profileResult != null) {
                    _userProfile.value = UserProfile(
                        name = profileResult.name ?: "Gita Seeker",
                        email = profileResult.email ?: "",
                        phone = profileResult.phone ?: "",
                        profileImageUrl = profileResult.profileImageUrl,
                        dateOfBirth = profileResult.dateOfBirth ?: "",
                        gender = profileResult.gender ?: "",
                        totalXP = levelResult?.totalXp ?: 0,
                        currentStreak = levelResult?.currentStreak ?: 0,
                        longestStreak = levelResult?.longestStreak ?: 0
                    )
                }
            } catch (e: Exception) {
                // Handle or fallback
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendOtp(emailInput: String, shouldCreateUser: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                // Cooldown check: 60 seconds
                val lastSend = _lastOtpSentTime.value
                if (lastSend != null) {
                    val elapsed = (System.currentTimeMillis() - lastSend) / 1000
                    if (elapsed < 60) {
                        _authError.value = "Please wait ${60 - elapsed} seconds before requesting another OTP."
                        return@launch
                    }
                }

                // If signing up, first check if user exists in the profiles database
                if (shouldCreateUser) {
                    val emailExists = checkEmailExists(emailInput)
                    if (emailExists) {
                        _authError.value = "An account with this email already exists. Please log in instead."
                        return@launch
                    }
                }

                // Call Supabase OTP send
                SupabaseManager.client.auth.signInWith(OTP) {
                    email = emailInput
                    createUser = shouldCreateUser
                }
                
                _lastOtpSentTime.value = System.currentTimeMillis()
                _isOtpSent.value = true
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Unknown OTP error"
                if (!shouldCreateUser && (
                    msg.contains("user_not_found") || 
                    msg.contains("User not found") || 
                    msg.contains("Signups not allowed")
                )) {
                    _authError.value = "We couldn't find an account matching that email. Please create an account to get started."
                } else {
                    _authError.value = msg
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val result = SupabaseManager.client.postgrest.from("profiles")
                .select {
                    filter {
                        eq("email", email)
                    }
                }.decodeList<DbProfile>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun verifyOtp(emailInput: String, otpToken: String, nameInput: String, isSignUpMode: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                SupabaseManager.client.auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = emailInput,
                    token = otpToken
                )
                
                val user = SupabaseManager.client.auth.currentUserOrNull()
                if (user != null) {
                    if (isSignUpMode) {
                        // Create profile and level data
                        val newProfile = DbProfile(
                            userId = user.id,
                            name = nameInput,
                            displayName = nameInput,
                            email = emailInput
                        )
                        try {
                            SupabaseManager.client.postgrest.from("profiles").insert(newProfile)
                        } catch (e: Exception) {
                            // ignore
                        }

                        val newLevelData = DbUserLevelData(
                            userId = user.id,
                            totalXp = 0,
                            currentStreak = 0,
                            longestStreak = 0
                        )
                        try {
                            SupabaseManager.client.postgrest.from("user_level_data").insert(newLevelData)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                    
                    _isAuthenticated.value = true
                    fetchUserProfile(user.id)
                    _currentScreen.value = Screen.PROFILE
                } else {
                    _authError.value = "Failed to retrieve user session."
                }
            } catch (e: Exception) {
                _authError.value = "Verification failed. Invalid or expired OTP."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetOtpState() {
        _isOtpSent.value = false
        _authError.value = null
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun updateProfile(name: String, email: String, phone: String, dateOfBirth: String, gender: String) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updated = DbProfile(
                    userId = userId,
                    name = name,
                    displayName = name,
                    email = email,
                    phone = phone,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    profileImageUrl = _userProfile.value?.profileImageUrl
                )
                SupabaseManager.client.postgrest.from("profiles").upsert(updated)
                
                // Update local state
                _userProfile.value = _userProfile.value?.copy(
                    name = name,
                    email = email,
                    phone = phone,
                    dateOfBirth = dateOfBirth,
                    gender = gender
                )
                _currentScreen.value = Screen.PROFILE
            } catch (e: Exception) {
                // ignore
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfileImage(url: String?) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            try {
                val current = _userProfile.value ?: return@launch
                val updated = DbProfile(
                    userId = userId,
                    name = current.name,
                    displayName = current.name,
                    email = current.email,
                    phone = current.phone,
                    dateOfBirth = current.dateOfBirth,
                    gender = current.gender,
                    profileImageUrl = url
                )
                SupabaseManager.client.postgrest.from("profiles").upsert(updated)
                _userProfile.value = _userProfile.value?.copy(profileImageUrl = url)
            } catch (e: Exception) {
                // ignore
            }
        }
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
        viewModelScope.launch {
            try {
                SupabaseManager.client.auth.signOut()
            } catch (e: Exception) {
                // ignore
            }
            _isAuthenticated.value = false
            _userProfile.value = null
            _currentScreen.value = Screen.PROFILE
        }
    }
}

package com.gitaconnect.app.authentication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaconnect.app.profilepage.GoldAccent
import com.gitaconnect.app.profilepage.GlassCard
import com.gitaconnect.app.profilepage.ProfileBackground
import com.gitaconnect.app.profilepage.ProfileViewModel
import com.gitaconnect.app.profilepage.TextDark
import com.gitaconnect.app.profilepage.TextMuted
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val lastOtpSentTime by viewModel.lastOtpSentTime.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var otpToken by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    // Countdown timer state
    var secondsRemaining by remember { mutableStateOf(0) }

    // Run timer countdown when lastOtpSentTime changes
    LaunchedEffect(lastOtpSentTime, isOtpSent) {
        if (isOtpSent && lastOtpSentTime != null) {
            while (true) {
                val elapsed = (System.currentTimeMillis() - lastOtpSentTime!!) / 1000
                val remaining = (60 - elapsed).toInt()
                secondsRemaining = if (remaining > 0) remaining else 0
                if (secondsRemaining <= 0) break
                delay(1000)
            }
        }
    }

    // Reset fields when switching modes
    LaunchedEffect(isSignUpMode) {
        viewModel.resetOtpState()
        otpToken = ""
    }

    ProfileBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Logo / Icon Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ॐ",
                    fontSize = 48.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Gita Connect",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Connect with Eternal Spiritual Wisdom",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Auth Card
            GlassCard {
                if (!isOtpSent) {
                    // --- STEP 1: Enter Email/Name to request OTP ---
                    Text(
                        text = if (isSignUpMode) "Begin your journey" else "Welcome back",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Name Input Field (Only in Sign Up Mode)
                    AnimatedVisibility(visible = isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = Color(0xFFECE4D9),
                                focusedLabelColor = GoldAccent
                            ),
                            singleLine = true
                        )
                    }

                    // Email Input Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFFECE4D9),
                            focusedLabelColor = GoldAccent
                        ),
                        singleLine = true
                    )

                    // Error Message Box
                    authError?.let { errorMsg ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMsg,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Submit Button: Send OTP
                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                if (email.isNotBlank() && name.isNotBlank()) {
                                    viewModel.sendOtp(email, shouldCreateUser = true)
                                }
                            } else {
                                if (email.isNotBlank()) {
                                    viewModel.sendOtp(email, shouldCreateUser = false)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading && email.isNotBlank() && (!isSignUpMode || name.isNotBlank())
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (isSignUpMode) "Create Account" else "Send OTP",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mode Toggle Button
                    TextButton(
                        onClick = {
                            isSignUpMode = !isSignUpMode
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isSignUpMode) "Already have an account? Log In" else "New here? Create Account",
                            color = GoldAccent,
                            fontSize = 14.sp
                        )
                    }

                } else {
                    // --- STEP 2: Enter OTP Code ---
                    Text(
                        text = "Verify Email",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = "We sent a 6-digit OTP code to $email",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // OTP Input Field
                    OutlinedTextField(
                        value = otpToken,
                        onValueChange = { if (it.length <= 6) otpToken = it },
                        label = { Text("6-Digit OTP Code") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFFECE4D9),
                            focusedLabelColor = GoldAccent
                        ),
                        singleLine = true
                    )

                    // Error Message Box
                    authError?.let { errorMsg ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMsg,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Submit Button: Verify OTP
                    Button(
                        onClick = {
                            if (otpToken.length == 6) {
                                viewModel.verifyOtp(email, otpToken, name, isSignUpMode)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading && otpToken.length == 6
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Verify OTP",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Back & Cooldown Resend Buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.resetOtpState()
                                otpToken = ""
                            },
                            enabled = !isLoading
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", color = GoldAccent)
                        }

                        if (secondsRemaining > 0) {
                            Text(
                                text = "Resend in ${secondsRemaining}s",
                                color = TextMuted,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    viewModel.sendOtp(email, shouldCreateUser = isSignUpMode)
                                },
                                enabled = !isLoading
                            ) {
                                Text("Resend OTP", color = GoldAccent)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

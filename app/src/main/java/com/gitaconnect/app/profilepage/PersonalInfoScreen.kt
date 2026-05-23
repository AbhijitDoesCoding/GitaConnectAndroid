package com.gitaconnect.app.profilepage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    // Local state for editing mode
    var isEditing by remember { mutableStateOf(false) }
    
    // Form fields state
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var dobInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("") }

    // Dialog state
    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }

    // Sync input fields when profile loads or enters edit mode
    LaunchedEffect(userProfile, isEditing) {
        userProfile?.let {
            nameInput = it.name
            emailInput = it.email
            phoneInput = it.phone
            dobInput = it.dateOfBirth
            genderInput = it.gender
        }
    }

    ProfileBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Premium Custom TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Profile" else "Profile Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontFamily = FontFamily.Serif
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                isEditing = false // Cancel editing
                            } else {
                                viewModel.navigateTo(Screen.PROFILE)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (isEditing) "Cancel" else "Back",
                            tint = TextDark
                        )
                    }
                },
                actions = {
                    if (userProfile != null) {
                        if (isEditing) {
                            IconButton(
                                onClick = {
                                    // Validation
                                    if (nameInput.isBlank()) {
                                        validationErrorMessage = "Name cannot be empty."
                                        showValidationErrorDialog = true
                                    } else if (emailInput.isNotBlank() && !emailInput.contains("@")) {
                                        validationErrorMessage = "Please enter a valid email address containing '@'."
                                        showValidationErrorDialog = true
                                    } else {
                                        viewModel.updateProfile(
                                            name = nameInput,
                                            email = emailInput,
                                            phone = phoneInput,
                                            dateOfBirth = dobInput,
                                            gender = genderInput
                                        )
                                        isEditing = false
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    tint = GoldAccent
                                )
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = TextDark
                                )
                            }
                        }
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Header Section
                Spacer(modifier = Modifier.height(10.dp))
                
                val initials = userProfile?.name?.let { name ->
                    name.split(" ").filter { it.isNotBlank() }.take(2).map { it.take(1) }.joinToString("").uppercase()
                } ?: "GU"

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(GoldGradient)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to Change Photo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                // Mock Photo Picker Update
                                val mockUrls = listOf(
                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"
                                )
                                viewModel.updateProfileImage(mockUrls.random())
                            }
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Profile Fields UI
                if (isEditing) {
                    EditProfileFields(
                        name = nameInput,
                        onNameChange = { nameInput = it },
                        email = emailInput,
                        onEmailChange = { emailInput = it },
                        phone = phoneInput,
                        onPhoneChange = { phoneInput = it },
                        dob = dobInput,
                        onDobChange = { dobInput = it },
                        gender = genderInput,
                        onGenderChange = { genderInput = it }
                    )
                } else {
                    ViewProfileFields(
                        name = nameInput,
                        email = emailInput,
                        phone = phoneInput,
                        dob = dobInput,
                        gender = genderInput
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            title = {
                Text(
                    text = "Validation Error",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(text = validationErrorMessage) },
            confirmButton = {
                TextButton(onClick = { showValidationErrorDialog = false }) {
                    Text(text = "OK", color = GoldAccent)
                }
            }
        )
    }
}

@Composable
fun ViewProfileFields(
    name: String,
    email: String,
    phone: String,
    dob: String,
    gender: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PUBLIC PROFILE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(start = 8.dp)
        )
        
        GlassCard {
            InfoRow(label = "Name", value = name.ifEmpty { "Not specified" })
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PRIVATE PROFILE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(start = 8.dp)
        )

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(label = "Email", value = email.ifEmpty { "Not specified" })
                Divider(color = Color.White.copy(alpha = 0.3f))
                InfoRow(label = "Phone", value = phone.ifEmpty { "Not specified" })
                Divider(color = Color.White.copy(alpha = 0.3f))
                InfoRow(label = "Date of Birth", value = dob.ifEmpty { "Not specified" })
                Divider(color = Color.White.copy(alpha = 0.3f))
                InfoRow(label = "Gender", value = gender.ifEmpty { "Not specified" })
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
    }
}

@Composable
fun EditProfileFields(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    dob: String,
    onDobChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PUBLIC PROFILE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(start = 8.dp)
        )

        GlassTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Name"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PRIVATE PROFILE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(start = 8.dp)
        )

        GlassTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email"
        )

        GlassTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "Phone"
        )

        GlassTextField(
            value = dob,
            onValueChange = onDobChange,
            label = "Date of Birth (DD-MM-YYYY)"
        )

        GlassTextField(
            value = gender,
            onValueChange = onGenderChange,
            label = "Gender"
        )
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted, fontSize = 14.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            focusedLabelColor = GoldAccent,
            unfocusedLabelColor = TextMuted,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            focusedContainerColor = Color.White.copy(alpha = 0.4f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

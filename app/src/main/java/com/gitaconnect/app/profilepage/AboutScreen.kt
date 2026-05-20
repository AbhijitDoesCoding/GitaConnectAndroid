package com.gitaconnect.app.profilepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    ProfileBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Elegant top navigation
            TopAppBar(
                title = {
                    Text(
                        text = "About",
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
                // Section 1: About GitaConnect
                AboutSectionCard(
                    title = "About GitaConnect",
                    content = "GitaConnect is a spiritual companion app designed to bring the profound wisdom of the Bhagavad Gita to your daily life. Our mission is to provide an accessible, immersive, and personalized experience for exploring the sacred verses, whether through reading, listening, or reflection. We aim to bridge ancient wisdom with modern technology to help you find peace, guidance, and spiritual growth."
                )

                // Section 2: Bhagavad Gita Audio
                AboutSectionCard(
                    title = "Bhagavad Gita Audio",
                    content = "The high-quality audio recordings for the Bhagavad Gita verses used in this app are sourced from the 'Bhagavad Gita Audio' dataset on Kaggle by Dhruv Jaradi.\n\nSource: https://www.kaggle.com/datasets/dhruvjaradi/bhagavad-gita-audio\n\nWe are grateful for this resource which allows our users to experience the vibration and pronunciation of the original Sanskrit verses."
                )

                // Section 3: Sarvam AI
                AboutSectionCard(
                    title = "Sarvam AI",
                    content = "We leverage Sarvam AI's advanced Indic language models and speech services to enhance the linguistic accuracy and natural processing of Indian languages within the app, ensuring a more authentic and localized experience."
                )

                // Section 4: AI Translations
                AboutSectionCard(
                    title = "AI Translations",
                    content = "To provide accurate and meaningful translations across multiple regional languages, GitaConnect leverages Google's Gemini AI. This technology helps us translate the essence and wisdom of the Gita into languages including Hindi, Tamil, Telugu, Kannada, Malayalam, Marathi, Gujarati, Bengali, Punjabi, and Odia."
                )

                // Section 5: Licenses & Third-Party Libraries
                AboutSectionCard(
                    title = "Licenses & Third-Party Libraries",
                    content = "GitaConnect is built using several open-source libraries and frameworks. For full license details, please visit the respective project pages:\n\n• Apache License 2.0: https://www.apache.org/licenses/LICENSE-2.0\n• Supabase (MIT License): https://supabase.com\n• LiquidGlass Effect: Custom glassmorphism implementation\n• Indic Datasets: Creative Commons Attribution\n\nAll trademarks, logos, and brand names are the property of their respective owners."
                )

                // Section 6: Contact Us
                AboutSectionCard(
                    title = "Contact Us",
                    content = "We would love to hear from you! Whether you have questions, feedback, or feature suggestions, please feel free to reach out to us:\n\n• Email: support@gitaconnect.com\n• Website: https://gitaconnect.com"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AboutSectionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                color = TextDark.copy(alpha = 0.85f)
            )
        }
    }
}

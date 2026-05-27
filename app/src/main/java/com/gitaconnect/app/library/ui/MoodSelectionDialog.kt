package com.gitaconnect.app.library.ui

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaconnect.app.library.repository.GitaRepository
import com.gitaconnect.app.library.services.MoodProgressManager
import com.gitaconnect.app.supabasecentral.SupabaseManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RecommendationResponse(
    val chapter: Int,
    val verse: Int
)

@Composable
fun MoodSelectionDialog(
    onDismiss: () -> Unit,
    onComplete: (chapter: Int, verseId: Int) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Step 1: Slider score (1.0 to 10.0)
    var score by remember { mutableFloatStateOf(5.0f) }
    
    // Step 2: Random question selected based on score
    var selectedQuestion by remember { mutableStateOf<MoodProgressManager.MoodQuestion?>(null) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    
    // Step 3: Thoughts text
    var thoughtsText by remember { mutableStateOf("") }
    
    // Loading State
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Visibility state for entering/leaving animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun dismissWithAnimation() {
        coroutineScope.launch {
            isVisible = false
            delay(280) // matches fade/scale transition exit duration
            onDismiss()
        }
    }

    // Initialize/pick a question when entering step 2
    LaunchedEffect(currentStep) {
        if (currentStep == 2 && selectedQuestion == null) {
            selectedQuestion = MoodProgressManager.getQuestionForScore(score.toDouble())
            selectedOptionIndex = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Scrim Backdrop (Fades In/Out)
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = !isLoading) { dismissWithAnimation() }
            )
        }

        // 2. Dialog Card Overlay (Slide In from Bottom, Slide Out to Bottom)
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f) // 95% screen height like iOS page sheet
                    .clickable(enabled = true, onClick = {}), // Prevent clicking through to scrim
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header (iOS Style Navigation Bar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isLoading) {
                            if (currentStep == 1) {
                                IconButton(onClick = { dismissWithAnimation() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = GitaCharcoal)
                                }
                            } else {
                                IconButton(onClick = { 
                                    if (currentStep == 2) {
                                        selectedQuestion = null
                                        currentStep = 1 
                                    } else if (currentStep == 3) {
                                        currentStep = 2 
                                    }
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GitaCharcoal)
                                }
                            }
                        } else {
                            Spacer(Modifier.width(48.dp))
                        }

                        Text(
                            text = "Step $currentStep",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GitaCharcoal
                        )

                        Spacer(Modifier.width(48.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    // Step Content
                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = GitaSaffron, strokeWidth = 3.dp)
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = "Seeking resonant verse from Bhagavad Gita...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = GitaCharcoalSoft,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Horizontal Sliding Step Transitions
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width } + fadeIn(tween(250)))
                                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(tween(250)))
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn(tween(250)))
                                        .togetherWith(slideOutHorizontally { width -> width } + fadeOut(tween(250)))
                                }
                            },
                            label = "StepTransition"
                        ) { step ->
                            when (step) {
                                1 -> Step1Content(
                                    score = score,
                                    onScoreChange = { score = it },
                                    onNext = { currentStep = 2 }
                                )
                                2 -> Step2Content(
                                    question = selectedQuestion,
                                    selectedIndex = selectedOptionIndex,
                                    onOptionSelected = { selectedOptionIndex = it },
                                    onNext = { currentStep = 3 }
                                )
                                3 -> Step3Content(
                                    thoughts = thoughtsText,
                                    onThoughtsChange = { thoughtsText = it },
                                    onSubmit = {
                                        isLoading = true
                                        coroutineScope.launch {
                                            performAIRecommendation(
                                                context = context,
                                                score = score,
                                                selectedQuestion = selectedQuestion,
                                                selectedOptionIndex = selectedOptionIndex,
                                                thoughtsText = thoughtsText,
                                                onComplete = { ch, v ->
                                                    isLoading = false
                                                    onComplete(ch, v)
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun Step1Content(
    score: Float,
    onScoreChange: (Float) -> Unit,
    onNext: () -> Unit
) {
    val level = MoodProgressManager.PleasantnessLevel.fromScore(score.toDouble())
    val label = when (level) {
        MoodProgressManager.PleasantnessLevel.VERY_UNPLEASANT -> "Very Unpleasant 😢"
        MoodProgressManager.PleasantnessLevel.UNPLEASANT -> "Unpleasant 🙁"
        MoodProgressManager.PleasantnessLevel.NEUTRAL -> "Neutral 😐"
        MoodProgressManager.PleasantnessLevel.PLEASANT -> "Pleasant 🙂"
        MoodProgressManager.PleasantnessLevel.VERY_PLEASANT -> "Very Pleasant 😇"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How do you feel today?",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = GitaCharcoal,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Score: ${"%.1f".format(score)}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GitaSaffron
        )
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = GitaCharcoalSoft
        )
        Spacer(Modifier.height(24.dp))
        Slider(
            value = score,
            onValueChange = onScoreChange,
            valueRange = 1f..10f,
            colors = SliderDefaults.colors(
                thumbColor = GitaSaffron,
                activeTrackColor = GitaSaffron,
                inactiveTrackColor = GitaDivider
            )
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GitaSaffron)
        ) {
            Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Step2Content(
    question: MoodProgressManager.MoodQuestion?,
    selectedIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question?.question ?: "How is your mood affecting you?",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GitaCharcoal,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        
        question?.options?.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            Card(
                onClick = { onOptionSelected(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) GitaSaffron else Color(0xFFE8DFC8)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GitaSaffron.copy(alpha = 0.08f) else Color(0xFFFFFDF9)
                )
            ) {
                Text(
                    text = option,
                    fontSize = 14.sp,
                    color = GitaCharcoal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
        
        Spacer(Modifier.height(28.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedIndex != null,
            colors = ButtonDefaults.buttonColors(containerColor = GitaSaffron)
        ) {
            Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Step3Content(
    thoughts: String,
    onThoughtsChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tell us what's the reason for your mood (optional)",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = GitaCharcoal,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = thoughts,
            onValueChange = { if (it.length <= 250) onThoughtsChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            placeholder = { Text("Type your thoughts or situation here...", color = Color.Gray, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GitaSaffron,
                unfocusedBorderColor = Color(0xFFE8DFC8),
                focusedLabelColor = GitaSaffron,
                cursorColor = GitaSaffron
            )
        )
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "${thoughts.length} / 250",
                fontSize = 12.sp,
                color = GitaCharcoalSoft
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GitaSaffron)
        ) {
            Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

private suspend fun performAIRecommendation(
    context: Context,
    score: Float,
    selectedQuestion: MoodProgressManager.MoodQuestion?,
    selectedOptionIndex: Int?,
    thoughtsText: String,
    onComplete: (chapter: Int, verseId: Int) -> Unit
) {
    val pleasantness = score.toDouble()
    val moodLevel = MoodProgressManager.PleasantnessLevel.fromScore(pleasantness)
    val moodLevelKey = moodLevel.key
    val moodQuestion = selectedQuestion?.question ?: "Context"
    val moodAnswer = selectedOptionIndex?.let { selectedQuestion?.options?.getOrNull(it) } ?: "Not specified"
    val thoughts = thoughtsText.trim().ifEmpty { "I am looking for guidance." }
    val mappedVersePools = MoodProgressManager.getFullMappingJSON(context)

    val systemInstruction = """
        You are a spiritual guide helping users find the perfect Bhagavad Gita verse for their current emotional and situational state.

        You must choose from this mapped verse pool only:
        $mappedVersePools

        Current user's mood level key: $moodLevelKey

        PRIORITY FOR MATCHING (strict order):
        1) Mood score and mood level key (primary signal)
        2) Follow-up question and selected answer
        3) Personal thoughts text
        4) Selected tags

        SELECTION RULES:
        - First, only consider verses from the array matching the current mood level key.
        - Then choose the ONE verse that best resonates with the user's complete context.
        - Do not use verses from other mood level arrays.
        - Do not hallucinate chapter/verse numbers.
        - Maximum verses per chapter: {1:47, 2:72, 3:43, 4:42, 5:29, 6:47, 7:30, 8:28, 9:34, 10:42, 11:55, 12:20, 13:35, 14:27, 15:20, 16:24, 17:28, 18:78}.
        - Do not output markdown, backticks, or extra text.

        Output strictly raw JSON in this exact format:
        {"chapter": <Int>, "verse": <Int>}
    """.trimIndent()

    val uniqueID = java.util.UUID.randomUUID().toString()
    val userPrompt = """
        Request ID: $uniqueID
        Mood Score (1-10): $pleasantness
        Pre-selected Prompt: $moodQuestion - $moodAnswer
        User Tags: 
        Specific Thoughts/Situation: "$thoughts"
    """.trimIndent()

    val messages = listOf(
        mapOf("role" to "system", "content" to systemInstruction),
        mapOf("role" to "user", "content" to userPrompt)
    )

    try {
        val aiResponse = SupabaseManager.callSarvamAI(messages, temperature = 0.3)
        val content = aiResponse.choices?.firstOrNull()?.message?.content
        if (!content.isNullOrBlank()) {
            val sanitized = content
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val json = Json { ignoreUnknownKeys = true }
            val recommendation = json.decodeFromString<RecommendationResponse>(sanitized)
            
            val repository = GitaRepository(context)
            val verses = repository.getVersesForChapter(recommendation.chapter)
            val verse = verses.firstOrNull { it.verseId == recommendation.verse }
            
            if (verse != null) {
                MoodProgressManager.saveMoodCompletion(
                    chapter = recommendation.chapter,
                    verseId = recommendation.verse,
                    sanskrit = verse.sanskritVerse,
                    english = verse.englishTranslation,
                    moodKey = moodLevelKey
                )
                onComplete(recommendation.chapter, recommendation.verse)
                return
            }
        }
        fallbackRecommendation(context, moodLevelKey, onComplete)
    } catch (e: Exception) {
        e.printStackTrace()
        fallbackRecommendation(context, moodLevelKey, onComplete)
    }
}

private suspend fun fallbackRecommendation(
    context: Context,
    moodLevelKey: String,
    onComplete: (chapter: Int, verseId: Int) -> Unit
) {
    val fallbackPair = MoodProgressManager.getFallbackVerseForMood(moodLevelKey) ?: Pair(1, 1)
    val repository = GitaRepository(context)
    val verses = repository.getVersesForChapter(fallbackPair.first)
    val verse = verses.firstOrNull { it.verseId == fallbackPair.second } ?: verses.firstOrNull()
    if (verse != null) {
        MoodProgressManager.saveMoodCompletion(
            chapter = verse.chapter,
            verseId = verse.verseId,
            sanskrit = verse.sanskritVerse,
            english = verse.englishTranslation,
            moodKey = moodLevelKey
        )
        onComplete(verse.chapter, verse.verseId)
    } else {
        onComplete(1, 1)
    }
}

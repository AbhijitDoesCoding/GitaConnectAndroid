package com.gitaconnect.app.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.gitaconnect.app.R
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import com.gitaconnect.app.library.models.Verse

// ---------------------------------------------------------------------------
// Read Verse Screen
// No nested Scaffold — the parent MainScreen Scaffold already handles insets.
// We use a Column with explicit top-bar + scrollable content + bottom nav bar.
// ---------------------------------------------------------------------------

private val languages = listOf(
    "English", "Hindi", "Tamil", "Telugu",
    "Kannada", "Malayalam", "Marathi", "Gujarati", "Bengali",
    "Punjabi", "Odia"
)

@Composable
fun ReadVerseScreen(
    initialVerse: Verse,
    allVerses: List<Verse>,
    onBack: () -> Unit
) {
    // Current verse index drives navigation arrows
    var currentIndex by remember {
        mutableIntStateOf(
            allVerses.indexOfFirst { it.verseId == initialVerse.verseId }.coerceAtLeast(0)
        )
    }
    val verse = allVerses.getOrElse(currentIndex) { initialVerse }
    val scrollState = rememberScrollState()

    var selectedLanguageIndex by remember { mutableIntStateOf(0) }
    var isRead by remember { mutableStateOf(false) }

    // Update isRead state when current verse changes
    LaunchedEffect(currentIndex) {
        isRead = com.gitaconnect.app.library.services.ReadingProgressManager.isVerseRead(verse.chapter, verse.verseId)
    }

    // ---- Audio Player Setup ----
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }

    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    // Initialize MediaController connected to Foreground Service
    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, "com.gitaconnect.app.library.services.VerseAudioService"))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            mediaController = controller
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    isPlaying = isPlayingNow
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering = playbackState == Player.STATE_BUFFERING
                    if (playbackState == Player.STATE_ENDED) {
                        isPlaying = false
                        controller.seekTo(0)
                    }
                }
            })
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            MediaController.releaseFuture(controllerFuture)
            mediaController = null
        }
    }

    // Reset scroll to top and update audio when verse changes
    LaunchedEffect(currentIndex, mediaController) { 
        scrollState.scrollTo(0) 
        
        mediaController?.let { controller ->
            val audioUrl = "https://ltacraukglfzilebevyg.supabase.co/storage/v1/object/public/recordings/${verse.chapter}-${verse.verseId}.mp3"
            val mediaItem = MediaItem.fromUri(audioUrl)
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.pause() // Pause automatically when changing verses
        }
    }

    var showMentorSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_beige22),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ---- Top Bar ----
            ReadVerseTopBar(
                verse = verse, 
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                selectedLanguageIndex = selectedLanguageIndex,
                isRead = isRead,
                onPlayPauseClick = {
                    mediaController?.let { controller ->
                        if (isPlaying) {
                            controller.seekTo(0)
                            controller.pause()
                        } else {
                            controller.play()
                        }
                    }
                },
                onLanguageSelected = { selectedLanguageIndex = it },
                onMarkReadClick = {
                    val chapter = verse.chapter
                    val verseId = verse.verseId
                    if (com.gitaconnect.app.library.services.ReadingProgressManager.isVerseRead(chapter, verseId)) {
                        com.gitaconnect.app.library.services.ReadingProgressManager.unmarkVerseRead(chapter, verseId)
                        isRead = false
                    } else {
                        com.gitaconnect.app.library.services.ReadingProgressManager.markVerseRead(chapter, verseId)
                        isRead = true
                    }
                },
                onBack = onBack
            )

            // ---- Scrollable Content ----
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section 1: Sanskrit Shloka
                VerseSection(label = "Shloka") {
                    Text(
                        text = verse.sanskritVerse,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GitaCharcoal,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp,
                        letterSpacing = 0.3.sp
                    )
                }

                Spacer(Modifier.height(16.dp))
                GitaDividerLine()
                Spacer(Modifier.height(16.dp))

                // Section 2: Translation
                VerseSection(label = "Translation") {
                    val translationText = when (selectedLanguageIndex) {
                        0 -> verse.englishTranslation
                        1 -> verse.hindiTranslation ?: verse.englishTranslation
                        2 -> verse.tamilTranslation ?: verse.englishTranslation
                        3 -> verse.teluguTranslation ?: verse.englishTranslation
                        4 -> verse.kannadaTranslation ?: verse.englishTranslation
                        5 -> verse.malayalamTranslation ?: verse.englishTranslation
                        6 -> verse.marathiTranslation ?: verse.englishTranslation
                        7 -> verse.gujaratiTranslation ?: verse.englishTranslation
                        8 -> verse.bengaliTranslation ?: verse.englishTranslation
                        9 -> verse.punjabiTranslation ?: verse.englishTranslation
                        10 -> verse.odiaTranslation ?: verse.englishTranslation
                        else -> verse.englishTranslation
                    }
                    Text(
                        text = translationText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = GitaCharcoal,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Start
                    )
                }

                // Section 3: Reflection
                val reflectionText = when (selectedLanguageIndex) {
                    0 -> verse.essence?.reflection
                    1 -> verse.essence?.reflectionHindi ?: verse.essence?.reflection
                    2 -> verse.essence?.reflectionTamil ?: verse.essence?.reflection
                    3 -> verse.essence?.reflectionTelugu ?: verse.essence?.reflection
                    4 -> verse.essence?.reflectionKannada ?: verse.essence?.reflection
                    5 -> verse.essence?.reflectionMalayalam ?: verse.essence?.reflection
                    6 -> verse.essence?.reflectionMarathi ?: verse.essence?.reflection
                    7 -> verse.essence?.reflectionGujarati ?: verse.essence?.reflection
                    8 -> verse.essence?.reflectionBengali ?: verse.essence?.reflection
                    9 -> verse.essence?.reflectionPunjabi ?: verse.essence?.reflection
                    10 -> verse.essence?.reflectionOdia ?: verse.essence?.reflection
                    else -> verse.essence?.reflection
                }
                if (!reflectionText.isNullOrBlank() && reflectionText != "None.") {
                    Spacer(Modifier.height(16.dp))
                    GitaDividerLine()
                    Spacer(Modifier.height(16.dp))

                    VerseSection(label = "Reflection") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GitaSaffron.copy(alpha = 0.08f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = reflectionText,
                                fontSize = 15.sp,
                                fontStyle = FontStyle.Italic,
                                color = GitaCharcoalSoft,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                // Section 4: Krishna's Guidance
                val guidanceText = when (selectedLanguageIndex) {
                    0 -> verse.essence?.krishnaGuidance
                    1 -> verse.essence?.krishnaGuidanceHindi ?: verse.essence?.krishnaGuidance
                    2 -> verse.essence?.krishnaGuidanceTamil ?: verse.essence?.krishnaGuidance
                    3 -> verse.essence?.krishnaGuidanceTelugu ?: verse.essence?.krishnaGuidance
                    4 -> verse.essence?.krishnaGuidanceKannada ?: verse.essence?.krishnaGuidance
                    5 -> verse.essence?.krishnaGuidanceMalayalam ?: verse.essence?.krishnaGuidance
                    6 -> verse.essence?.krishnaGuidanceMarathi ?: verse.essence?.krishnaGuidance
                    7 -> verse.essence?.krishnaGuidanceGujarati ?: verse.essence?.krishnaGuidance
                    8 -> verse.essence?.krishnaGuidanceBengali ?: verse.essence?.krishnaGuidance
                    9 -> verse.essence?.krishnaGuidancePunjabi ?: verse.essence?.krishnaGuidance
                    10 -> verse.essence?.krishnaGuidanceOdia ?: verse.essence?.krishnaGuidance
                    else -> verse.essence?.krishnaGuidance
                }
                if (!guidanceText.isNullOrBlank() && guidanceText != "None.") {
                    Spacer(Modifier.height(16.dp))
                    GitaDividerLine()
                    Spacer(Modifier.height(16.dp))

                    VerseSection(label = "Krishna's Guidance") {
                        Text(
                            text = guidanceText,
                            fontSize = 15.sp,
                            color = GitaCharcoal,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                GitaDividerLine()
                Spacer(Modifier.height(24.dp))

                // Section 5: Ask Gita Mentor Inline Card (iOS AskMentorButtonCell Parity)
                Card(
                    onClick = { showMentorSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ai_pandit),
                            contentDescription = "Gita Mentor Avatar",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.dp, GitaSaffron, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ask Gita Mentor",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GitaCharcoal
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Seek guidance on this verse",
                                fontSize = 13.sp,
                                color = GitaCharcoalSoft
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Ask Gita Mentor",
                            tint = GitaSaffron,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(80.dp)) // Leave room for FAB and spacing
            }

            // ---- Bottom Navigation Bar ----
            ReadVerseNavBar(
                currentIndex = currentIndex,
                totalVerses = allVerses.size,
                onPrev = { if (currentIndex > 0) currentIndex-- },
                onNext = { if (currentIndex < allVerses.size - 1) currentIndex++ }
            )
        }

        // ---- Verse AI Mentor Overlay Card ----
        androidx.compose.animation.AnimatedVisibility(
            visible = showMentorSheet,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)) + 
                    androidx.compose.animation.scaleIn(initialScale = 0.85f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(durationMillis = 250)) + 
                   androidx.compose.animation.scaleOut(targetScale = 0.85f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 250))
        ) {
            com.gitaconnect.app.mentor.VerseMentorBottomSheet(
                verse = verse,
                onDismiss = { showMentorSheet = false }
            )
        }
    }
}

@Composable
private fun ReadVerseTopBar(
    verse: Verse, 
    isPlaying: Boolean,
    isBuffering: Boolean,
    selectedLanguageIndex: Int,
    isRead: Boolean,
    onPlayPauseClick: () -> Unit,
    onLanguageSelected: (Int) -> Unit,
    onMarkReadClick: () -> Unit,
    onBack: () -> Unit
) {
    Column {
        Surface(
            color = Color(0xFFFFFDF9), // WarmBeigeLight solid top bar
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = GitaCharcoal
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BG ${verse.chapter}.${verse.verseId}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GitaCharcoal
                    )
                    Text(
                        text = "Verse ${verse.verseId}",
                        fontSize = 12.sp,
                        color = GitaSaffron
                    )
                }

                // Audio Play/Pause Button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GitaSaffron.copy(alpha = 0.15f))
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GitaSaffron,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = GitaSaffron,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Mark as Read checkmark circle button (iOS Parity)
                IconButton(onClick = onMarkReadClick) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Mark Read",
                        tint = if (isRead) GitaSaffron else GitaCharcoal.copy(alpha = 0.2f),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Language Selector (iOS Parity)
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GitaBeige)
                            .clickable { expanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = languages.getOrElse(selectedLanguageIndex) { "English" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GitaCharcoal
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Language",
                            tint = GitaCharcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languages.forEachIndexed { index, language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    onLanguageSelected(index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFE0D9CC), thickness = 1.dp)
    }
}

@Composable
private fun ReadVerseNavBar(
    currentIndex: Int,
    totalVerses: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        HorizontalDivider(color = Color(0xFFE0D9CC), thickness = 1.dp)
        Surface(
            color = Color(0xFFFFFDF9), // WarmBeigeLight solid bottom bar
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalButton(
                    onClick = onPrev,
                    enabled = currentIndex > 0,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GitaSaffron.copy(alpha = 0.15f),
                        contentColor = GitaSaffron
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = "Previous")
                    Spacer(Modifier.width(4.dp))
                    Text("Previous", fontSize = 13.sp)
                }

                Text(
                    text = "${currentIndex + 1} / $totalVerses",
                    fontSize = 13.sp,
                    color = GitaCharcoalSoft,
                    fontWeight = FontWeight.Medium
                )

                FilledTonalButton(
                    onClick = onNext,
                    enabled = currentIndex < totalVerses - 1,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GitaSaffron.copy(alpha = 0.15f),
                        contentColor = GitaSaffron
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Next", fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = "Next")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared UI building blocks
// ---------------------------------------------------------------------------

@Composable
private fun VerseSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section label pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(GitaSaffron.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GitaSaffron,
                letterSpacing = 1.2.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun GitaDividerLine() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = GitaDivider,
            thickness = 1.dp
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(GitaSaffron.copy(alpha = 0.4f))
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = GitaDivider,
            thickness = 1.dp
        )
    }
}

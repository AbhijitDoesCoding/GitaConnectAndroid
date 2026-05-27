package com.gitaconnect.app.library.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.gitaconnect.app.R
import com.gitaconnect.app.library.models.Chapter
import com.gitaconnect.app.library.models.Verse
import com.gitaconnect.app.library.viewmodel.GitaLibraryViewModel
import com.gitaconnect.app.library.viewmodel.GitaUiState
import com.gitaconnect.app.library.services.MoodProgressManager
import com.gitaconnect.app.library.repository.GitaRepository
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

import androidx.compose.foundation.BorderStroke

// ---------------------------------------------------------------------------
// Design tokens (shared across library screens via package-level vals)
// ---------------------------------------------------------------------------

val GitaBeige        = Color(0xFFF5F0E8)
val GitaBeigeLight   = Color(0xFFFAF7F0)
val GitaCharcoal     = Color(0xFF2C2C2C)
val GitaCharcoalSoft = Color(0xFF555555)
val GitaSaffron      = Color(0xFFE27D60)
val GitaSaffronLight = Color(0xFFF0A68A)
val GitaDivider      = Color(0xFFE0D9CC)
val GitaGold         = Color(0xFFB8860B)

// ---------------------------------------------------------------------------
// Library Home Screen (Primary Dashboard)
// ---------------------------------------------------------------------------

@Composable
fun LibraryHomeScreen(
    onLibraryClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onMantrasClick: () -> Unit,
    onVerseClick: (Verse, List<Verse>) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_beige22),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ---- iOS Style Simple Top Header ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bhagavad Gita",
                    color = GitaCharcoal,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SelectMoodCard(
                    refreshTrigger = refreshTrigger,
                    onSelectMoodClick = { showDialog = true },
                    onVerseClick = onVerseClick
                )

                DailySadhnaCard(
                    onCardClick = onChallengesClick
                )

                BhagavadGitaLibraryCard(onClick = onLibraryClick)

                MantraListCard(
                    onCardClick = onMantrasClick,
                    onMantraClick = { /* TODO: Implement Mantra Play logic when audio service is ready */ }
                )
            }
        }

        if (showDialog) {
            MoodSelectionDialog(
                onDismiss = { showDialog = false },
                onComplete = { chapter, verseId ->
                    showDialog = false
                    refreshTrigger++
                    coroutineScope.launch {
                        val repository = GitaRepository(context)
                        val verses = repository.getVersesForChapter(chapter)
                        val verse = verses.firstOrNull { it.verseId == verseId }
                        if (verse != null) {
                            onVerseClick(verse, verses)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BhagavadGitaLibraryCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 100.dp) // Avoid overlapping with the book image
            ) {
                Text(
                    text = "Bhagavad Gita Library",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GitaCharcoal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dive into the sacred scripture. Explore all 18 chapters and 700 verses of timeless wisdom.",
                    fontSize = 13.sp,
                    color = GitaCharcoalSoft,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GitaSaffron.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "18 Chapters · 700 Verses",
                        color = GitaSaffron,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.bg_book),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 95.dp, height = 90.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Chapters List Screen
// ---------------------------------------------------------------------------

@Composable
fun GitaLibraryChaptersScreen(
    viewModel: GitaLibraryViewModel = viewModel(),
    onChapterClick: (Chapter) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.chaptersState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var refreshTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_beige22),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ---- iOS Style Top Bar with Back Button ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = GitaCharcoal
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Library",
                    color = GitaCharcoal,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            // ---- Content ----
            when (val s = state) {
                is GitaUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GitaSaffron)
                    }
                }
                is GitaUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = GitaCharcoal)
                    }
                }
                is GitaUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            ReadingStatsHeader(refreshTrigger = refreshTrigger)
                            Spacer(Modifier.height(4.dp))
                        }
                        
                        itemsIndexed(s.data) { index, chapter ->
                            ChapterCard(chapter = chapter, onClick = { onChapterClick(chapter) })
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingStatsHeader(refreshTrigger: Int) {
    val stats = remember(refreshTrigger) {
        com.gitaconnect.app.library.services.ReadingProgressManager.getStats()
    }
    
    val statusText = when {
        stats.totalVerses == 0 -> "Fresh start"
        stats.totalVerses < 10 -> "Keep going!"
        stats.totalVerses < 50 -> "Steady progress"
        stats.totalVerses < 200 -> "Spiritual path"
        else -> "Gita Devotee"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(end = 120.dp) // Avoid book image overlap
                ) {
                    Text(
                        text = "Reading Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GitaCharcoalSoft
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = GitaCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${stats.totalVerses} Verses · ${stats.totalChapters} Chapters",
                        fontSize = 14.sp,
                        color = GitaCharcoalSoft
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.bg_book),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(width = 110.dp, height = 80.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = GitaDivider,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatBlock(value = "${stats.today}", label = "Today")
                StatBlock(value = "${stats.week}", label = "This Week")
                StatBlock(value = "${stats.month}", label = "This Month")
            }
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = GitaCharcoal
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = GitaCharcoalSoft
        )
    }
}

@Composable
fun SelectMoodCard(
    refreshTrigger: Int,
    onSelectMoodClick: () -> Unit,
    onVerseClick: (Verse, List<Verse>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isCompleted by remember(refreshTrigger) { 
        mutableStateOf(MoodProgressManager.isMoodCompletedToday()) 
    }
    val recommendedInfo by remember(refreshTrigger) {
        mutableStateOf(MoodProgressManager.getRecommendedVerseInfo())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (!isCompleted || recommendedInfo == null) {
                Text(
                    text = "How is your mood today?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GitaCharcoal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your daily mood to get a recommended verse from the Bhagavad Gita.",
                    fontSize = 13.sp,
                    color = GitaCharcoalSoft,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSelectMoodClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GitaSaffron),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Select Mood", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                val info = recommendedInfo!!
                val moodLabel = when (info.moodKey) {
                    "veryUnpleasant" -> "Very Unpleasant"
                    "unpleasant" -> "Unpleasant"
                    "neutral" -> "Neutral"
                    "pleasant" -> "Pleasant"
                    "veryPleasant" -> "Very Pleasant"
                    else -> "Neutral"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Mood",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GitaCharcoalSoft
                        )
                        Text(
                            text = moodLabel,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GitaSaffron
                        )
                    }
                    OutlinedButton(
                        onClick = onSelectMoodClick,
                        border = BorderStroke(1.dp, GitaSaffron),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GitaSaffron),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = GitaDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    onClick = {
                        coroutineScope.launch {
                            val repository = GitaRepository(context)
                            val verses = repository.getVersesForChapter(info.chapter)
                            val verse = verses.firstOrNull { it.verseId == info.verseId }
                            if (verse != null) {
                                onVerseClick(verse, verses)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Recommended Verse",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GitaSaffron,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chapter ${info.chapter}, Verse ${info.verseId}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GitaCharcoal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = info.sanskrit,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = GitaCharcoal,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to read full verse...",
                            fontSize = 11.sp,
                            color = GitaCharcoalSoft,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(chapter: Chapter, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GitaSaffron.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${chapter.chapterNumber}",
                    color = GitaSaffron,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chapter ${chapter.chapterNumber}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GitaSaffron,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = chapter.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GitaCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!chapter.subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = chapter.subtitle,
                        fontSize = 12.sp,
                        color = GitaCharcoalSoft,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${chapter.verseCount} verses",
                    fontSize = 11.sp,
                    color = GitaGold.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Read",
                tint = GitaSaffron.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

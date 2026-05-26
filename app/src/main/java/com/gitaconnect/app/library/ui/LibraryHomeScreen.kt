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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.gitaconnect.app.R
import com.gitaconnect.app.library.models.Chapter
import com.gitaconnect.app.library.viewmodel.GitaLibraryViewModel
import com.gitaconnect.app.library.viewmodel.GitaUiState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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
// Library Home Screen
// No nested Scaffold — the parent MainScreen Scaffold already handles insets.
// ---------------------------------------------------------------------------

@Composable
fun LibraryHomeScreen(
    viewModel: GitaLibraryViewModel = viewModel(),
    onChapterClick: (Chapter) -> Unit
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
                    ChapterList(chapters = s.data, refreshTrigger = refreshTrigger, onChapterClick = onChapterClick)
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
private fun ChapterList(
    chapters: List<Chapter>,
    refreshTrigger: Int,
    onChapterClick: (Chapter) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ReadingStatsHeader(refreshTrigger = refreshTrigger)
            Spacer(Modifier.height(4.dp))
        }
        
        itemsIndexed(chapters) { index, chapter ->
            ChapterCard(chapter = chapter, onClick = { onChapterClick(chapter) })
        }
        item { Spacer(Modifier.height(16.dp)) }
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
            // Chapter number badge
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

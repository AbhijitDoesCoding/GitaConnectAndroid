package com.gitaconnect.app.library.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_book),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ---- Gradient Header ----
            GitaLibraryHeader()

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
                    ChapterList(chapters = s.data, onChapterClick = onChapterClick)
                }
            }
        }
    }
}

@Composable
private fun GitaLibraryHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3D2B1F),
                        Color(0xFF7B4F2E),
                        GitaSaffron.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFFFFF8EE),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "श्रीमद्भगवद्गीता",
                    color = Color(0xFFFFF8EE).copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Bhagavad Gita",
                color = Color(0xFFFFF8EE),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "18 Chapters · Sacred Knowledge",
                color = Color(0xFFFFF8EE).copy(alpha = 0.65f),
                fontSize = 13.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun ChapterList(
    chapters: List<Chapter>,
    onChapterClick: (Chapter) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(chapters) { index, chapter ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300 + index * 40)) +
                        slideInVertically(tween(300 + index * 40)) { it / 4 }
            ) {
                ChapterCard(chapter = chapter, onClick = { onChapterClick(chapter) })
            }
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

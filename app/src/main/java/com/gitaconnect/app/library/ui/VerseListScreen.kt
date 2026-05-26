package com.gitaconnect.app.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.gitaconnect.app.R
import com.gitaconnect.app.library.models.Chapter
import com.gitaconnect.app.library.models.Verse
import com.gitaconnect.app.library.viewmodel.GitaLibraryViewModel
import com.gitaconnect.app.library.viewmodel.GitaUiState

// ---------------------------------------------------------------------------
// Verse List Screen
// No nested Scaffold — parent MainScreen Scaffold already handles insets.
// The dark header spans behind the status bar for immersive look.
// ---------------------------------------------------------------------------

@Composable
fun VerseListScreen(
    chapter: Chapter,
    viewModel: GitaLibraryViewModel,
    onVerseClick: (Verse, List<Verse>) -> Unit,
    onBack: () -> Unit
) {
    // Load verses whenever this screen is shown with a new chapter
    LaunchedEffect(chapter.chapterNumber) {
        viewModel.loadVerses(chapter.chapterNumber)
    }

    val state by viewModel.versesState.collectAsState()

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
            // Immersive dark header with back button
            VerseListHeader(chapter = chapter, onBack = onBack)

            // Content area
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
                    VerseList(
                        verses = s.data,
                        onVerseClick = { verse -> onVerseClick(verse, s.data) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerseListHeader(chapter: Chapter, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3D2B1F).copy(alpha=0.95f), Color(0xFF7B4F2E).copy(alpha=0.9f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFFFF8EE)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Chapter ${chapter.chapterNumber}",
                color = GitaSaffronLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
            Text(
                text = chapter.title,
                color = Color(0xFFFFF8EE),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            if (!chapter.subtitle.isNullOrBlank()) {
                Text(
                    text = chapter.subtitle,
                    color = Color(0xFFFFF8EE).copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun VerseList(
    verses: List<Verse>,
    onVerseClick: (Verse) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(verses) { index, verse ->
            VerseListItem(
                verse = verse,
                index = index,
                onClick = { onVerseClick(verse) }
            )
        }
    }
}

@Composable
private fun VerseListItem(verse: Verse, index: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verse number pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GitaSaffron.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${verse.chapter}.${verse.verseId}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GitaSaffron
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Verse ${verse.verseId}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GitaCharcoal
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = verse.englishTranslation,
                    fontSize = 13.sp,
                    color = GitaCharcoalSoft,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Read Verse",
                tint = GitaSaffron.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

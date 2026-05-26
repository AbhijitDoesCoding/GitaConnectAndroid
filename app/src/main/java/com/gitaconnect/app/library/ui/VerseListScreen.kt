package com.gitaconnect.app.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.compose.foundation.BorderStroke

// ---------------------------------------------------------------------------
// Verse List Screen
// No nested Scaffold — parent MainScreen Scaffold already handles insets.
// The dark header spans behind the status bar for immersive look.
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
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
            // Immersive clean header with back button
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
                        chapter = chapter,
                        verses = s.data,
                        refreshTrigger = refreshTrigger,
                        onVerseClick = { verse -> onVerseClick(verse, s.data) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseListHeader(chapter: Chapter, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Chapter ${chapter.chapterNumber}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GitaCharcoal
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = GitaCharcoal
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun ChapterEssenceCard(chapter: Chapter, verses: List<Verse>, refreshTrigger: Int) {
    var isExpanded by remember { mutableStateOf(false) }

    val readCount = remember(verses, refreshTrigger) {
        verses.count { com.gitaconnect.app.library.services.ReadingProgressManager.isVerseRead(chapter.chapterNumber, it.verseId) }
    }
    val remainingCount = (chapter.verseCount - readCount).coerceAtLeast(0)
    val completedPercentage = if (chapter.verseCount > 0) {
        (readCount * 100) / chapter.verseCount
    } else {
        0
    }

    Card(
        onClick = { isExpanded = !isExpanded },
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
            Text(
                text = "Chapter Essence",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GitaCharcoalSoft
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${chapter.chapterNumber}. ${chapter.title}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GitaCharcoal
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            // Expandable description
            val essenceText = if (!chapter.subtitle.isNullOrBlank()) {
                chapter.subtitle
            } else {
                "Explore the sacred teachings and verses of Chapter ${chapter.chapterNumber} of the Bhagavad Gita."
            }
            
            Text(
                text = essenceText,
                fontSize = 15.sp,
                color = GitaCharcoalSoft,
                maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Hint label "Read more" / "Show less"
            Text(
                text = if (isExpanded) "Show less" else "Read more",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GitaSaffron
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(
                color = GitaDivider,
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row: Read, Remaining, Completed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatBlock(value = "$readCount", label = "Read")
                StatBlock(value = "$remainingCount", label = "Remaining")
                StatBlock(value = "$completedPercentage%", label = "Completed")
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
private fun VerseList(
    chapter: Chapter,
    verses: List<Verse>,
    refreshTrigger: Int,
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
        item {
            ChapterEssenceCard(chapter = chapter, verses = verses, refreshTrigger = refreshTrigger)
            Spacer(Modifier.height(8.dp))
        }
        
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
        border = BorderStroke(1.dp, Color(0xFFE8DFC8)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

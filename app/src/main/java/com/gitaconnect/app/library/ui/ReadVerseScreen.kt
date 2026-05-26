package com.gitaconnect.app.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_book),
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
            onPlayPauseClick = {
                mediaController?.let { controller ->
                    if (isPlaying) {
                        controller.pause()
                    } else {
                        controller.play()
                    }
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

            // Section 2: English Translation
            VerseSection(label = "Translation") {
                Text(
                    text = verse.englishTranslation,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = GitaCharcoal,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Start
                )
            }

            // Section 3: Reflection
            verse.essence?.reflection?.takeIf { it.isNotBlank() && it != "None." }
                ?.let { reflection ->
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
                                text = reflection,
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
            verse.essence?.krishnaGuidance
                ?.takeIf { it.isNotBlank() && it != "None." }
                ?.let { guidance ->
                    Spacer(Modifier.height(16.dp))
                    GitaDividerLine()
                    Spacer(Modifier.height(16.dp))

                    VerseSection(label = "Krishna's Guidance") {
                        Text(
                            text = guidance,
                            fontSize = 15.sp,
                            color = GitaCharcoal,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }

            Spacer(Modifier.height(24.dp))
        }

        // ---- Bottom Navigation Bar ----
        ReadVerseNavBar(
            currentIndex = currentIndex,
            totalVerses = allVerses.size,
            onPrev = { if (currentIndex > 0) currentIndex-- },
            onNext = { if (currentIndex < allVerses.size - 1) currentIndex++ }
        )
    }
    }
}

@Composable
private fun ReadVerseTopBar(
    verse: Verse, 
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPlayPauseClick: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 2.dp
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
                    .padding(end = 12.dp)
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
        }
    }
}

@Composable
private fun ReadVerseNavBar(
    currentIndex: Int,
    totalVerses: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 4.dp
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

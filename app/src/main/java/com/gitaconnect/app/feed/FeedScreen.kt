@file:Suppress("SpellCheckingInspection")
package com.gitaconnect.app.feed

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// FeedScreen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel = viewModel()) {
    val context = LocalContext.current

    // ---- State from ViewModel ----
    val feedItems by viewModel.feedItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Map from page index → borrowed ExoPlayer
    val players = remember { mutableMapOf<Int, ExoPlayer>() }

    // ---- Pager ----
    val pagerState = rememberPagerState(pageCount = { feedItems.size })

    // ---- Autoplay: observe current page and play/pause accordingly ----
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage ->
            players.forEach { (index, player) ->
                if (index == currentPage) player.play() else player.pause()
            }
        }
    }

    // ---- Pause everything and return players when screen leaves composition ----
    DisposableEffect(Unit) {
        onDispose {
            players.values.forEach { VideoPlayerPool.returnPlayer(it) }
            players.clear()
        }
    }

    // ---- UI ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val item = feedItems[page]
                FeedPageItem(
                    item = item,
                    isCurrentPage = page == pagerState.currentPage,
                    context = context,
                    onPlayerReady = { player -> players[page] = player },
                    onPlayerDisposed = { players.remove(page) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FeedPageItem – one full-screen video card
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun FeedPageItem(
    item: FeedItem,
    isCurrentPage: Boolean,
    context: Context,
    onPlayerReady: (ExoPlayer) -> Unit,
    onPlayerDisposed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showSheet = remember { mutableStateOf(false) }

    // ---- Obtain a player from the pool ----
    val player = remember {
        VideoPlayerPool.getPlayer(context).also { exoPlayer ->
            item.videoURL?.let { url ->
                exoPlayer.setMediaItem(MediaItem.fromUri(url))
                exoPlayer.prepare()
            }
            onPlayerReady(exoPlayer)
        }
    }

    // ---- App Lifecycle Control (onPause/onResume) & Autoplay ----
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isCurrentPage) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (isCurrentPage) player.play()
                Lifecycle.Event.ON_PAUSE -> player.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        // Also trigger play/pause immediately when page visibility changes
        if (isCurrentPage && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            player.play()
        } else {
            player.pause()
        }
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ---- Return player to pool when this composable leaves composition ----
    DisposableEffect(item.id) {
        onDispose {
            VideoPlayerPool.returnPlayer(player)
            onPlayerDisposed()
        }
    }

    // ---- Like State (isolated boolean tracker) ----
    var isLiked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---- Video surface (double-tap = like) ----
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            isLiked = !isLiked
                            onLikeItem(item)
                        }
                    )
                }
        )

        // ---- Right-side overlay buttons ----
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Like Toggle
            IconButton(
                onClick = {
                    isLiked = !isLiked
                    onLikeItem(item)
                },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            // Share (single tap)
            IconButton(
                onClick = { shareItem(context, item) },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Info / Learnings
            IconButton(
                onClick = {
                    if (!item.learnings.isNullOrBlank()) {
                        scope.launch { showSheet.value = true }
                    }
                },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Learnings",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // ---- Title overlay bottom-left ----
        if (!item.title.isNullOrBlank()) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 48.dp, end = 72.dp)
            )
        }
    }

    // ---- Learnings ModalBottomSheet ----
    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1C1C1E)
        ) {
            Text(
                text = item.learnings ?: "",
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Action helpers
// ---------------------------------------------------------------------------

/** Like action — wire to your Supabase backend here. */
private fun onLikeItem(item: FeedItem) {
    // TODO: scope.launch { SupabaseManager.client.from("likes").insert(...) }
    println("Liked item: ${item.id}")
}

/** Opens Android's native share sheet with the video URL. */
private fun shareItem(context: Context, item: FeedItem) {
    if (item.videoURL.isNullOrBlank()) return
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, item.videoURL)
        putExtra(Intent.EXTRA_SUBJECT, item.title ?: "Check this out!")
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
}

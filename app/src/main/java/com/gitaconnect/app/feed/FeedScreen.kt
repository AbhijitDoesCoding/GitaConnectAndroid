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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Supabase stub – replace with your real SupabaseManager calls.
// ---------------------------------------------------------------------------

/** Placeholder that you can swap for a real Supabase client later. */
object SupabaseManager {
    // e.g. val client = createSupabaseClient(url, key) { install(Postgrest) }
}

/**
 * Fetches feed items from the backend.
 * Replace the stub list with a real Supabase Postgrest select call.
 */
suspend fun fetchFeedData(): List<FeedItem> {
    // TODO: val result = SupabaseManager.client
    //     .from("feed_items")
    //     .select()
    //     .decodeList<FeedItem>()
    // return result
    return listOf(
        FeedItem(
            id = "demo-1",
            videoURL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            title = "Big Buck Bunny",
            learnings = "This is a sample learning note for the first video.\n\nReplace this with real content from your Supabase table."
        ),
        FeedItem(
            id = "demo-2",
            videoURL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            title = "Elephants Dream",
            learnings = "Second demo video learning notes.\n\nWire up fetchFeedData() to your Supabase backend to see real content."
        )
    )
}

// ---------------------------------------------------------------------------
// FeedScreen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val context = LocalContext.current

    // ---- State ----
    val feedItems = remember { mutableStateListOf<FeedItem>() }
    var isLoading by remember { mutableStateOf(true) }

    // Map from page index → borrowed ExoPlayer
    val players = remember { mutableMapOf<Int, ExoPlayer>() }

    // ---- Load data on first composition ----
    LaunchedEffect(Unit) {
        val items = fetchFeedData()
        feedItems.addAll(items)
        isLoading = false
    }

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
    var showSheet by remember { mutableStateOf(false) }

    // ---- Obtain a player from the pool ----
    val player = remember {
        VideoPlayerPool.getPlayer(context).also { exoPlayer ->
            exoPlayer.setMediaItem(MediaItem.fromUri(item.videoURL))
            exoPlayer.prepare()
            onPlayerReady(exoPlayer)
        }
    }

    // ---- Play / pause based on page visibility ----
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) player.play() else player.pause()
    }

    // ---- Return player to pool when this composable leaves composition ----
    DisposableEffect(item.id) {
        onDispose {
            VideoPlayerPool.returnPlayer(player)
            onPlayerDisposed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---- Video surface (double-tap = like) ----
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
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
                        onDoubleTap = { onLikeItem(item) }
                    )
                }
        )

        // ---- Right-side overlay buttons ----
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        scope.launch { showSheet = true }
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
                    .padding(start = 16.dp, bottom = 96.dp, end = 72.dp)
            )
        }
    }

    // ---- Learnings ModalBottomSheet ----
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
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
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, item.videoURL)
        putExtra(Intent.EXTRA_SUBJECT, item.title ?: "Check this out!")
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
}

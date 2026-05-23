package com.gitaconnect.app.feed

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * A singleton pool that recycles up to [maxPoolSize] ExoPlayer instances to
 * avoid the overhead of creating and destroying players on every page scroll.
 *
 * Usage:
 *   val player = VideoPlayerPool.getPlayer(context)   // borrow
 *   VideoPlayerPool.returnPlayer(player)              // return when done
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
object VideoPlayerPool {

    const val maxPoolSize = 5

    // Simple ArrayDeque used as a LIFO stack so the most-recently-returned
    // player (still warm) is reused first.
    private val pool = ArrayDeque<ExoPlayer>(maxPoolSize)

    /**
     * Returns an [ExoPlayer] from the pool if one is available, otherwise
     * creates a new instance using the supplied [context].
     */
    fun getPlayer(context: Context): ExoPlayer {
        return if (pool.isNotEmpty()) {
            pool.removeLast()
        } else {
            ExoPlayer.Builder(context.applicationContext).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 1f
            }
        }
    }

    /**
     * Returns a borrowed [player] to the pool.
     *
     * - Pauses playback immediately.
     * - Clears the loaded media item so the player is ready for re-use.
     * - If the pool is already full the player is released instead of leaked.
     */
    fun returnPlayer(player: ExoPlayer) {
        player.pause()
        player.clearMediaItems()

        if (pool.size < maxPoolSize) {
            pool.addLast(player)
        } else {
            player.release()
        }
    }

    /**
     * Releases all pooled players. Call this when the host component (e.g. a
     * ViewModel) is cleared to avoid resource leaks.
     */
    fun releaseAll() {
        pool.forEach { it.release() }
        pool.clear()
    }
}

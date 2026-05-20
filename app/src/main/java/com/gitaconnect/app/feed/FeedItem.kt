package com.gitaconnect.app.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single video item in the feed.
 *
 * Mapped from the backend JSON with:
 *  - "feed_item_id" → [id]
 *  - "video_url"    → [videoURL]
 */
@Serializable
data class FeedItem(
    @SerialName("feed_item_id") val id: Long,
    @SerialName("video_url") val videoURL: String? = null,
    val title: String? = null,
    val learnings: String? = null
)

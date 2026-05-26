package com.gitaconnect.app.library.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root wrapper that maps the top-level "chapters" array in "Bhagavad Gita.json".
 */
@Serializable
data class GitaRoot(
    val chapters: List<Chapter>
)

/**
 * Represents a single chapter's metadata (from the root JSON).
 * Each chapter also embeds the full verse list in the root JSON,
 * but we use dedicated Chapter_{n}.json files for verses for efficiency.
 */
@Serializable
data class Chapter(
    @SerialName("chapter_number") val chapterNumber: Int,
    val title: String,
    val subtitle: String? = null,
    @SerialName("verse_count") val verseCount: Int
)

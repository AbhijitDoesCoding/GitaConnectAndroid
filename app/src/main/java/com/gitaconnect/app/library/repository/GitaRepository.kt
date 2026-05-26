package com.gitaconnect.app.library.repository

import android.content.Context
import com.gitaconnect.app.library.models.Chapter
import com.gitaconnect.app.library.models.GitaRoot
import com.gitaconnect.app.library.models.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Repository responsible for reading and parsing Bhagavad Gita JSON files
 * from the Android assets folder. All IO operations run on [Dispatchers.IO].
 */
class GitaRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true   // Safely ignore extra keys (like multi-lang fields)
        isLenient = true
        coerceInputValues = true
    }

    // In-memory cache so we don't re-parse on every call
    private var cachedChapters: List<Chapter>? = null
    private val cachedVerses = mutableMapOf<Int, List<Verse>>()

    /**
     * Reads "Bhagavad Gita.json" from assets and returns the list of all 18 chapters.
     */
    suspend fun getAllChapters(): List<Chapter> = withContext(Dispatchers.IO) {
        cachedChapters?.let { return@withContext it }

        val jsonString = context.assets
            .open("Bhagavad Gita.json")
            .bufferedReader()
            .use { it.readText() }

        val root = json.decodeFromString<GitaRoot>(jsonString)
        root.chapters.also { cachedChapters = it }
    }

    /**
     * Reads "Chapter_{chapterNumber}.json" from assets and returns the list of verses.
     */
    suspend fun getVersesForChapter(chapterNumber: Int): List<Verse> = withContext(Dispatchers.IO) {
        cachedVerses[chapterNumber]?.let { return@withContext it }

        val jsonString = context.assets
            .open("Chapter_$chapterNumber.json")
            .bufferedReader()
            .use { it.readText() }

        val verses = json.decodeFromString<List<Verse>>(jsonString)
        verses.also { cachedVerses[chapterNumber] = it }
    }
}

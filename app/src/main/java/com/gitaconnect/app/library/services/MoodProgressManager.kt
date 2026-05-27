package com.gitaconnect.app.library.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object MoodProgressManager {
    private const val PREFS_NAME = "bhagavad_gita_mood_prefs"
    private const val KEY_COMPLETION_DATE = "MoodCompletionDate"
    private const val KEY_MOOD_LEVEL = "MoodSelectedLevelKey"
    private const val KEY_VERSE_CHAPTER = "MoodSelectedVerseChapter"
    private const val KEY_VERSE_ID = "MoodSelectedVerseId"
    private const val KEY_VERSE_SANSKRIT = "MoodSelectedVerseSanskrit"
    private const val KEY_VERSE_ENGLISH = "MoodSelectedVerseEnglish"

    private lateinit var prefs: SharedPreferences
    private val json = Json { ignoreUnknownKeys = true }

    private var moodQuestions: MoodContent? = null
    private var moodVerseMapping: VerseMappingData? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadAssets(context)
    }

    private fun loadAssets(context: Context) {
        try {
            val questionsStr = context.assets.open("mood_questions.json").bufferedReader().use { it.readText() }
            moodQuestions = json.decodeFromString<MoodContent>(questionsStr)

            val mappingStr = context.assets.open("mood_verse_mapping.json").bufferedReader().use { it.readText() }
            moodVerseMapping = json.decodeFromString<VerseMappingData>(mappingStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isMoodCompletedToday(): Boolean {
        val savedDate = prefs.getString(KEY_COMPLETION_DATE, null)
        return savedDate == getCurrentDateString()
    }

    fun saveMoodCompletion(chapter: Int, verseId: Int, sanskrit: String, english: String, moodKey: String) {
        prefs.edit().apply {
            putString(KEY_COMPLETION_DATE, getCurrentDateString())
            putString(KEY_MOOD_LEVEL, moodKey)
            putInt(KEY_VERSE_CHAPTER, chapter)
            putInt(KEY_VERSE_ID, verseId)
            putString(KEY_VERSE_SANSKRIT, sanskrit)
            putString(KEY_VERSE_ENGLISH, english)
            apply()
        }
    }

    fun getRecommendedVerseInfo(): RecommendedVerseInfo? {
        if (!isMoodCompletedToday()) {
            clearMoodState()
            return null
        }
        val chapter = prefs.getInt(KEY_VERSE_CHAPTER, 0)
        val verseId = prefs.getInt(KEY_VERSE_ID, 0)
        val sanskrit = prefs.getString(KEY_VERSE_SANSKRIT, null)
        val english = prefs.getString(KEY_VERSE_ENGLISH, null)
        val moodKey = prefs.getString(KEY_MOOD_LEVEL, null)

        if (chapter == 0 || verseId == 0 || sanskrit == null || english == null || moodKey == null) {
            return null
        }
        return RecommendedVerseInfo(chapter, verseId, sanskrit, english, moodKey)
    }

    fun clearMoodState() {
        prefs.edit().apply {
            remove(KEY_COMPLETION_DATE)
            remove(KEY_MOOD_LEVEL)
            remove(KEY_VERSE_CHAPTER)
            remove(KEY_VERSE_ID)
            remove(KEY_VERSE_SANSKRIT)
            remove(KEY_VERSE_ENGLISH)
            apply()
        }
    }

    fun getQuestionForScore(score: Double): MoodQuestion {
        val level = PleasantnessLevel.fromScore(score)
        val fallback = MoodQuestion(
            id = "fallback",
            question = "How are you feeling?",
            options = listOf("Not good", "Okay", "Great")
        )
        val content = moodQuestions ?: return fallback
        val questions = when (level) {
            PleasantnessLevel.VERY_UNPLEASANT -> content.veryUnpleasant
            PleasantnessLevel.UNPLEASANT -> content.unpleasant
            PleasantnessLevel.NEUTRAL -> content.neutral
            PleasantnessLevel.PLEASANT -> content.pleasant
            PleasantnessLevel.VERY_PLEASANT -> content.veryPleasant
        }
        if (questions.isEmpty()) return fallback
        return questions[Random.nextInt(questions.size)]
    }

    fun getFallbackVerseForMood(moodKey: String): Pair<Int, Int>? {
        val mapping = moodVerseMapping ?: return null
        val list = when (moodKey) {
            "veryUnpleasant" -> mapping.veryUnpleasant
            "unpleasant" -> mapping.unpleasant
            "neutral" -> mapping.neutral
            "pleasant" -> mapping.pleasant
            "veryPleasant" -> mapping.veryPleasant
            else -> emptyList()
        }
        if (list.isEmpty()) return null
        val selected = list[Random.nextInt(list.size)]
        return Pair(selected.chapter, selected.verse)
    }

    fun getFullMappingJSON(context: Context): String {
        return try {
            context.assets.open("mood_verse_mapping.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "{}"
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    @Serializable
    data class MoodQuestion(
        val id: String,
        val question: String,
        val options: List<String>
    )

    @Serializable
    data class MoodContent(
        val veryUnpleasant: List<MoodQuestion>,
        val unpleasant: List<MoodQuestion>,
        val neutral: List<MoodQuestion>,
        val pleasant: List<MoodQuestion>,
        val veryPleasant: List<MoodQuestion>
    )

    @Serializable
    data class VerseReference(
        val chapter: Int,
        val verse: Int
    )

    @Serializable
    data class VerseMappingData(
        val veryUnpleasant: List<VerseReference>,
        val unpleasant: List<VerseReference>,
        val neutral: List<VerseReference>,
        val pleasant: List<VerseReference>,
        val veryPleasant: List<VerseReference>
    )

    data class RecommendedVerseInfo(
        val chapter: Int,
        val verseId: Int,
        val sanskrit: String,
        val english: String,
        val moodKey: String
    )

    enum class PleasantnessLevel(val rawValue: Int, val key: String) {
        VERY_UNPLEASANT(1, "veryUnpleasant"),
        UNPLEASANT(2, "unpleasant"),
        NEUTRAL(3, "neutral"),
        PLEASANT(4, "pleasant"),
        VERY_PLEASANT(5, "veryPleasant");

        companion object {
            fun fromScore(score: Double): PleasantnessLevel {
                return when (score) {
                    in 1.0..2.999 -> VERY_UNPLEASANT
                    in 3.0..4.999 -> UNPLEASANT
                    in 5.0..6.999 -> NEUTRAL
                    in 7.0..8.999 -> PLEASANT
                    in 9.0..10.0 -> VERY_PLEASANT
                    else -> NEUTRAL
                }
            }
        }
    }
}

package com.gitaconnect.app.library.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

object ReadingProgressManager {
    private const val PREFS_NAME = "bhagavad_gita_reading_progress_prefs"
    private const val PROGRESS_KEY = "bhagavad_gita_reading_progress"

    private lateinit var prefs: SharedPreferences
    private var readVersesMap = mutableMapOf<String, Long>() // "chapter:verse" -> timestamp

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadProgress()
    }

    private fun loadProgress() {
        val jsonStr = prefs.getString(PROGRESS_KEY, null)
        if (jsonStr != null) {
            try {
                readVersesMap = Json.decodeFromString<Map<String, Long>>(jsonStr).toMutableMap()
            } catch (e: Exception) {
                readVersesMap = mutableMapOf()
            }
        } else {
            readVersesMap = mutableMapOf()
        }
    }

    private fun saveProgress() {
        try {
            val jsonStr = Json.encodeToString(readVersesMap.toMap())
            prefs.edit().putString(PROGRESS_KEY, jsonStr).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun markVerseRead(chapter: Int, verse: Int) {
        val key = "$chapter:$verse"
        if (!readVersesMap.containsKey(key)) {
            readVersesMap[key] = System.currentTimeMillis()
            saveProgress()
        }
    }

    fun unmarkVerseRead(chapter: Int, verse: Int) {
        val key = "$chapter:$verse"
        if (readVersesMap.containsKey(key)) {
            readVersesMap.remove(key)
            saveProgress()
        }
    }

    fun isVerseRead(chapter: Int, verse: Int): Boolean {
        val key = "$chapter:$verse"
        return readVersesMap.containsKey(key)
    }

    fun getStats(): Stats {
        val all = readVersesMap
        val totalVerses = all.size

        val chapterIdentifiers = all.keys.mapNotNull { it.split(":").firstOrNull()?.toIntOrNull() }.toSet()
        val totalChapters = chapterIdentifiers.size

        val todayStart = getStartOfDay().timeInMillis
        val todayCount = all.values.filter { it >= todayStart }.size

        val weekStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
        val weekCount = all.values.filter { it >= weekStart }.size

        val monthStart = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
        val monthCount = all.values.filter { it >= monthStart }.size

        return Stats(totalVerses, totalChapters, todayCount, weekCount, monthCount)
    }

    private fun getStartOfDay(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    data class Stats(
        val totalVerses: Int,
        val totalChapters: Int,
        val today: Int,
        val week: Int,
        val month: Int
    )
}

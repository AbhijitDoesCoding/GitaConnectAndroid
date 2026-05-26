package com.team18.gitaconnect

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun testParseChapters() {
        val file = java.io.File("src/main/assets/Bhagavad Gita.json")
        println("File path: ${file.absolutePath}, exists: ${file.exists()}")
        val jsonString = file.readText()
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
        try {
            val root = json.decodeFromString<com.gitaconnect.app.library.models.GitaRoot>(jsonString)
            println("Successfully parsed ${root.chapters.size} chapters!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
package com.gitaconnect.app.mentor

import com.gitaconnect.app.supabasecentral.SupabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SarvamAIService {

    suspend fun getCompletion(
        history: List<Map<String, String>>,
        language: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseManager.callSarvamAI(
                messages = history,
                temperature = 0.2,
                language = language,
                model = "sarvam-105b"
            )
            
            val content = response.choices?.firstOrNull()?.message?.content
            content?.sanitizedAIOutput()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun String.sanitizedAIOutput(): String {
        var result = this

        // Remove <system-reminder>...</system-reminder>
        result = result.replace(Regex("(?is)<system-reminder\\b[^>]*>.*?</system-reminder>"), "")

        // Remove <think>...</think>
        result = result.replace(Regex("(?is)<think\\b[^>]*>.*?</think>"), "")

        // Remove <analysis>...</analysis>
        result = result.replace(Regex("(?is)<analysis\\b[^>]*>.*?</analysis>"), "")

        val metaBlockPatterns = listOf(
            "(?im)^\\s*thinking\\s*:\\s*.*(?:\\n[ \\t].*)*",
            "(?im)^\\s*reasoning\\s*:\\s*.*(?:\\n[ \\t].*)*",
            "(?im)^\\s*chain\\s+of\\s+thought\\s*:\\s*.*(?:\\n[ \\t].*)*",
            "(?im)^\\s*internal\\s+notes\\s*:\\s*.*(?:\\n[ \\t].*)*"
        )

        metaBlockPatterns.forEach { pattern ->
            result = result.replace(Regex(pattern), "")
        }

        val strayTags = listOf(
            "<think>", "</think>",
            "<analysis>", "</analysis>",
            "<system-reminder>", "</system-reminder>"
        )

        strayTags.forEach { tag ->
            result = result.replace(tag, "", ignoreCase = true)
        }

        result = result
            .replace("```json", "")
            .replace("```", "")

        return result.trim()
    }
}

package com.gitaconnect.app.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class MentorViewModel : ViewModel() {

    private val maxAssistantResponseCharacters = 1200

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentLanguage = MutableStateFlow("English")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    val languages = listOf(
        "English", "Hindi", "Tamil", "Telugu",
        "Kannada", "Malayalam", "Marathi", "Gujarati", "Bengali",
        "Punjabi", "Odia"
    )

    private val languageCodes = mapOf(
        "English" to "en-IN",
        "Hindi" to "hi-IN",
        "Tamil" to "ta-IN",
        "Telugu" to "te-IN",
        "Kannada" to "kn-IN",
        "Malayalam" to "ml-IN",
        "Marathi" to "mr-IN",
        "Gujarati" to "gu-IN",
        "Bengali" to "bn-IN",
        "Punjabi" to "pa-IN",
        "Odia" to "or-IN"
    )

    init {
        loadInitialMessages()
    }

    fun setLanguage(index: Int) {
        if (index in languages.indices) {
            _currentLanguage.value = languages[index]
        }
    }

    fun refresh() {
        _messages.value = emptyList()
        loadInitialMessages()
    }

    private fun loadInitialMessages() {
        val initialMessage = Message(
            text = "Welcome, dear seeker. How can I guide you through the wisdom of the Gita today?",
            isFromUser = false
        )
        _messages.value = listOf(initialMessage)
    }

    fun sendMessage(text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return

        val userMessage = Message(text = trimmedText, isFromUser = true)
        val thinkingMessage = Message(text = "", isFromUser = false, isThinking = true)

        _messages.update { it + userMessage + thinkingMessage }

        viewModelScope.launch {
            getAIResponse()
        }
    }

    private suspend fun getAIResponse() {
        val history = prepareChatHistory()
        if (history.size <= 1) {
            removeThinkingMessage()
            return
        }

        val languageCode = languageCodes[_currentLanguage.value] ?: "en-IN"
        val responseText = SarvamAIService.getCompletion(history, languageCode)
        removeThinkingMessage()

        if (responseText != null) {
            val sanitizedResponse = sanitizedAssistantResponse(responseText)
            if (sanitizedResponse.isEmpty()) {
                val fallbackMsg = "I am here with you. Please share that once more in a few words."
                _messages.update { it + Message(text = fallbackMsg, isFromUser = false) }
                return
            }

            val verseRef = extractVerseReference(sanitizedResponse)
            val assistantMessage = Message(
                text = sanitizedResponse,
                isFromUser = false,
                verseChapter = verseRef?.first,
                verseNumber = verseRef?.second
            )
            _messages.update { it + assistantMessage }
        } else {
            val errorMsg = "My reflections are clouded. Please ask again."
            _messages.update { it + Message(text = errorMsg, isFromUser = false) }
        }
    }

    private fun removeThinkingMessage() {
        _messages.update { list -> list.filterNot { it.isThinking } }
    }

    private fun prepareChatHistory(): List<Map<String, String>> {
        val history = mutableListOf<Map<String, String>>()
        val systemPrompt = SpiritualMentorPromptBuilder.buildSystemPrompt(currentLanguage.value)
        history.add(mapOf("role" to "system", "content" to systemPrompt))

        var hasAddedFirstUserMessage = false
        for (message in _messages.value) {
            if (message.isThinking) continue
            val trimmedText = message.text.trim()
            if (trimmedText.isNotEmpty()) {
                val role = if (message.isFromUser) "user" else "assistant"
                if (!hasAddedFirstUserMessage && role != "user") continue
                if (role == "user") hasAddedFirstUserMessage = true
                history.add(mapOf("role" to role, "content" to trimmedText))
            }
        }

        if (history.size <= 1) return emptyList()
        if (history.size > 10) {
            val systemMessage = history.first()
            val recentMessages = history.takeLast(7)
            return listOf(systemMessage) + recentMessages
        }
        return history
    }

    private fun extractVerseReference(text: String): Pair<Int, Int>? {
        val chapterVersePattern = Regex("(?i)chapter\\s*(\\d{1,2})\\s*[,\\-:]?\\s*verse\\s*(\\d{1,3})")
        val compactPattern = Regex("(?i)(?:bhagavad\\s*gita\\s*)?(\\d{1,2})\\s*[\\.:]\\s*(\\d{1,3})")

        val patterns = listOf(chapterVersePattern, compactPattern)

        for (pattern in patterns) {
            val matchResult = pattern.find(text)
            if (matchResult != null && matchResult.groupValues.size >= 3) {
                val chapter = matchResult.groupValues[1].toIntOrNull()
                val verse = matchResult.groupValues[2].toIntOrNull()
                if (chapter != null && verse != null) {
                    return Pair(chapter, verse)
                }
            }
        }
        return null
    }

    private fun sanitizedAssistantResponse(text: String): String {
        var normalized = text.replace("\r\n", "\n").replace("\r", "\n").trim()

        if (!lastUserExplicitlyRequestedOriginalVerseText()) {
            normalized = removeUnrequestedSanskritOrTransliteration(normalized)
        }

        while (normalized.contains("\n\n\n")) {
            normalized = normalized.replace("\n\n\n", "\n\n")
        }

        normalized = withSectionSpacing(normalized)

        var wasTrimmed = false
        if (normalized.length > maxAssistantResponseCharacters) {
            normalized = trimmedAtNaturalBoundary(normalized, maxAssistantResponseCharacters)
            wasTrimmed = true
        }

        return ensureReadableEnding(normalized, wasTrimmed)
    }

    private fun withSectionSpacing(text: String): String {
        val labels = listOf("Translation:", "Reflection:", "Action:")
        var updated = text
        for (label in labels) {
            updated = updated.replace("\n$label", "\n\n$label")
        }
        return updated
    }

    private fun lastUserExplicitlyRequestedOriginalVerseText(): Boolean {
        val lastUserMessage = _messages.value.lastOrNull { it.isFromUser }?.text?.lowercase() ?: return false
        val explicitRequestPhrases = listOf(
            "sanskrit", "transliteration", "devanagari", "original shloka", "original sloka", "original verse"
        )
        return explicitRequestPhrases.any { lastUserMessage.contains(it) }
    }

    private fun removeUnrequestedSanskritOrTransliteration(text: String): String {
        return text.lines().filter { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@filter true
            if (containsDevanagari(trimmed)) return@filter false
            val lower = trimmed.lowercase()
            if (lower.startsWith("sanskrit:") || lower.startsWith("transliteration:") ||
                lower.startsWith("shloka:") || lower.startsWith("sloka:")) {
                return@filter false
            }
            true
        }.joinToString("\n").trim()
    }

    private fun containsDevanagari(text: String): Boolean {
        return text.any { it.code in 0x0900..0x097F }
    }

    private fun trimmedAtNaturalBoundary(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text

        val prefix = text.take(maxLength)
        val sentenceEndChars = setOf('.', '?', '!', '।')

        val sentenceRegex = Regex("[\\.\\?\\!।]\\s*")
        val matches = sentenceRegex.findAll(prefix).toList()
        if (matches.isNotEmpty()) {
            val lastMatch = matches.last()
            if (lastMatch.range.first > (prefix.length - 180).coerceAtLeast(0)) {
                return prefix.substring(0, lastMatch.range.last + 1).trim()
            }
        }

        val lastNewline = prefix.lastIndexOf('\n')
        if (lastNewline != -1 && prefix.length - lastNewline < 140) {
            return prefix.substring(0, lastNewline).trim()
        }

        val lastSpace = prefix.lastIndexOf(' ')
        if (lastSpace != -1 && prefix.length - lastSpace < 80) {
            return prefix.substring(0, lastSpace).trim()
        }

        var fallback = prefix.trim()
        if (fallback.isNotEmpty() && !sentenceEndChars.contains(fallback.last())) {
            fallback += "."
        }
        return fallback
    }

    private fun ensureReadableEnding(text: String, wasTrimmed: Boolean): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return trimmed

        val punctuation = setOf('.', '?', '!', '।', '…')
        if (punctuation.contains(trimmed.last())) {
            return trimmed
        }

        return if (wasTrimmed) "$trimmed." else trimmed
    }
}

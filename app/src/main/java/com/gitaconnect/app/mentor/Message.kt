package com.gitaconnect.app.mentor

import java.util.Date

data class Message(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Date = Date(),
    val verseChapter: Int? = null,
    val verseNumber: Int? = null,
    val isThinking: Boolean = false
)

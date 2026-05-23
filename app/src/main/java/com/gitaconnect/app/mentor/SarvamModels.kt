package com.gitaconnect.app.mentor

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SarvamEdgeRequest(
    val messages: List<Map<String, String>>,
    val temperature: Double = 0.3,
    val language: String = "en-IN",
    val model: String = "sarvam-105b",
    @SerialName("max_tokens") val maxTokens: Int? = 900
)

@Serializable
data class SarvamChatResponse(
    val choices: List<Choice>? = null
) {
    @Serializable
    data class Choice(
        val message: Message
    ) {
        @Serializable
        data class Message(
            val content: String
        )
    }
}

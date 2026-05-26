package com.gitaconnect.app.library.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single verse, mapping from Chapter_N.json.
 * Equivalent to the Swift Verse model.
 */
@Serializable
data class Verse(
    val chapter: Int,
    @SerialName("verse_id") val verseId: Int,
    @SerialName("sanskrit_verse") val sanskritVerse: String,
    @SerialName("english_translation") val englishTranslation: String,
    val essence: Essence? = null,
    // Optional multi-language fields
    @SerialName("hindi_translation") val hindiTranslation: String? = null
)

/**
 * The reflective guidance embedded in each verse's "essence" object.
 */
@Serializable
data class Essence(
    @SerialName("krishna_guidance") val krishnaGuidance: String? = null,
    val reflection: String? = null
)

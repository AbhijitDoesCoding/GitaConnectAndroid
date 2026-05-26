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
    
    @SerialName("hindi_translation") val hindiTranslation: String? = null,
    @SerialName("sanskrit_translation") val sanskritTranslation: String? = null,
    @SerialName("tamil_translation") val tamilTranslation: String? = null,
    @SerialName("telugu_translation") val teluguTranslation: String? = null,
    @SerialName("kannada_translation") val kannadaTranslation: String? = null,
    @SerialName("malayalam_translation") val malayalamTranslation: String? = null,
    @SerialName("marathi_translation") val marathiTranslation: String? = null,
    @SerialName("gujarati_translation") val gujaratiTranslation: String? = null,
    @SerialName("bengali_translation") val bengaliTranslation: String? = null,
    @SerialName("punjabi_translation") val punjabiTranslation: String? = null,
    @SerialName("odia_translation") val odiaTranslation: String? = null
)

/**
 * The reflective guidance embedded in each verse's "essence" object.
 */
@Serializable
data class Essence(
    @SerialName("krishna_guidance") val krishnaGuidance: String? = null,
    val reflection: String? = null,

    @SerialName("krishna_guidance_english") val krishnaGuidanceEnglish: String? = null,
    @SerialName("krishna_guidance_hindi") val krishnaGuidanceHindi: String? = null,
    @SerialName("krishna_guidance_sanskrit") val krishnaGuidanceSanskrit: String? = null,
    @SerialName("krishna_guidance_tamil") val krishnaGuidanceTamil: String? = null,
    @SerialName("krishna_guidance_telugu") val krishnaGuidanceTelugu: String? = null,
    @SerialName("krishna_guidance_kannada") val krishnaGuidanceKannada: String? = null,
    @SerialName("krishna_guidance_malayalam") val krishnaGuidanceMalayalam: String? = null,
    @SerialName("krishna_guidance_marathi") val krishnaGuidanceMarathi: String? = null,
    @SerialName("krishna_guidance_gujarati") val krishnaGuidanceGujarati: String? = null,
    @SerialName("krishna_guidance_bengali") val krishnaGuidanceBengali: String? = null,
    @SerialName("krishna_guidance_punjabi") val krishnaGuidancePunjabi: String? = null,
    @SerialName("krishna_guidance_odia") val krishnaGuidanceOdia: String? = null,

    @SerialName("reflection_english") val reflectionEnglish: String? = null,
    @SerialName("reflection_hindi") val reflectionHindi: String? = null,
    @SerialName("reflection_sanskrit") val reflectionSanskrit: String? = null,
    @SerialName("reflection_tamil") val reflectionTamil: String? = null,
    @SerialName("reflection_telugu") val reflectionTelugu: String? = null,
    @SerialName("reflection_kannada") val reflectionKannada: String? = null,
    @SerialName("reflection_malayalam") val reflectionMalayalam: String? = null,
    @SerialName("reflection_marathi") val reflectionMarathi: String? = null,
    @SerialName("reflection_gujarati") val reflectionGujarati: String? = null,
    @SerialName("reflection_bengali") val reflectionBengali: String? = null,
    @SerialName("reflection_punjabi") val reflectionPunjabi: String? = null,
    @SerialName("reflection_odia") val reflectionOdia: String? = null
)

import Foundation

struct ChapterVerse: Codable, Identifiable {
    var id: String { "\(chapter).\(verseId)" }

    let chapter: Int
    let verseId: Int
    let sanskritVerse: String
    let englishTranslation: String
    let essence: VerseEssence

    let hindiTranslation: String?
    let sanskritTranslation: String?
    let tamilTranslation: String?
    let teluguTranslation: String?
    let kannadaTranslation: String?
    let malayalamTranslation: String?
    let marathiTranslation: String?
    let gujaratiTranslation: String?
    let bengaliTranslation: String?
    let punjabiTranslation: String?
    let odiaTranslation: String?

    private enum CodingKeys: String, CodingKey {
        case chapter
        case verseId = "verse_id"
        case sanskritVerse = "sanskrit_verse"
        case englishTranslation = "english_translation"
        case essence

        case hindiTranslation = "hindi_translation"
        case sanskritTranslation = "sanskrit_translation"
        case tamilTranslation = "tamil_translation"
        case teluguTranslation = "telugu_translation"
        case kannadaTranslation = "kannada_translation"
        case malayalamTranslation = "malayalam_translation"
        case marathiTranslation = "marathi_translation"
        case gujaratiTranslation = "gujarati_translation"
        case bengaliTranslation = "bengali_translation"
        case punjabiTranslation = "punjabi_translation"
        case odiaTranslation = "odia_translation"
    }
    var verse: Int { verseId }
}

struct VerseEssence: Codable {
    let krishnaGuidance: String
    let reflection: String

    let krishnaGuidanceEnglish: String?
    let krishnaGuidanceHindi: String?
    let krishnaGuidanceSanskrit: String?
    let krishnaGuidanceTamil: String?
    let krishnaGuidanceTelugu: String?
    let krishnaGuidanceKannada: String?
    let krishnaGuidanceMalayalam: String?
    let krishnaGuidanceMarathi: String?
    let krishnaGuidanceGujarati: String?
    let krishnaGuidanceBengali: String?
    let krishnaGuidancePunjabi: String?
    let krishnaGuidanceOdia: String?

    let reflectionEnglish: String?
    let reflectionHindi: String?
    let reflectionSanskrit: String?
    let reflectionTamil: String?
    let reflectionTelugu: String?
    let reflectionKannada: String?
    let reflectionMalayalam: String?
    let reflectionMarathi: String?
    let reflectionGujarati: String?
    let reflectionBengali: String?
    let reflectionPunjabi: String?
    let reflectionOdia: String?

    private enum CodingKeys: String, CodingKey {
        case krishnaGuidance = "krishna_guidance"
        case reflection

        case krishnaGuidanceEnglish = "krishna_guidance_english"
        case krishnaGuidanceHindi = "krishna_guidance_hindi"
        case krishnaGuidanceSanskrit = "krishna_guidance_sanskrit"
        case krishnaGuidanceTamil = "krishna_guidance_tamil"
        case krishnaGuidanceTelugu = "krishna_guidance_telugu"
        case krishnaGuidanceKannada = "krishna_guidance_kannada"
        case krishnaGuidanceMalayalam = "krishna_guidance_malayalam"
        case krishnaGuidanceMarathi = "krishna_guidance_marathi"
        case krishnaGuidanceGujarati = "krishna_guidance_gujarati"
        case krishnaGuidanceBengali = "krishna_guidance_bengali"
        case krishnaGuidancePunjabi = "krishna_guidance_punjabi"
        case krishnaGuidanceOdia = "krishna_guidance_odia"

        case reflectionEnglish = "reflection_english"
        case reflectionHindi = "reflection_hindi"
        case reflectionSanskrit = "reflection_sanskrit"
        case reflectionTamil = "reflection_tamil"
        case reflectionTelugu = "reflection_telugu"
        case reflectionKannada = "reflection_kannada"
        case reflectionMalayalam = "reflection_malayalam"
        case reflectionMarathi = "reflection_marathi"
        case reflectionGujarati = "reflection_gujarati"
        case reflectionBengali = "reflection_bengali"
        case reflectionPunjabi = "reflection_punjabi"
        case reflectionOdia = "reflection_odia"
    }
}

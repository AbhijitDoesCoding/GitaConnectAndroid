import Foundation

struct BhagavadGita: Codable {
    let chapters: [ChapterData]
}

struct ChapterData: Codable, Identifiable {
    let id: Int?
    let chapterNumber: Int
    let title: String
    let subtitle: String
    let verseCount: Int
    var verses: [ChapterVerse]

    private enum CodingKeys: String, CodingKey {
        case id
        case chapterNumber = "chapter_number"
        case title
        case subtitle
        case verseCount = "verse_count"
        case verses
    }
}

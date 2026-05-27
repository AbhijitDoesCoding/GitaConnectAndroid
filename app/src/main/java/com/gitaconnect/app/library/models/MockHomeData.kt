package com.gitaconnect.app.library.models

data class MockDailyMantra(
    val id: Int,
    val name: String,
    val chants: String,
    val isPlaying: Boolean = false
)

enum class MockChallengeState {
    AVAILABLE, COMPLETED, CLAIMED
}

data class MockChallengeProgress(
    val id: String,
    val title: String,
    val state: MockChallengeState
)

object MockHomeData {
    val mockMantras = listOf(
        MockDailyMantra(1, "Hare Krishna Maha Mantra", "108 Chants", false),
        MockDailyMantra(2, "Gayatri Mantra", "108 Chants", false),
        MockDailyMantra(3, "Mahamrityunjaya Mantra", "54 Chants", false)
    )

    val mockChallenges = listOf(
        MockChallengeProgress("daily_verse", "Verse", MockChallengeState.CLAIMED),
        MockChallengeProgress("daily_jaap", "Mala", MockChallengeState.COMPLETED),
        MockChallengeProgress("daily_mood", "Mood", MockChallengeState.CLAIMED),
        MockChallengeProgress("daily_mantra", "Mantra", MockChallengeState.AVAILABLE),
        MockChallengeProgress("daily_feed", "Feed", MockChallengeState.AVAILABLE)
    )

    val currentLevel = "Seeker"
    val nextLevel = "Devotee"
    val xpProgress = 0.65f // 65% progress to next level
}

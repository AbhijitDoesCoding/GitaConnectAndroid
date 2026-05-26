package com.gitaconnect.app.library.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gitaconnect.app.library.models.Chapter
import com.gitaconnect.app.library.models.Verse
import com.gitaconnect.app.library.repository.GitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// UI State sealed class
// ---------------------------------------------------------------------------

sealed class GitaUiState<out T> {
    object Loading : GitaUiState<Nothing>()
    data class Success<T>(val data: T) : GitaUiState<T>()
    data class Error(val message: String) : GitaUiState<Nothing>()
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

/**
 * ViewModel for the Bhagavad Gita Library feature.
 * Uses [AndroidViewModel] to hold the Application context for the repository.
 */
class GitaLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GitaRepository(application.applicationContext)

    // ---- Chapters State ----
    private val _chaptersState = MutableStateFlow<GitaUiState<List<Chapter>>>(GitaUiState.Loading)
    val chaptersState: StateFlow<GitaUiState<List<Chapter>>> = _chaptersState.asStateFlow()

    // ---- Verses State ----
    private val _versesState = MutableStateFlow<GitaUiState<List<Verse>>>(GitaUiState.Loading)
    val versesState: StateFlow<GitaUiState<List<Verse>>> = _versesState.asStateFlow()

    init {
        loadChapters()
    }

    fun loadChapters() {
        viewModelScope.launch {
            _chaptersState.value = GitaUiState.Loading
            try {
                val chapters = repository.getAllChapters()
                _chaptersState.value = GitaUiState.Success(chapters)
            } catch (e: Exception) {
                _chaptersState.value = GitaUiState.Error(e.message ?: "Failed to load chapters")
            }
        }
    }

    fun loadVerses(chapterNumber: Int) {
        viewModelScope.launch {
            _versesState.value = GitaUiState.Loading
            try {
                val verses = repository.getVersesForChapter(chapterNumber)
                _versesState.value = GitaUiState.Success(verses)
            } catch (e: Exception) {
                _versesState.value = GitaUiState.Error(e.message ?: "Failed to load verses")
            }
        }
    }
}

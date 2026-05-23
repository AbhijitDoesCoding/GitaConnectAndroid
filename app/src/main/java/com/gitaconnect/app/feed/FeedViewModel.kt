@file:Suppress("SpellCheckingInspection")
package com.gitaconnect.app.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitaconnect.app.supabasecentral.SupabaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    private val _feedItems = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems: StateFlow<List<FeedItem>> = _feedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchFeed()
    }

    private fun fetchFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = SupabaseManager.fetchFeedItems()
                _feedItems.value = items.shuffled()
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error fetching feed from Supabase", e)
                // Note: Make sure your 'feed_items' table exists in Supabase,
                // and has RLS policies that allow reading (anon select).
            } finally {
                _isLoading.value = false
            }
        }
    }
}

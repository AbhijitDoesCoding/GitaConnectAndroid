package com.gitaconnect.app.supabasecentral

import com.gitaconnect.app.feed.FeedItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

/**
 * Singleton manager for the Supabase client.
 */
object SupabaseManager {

    // Your actual Supabase project URL and Anon Key retrieved from the MCP
    private const val SUPABASE_URL = "https://ltacraukglfzilebevyg.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx0YWNyYXVrZ2xmemlsZWJldnlnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc2MDc0MzEsImV4cCI6MjA4MzE4MzQzMX0.KtP0BiT5oqMr2K5rXpL4S_bBC3l1WVoleb0_GQXpBgA"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    /**
     * Fetches the video feed items from the 'feed_items' table.
     */
    suspend fun fetchFeedItems(): List<FeedItem> {
        return client.postgrest.from("feed_items").select().decodeList<FeedItem>()
    }

    /**
     * Placeholder for future Edge Function integration via POST request.
     */
    suspend fun callSarvamAI() {
        // TODO: Implement Edge Function call using `client.functions` or Ktor
    }
}

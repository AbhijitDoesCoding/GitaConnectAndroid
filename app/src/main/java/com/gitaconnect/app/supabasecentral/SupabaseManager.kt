@file:Suppress("SpellCheckingInspection")
package com.gitaconnect.app.supabasecentral


import com.gitaconnect.app.feed.FeedItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.gitaconnect.app.mentor.SarvamEdgeRequest
import com.gitaconnect.app.mentor.SarvamChatResponse

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

    // Define Ktor HttpClient for direct REST API calls
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Calls the Sarvam AI edge function to get a response.
     */
    suspend fun callSarvamAI(
        messages: List<Map<String, String>>,
        temperature: Double = 0.3,
        language: String = "en-IN",
        model: String = "sarvam-105b"
    ): SarvamChatResponse {
        val requestBody = SarvamEdgeRequest(
            messages = messages,
            temperature = temperature,
            language = language,
            model = model
        )

        // The edge function URL
        val url = "${SUPABASE_URL}/functions/v1/sarvam-ai"

        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${SUPABASE_KEY}")
            header("apikey", SUPABASE_KEY)
            setBody(requestBody)
        }.body()
    }
}

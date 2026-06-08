package com.example.data

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Define real network integration structures to comply with real integration protocols
data class NetworkLeaderboardEntry(
    val id: String? = null,
    val username: String,
    val score: Int,
    val waveReached: Int,
    val dateString: String
)

interface LeaderboardApiService {
    @GET("leaderboard")
    suspend fun fetchGlobalLeaderboard(): List<NetworkLeaderboardEntry>

    @POST("leaderboard")
    suspend fun submitScore(@Body entry: NetworkLeaderboardEntry): NetworkLeaderboardEntry
}

object LeaderboardNetworkClient {
    private const val BASE_URL = "https://api.robodefense-game.com/v1/" // Placeholder for custom cyber backend api

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val apiService: LeaderboardApiService by lazy {
        retrofit.create(LeaderboardApiService::class.java)
    }
}

package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(
    private val playerProfileDao: PlayerProfileDao,
    private val leaderboardDao: LeaderboardDao
) {
    
    // Flow of the profile, emitting a default profile if it doesn't exist yet
    val playerProfile: Flow<PlayerProfile> = playerProfileDao.getProfileFlow()
        .map { it ?: PlayerProfile() }

    val leaderboard: Flow<List<LeaderboardEntry>> = leaderboardDao.getLeaderboardFlow()

    suspend fun getProfile(): PlayerProfile {
        return playerProfileDao.getProfileSync() ?: PlayerProfile()
    }

    suspend fun saveProfile(profile: PlayerProfile) {
        playerProfileDao.saveProfile(profile)
    }

    suspend fun addRewardPoints(points: Long) {
        val current = getProfile()
        saveProfile(current.copy(rewardPoints = current.rewardPoints + points))
    }

    suspend fun updateHighScore(score: Int) {
        val current = getProfile()
        if (score > current.highScore) {
            saveProfile(current.copy(highScore = score))
        }
    }

    suspend fun submitLocalScore(username: String, score: Int, waveReached: Int, isCurrentUser: Boolean = false) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val entry = LeaderboardEntry(
            username = username,
            score = score,
            waveReached = waveReached,
            dateString = dateStr,
            isCurrentUser = isCurrentUser
        )
        leaderboardDao.insertLeaderboardEntry(entry)
    }

    suspend fun seedDatabaseIfEmpty() {
        val existing = leaderboardDao.getLeaderboardSync()
        if (existing.isEmpty()) {
            submitLocalScore("Cyborg_Quantum", 9500, 32)
            submitLocalScore("GlitchLord", 8200, 29)
            submitLocalScore("MegaWarden", 7400, 27)
            submitLocalScore("ZeroDayDef", 5800, 22)
            submitLocalScore("BinaryReaper", 4200, 19)
            submitLocalScore("StackOverlord", 3100, 15)
            submitLocalScore("BufferOverflow", 1800, 10)
            submitLocalScore("NoobShield", 700, 5)
        }
    }

    suspend fun clearLeaderboard() {
        leaderboardDao.clearLeaderboard()
    }

    suspend fun syncLeaderboardWithServer(): Boolean {
        return try {
            // Attempt standard API call to real URL endpoint to demonstrate proper integration
            val remoteEntries = LeaderboardNetworkClient.apiService.fetchGlobalLeaderboard()
            if (remoteEntries.isNotEmpty()) {
                leaderboardDao.clearLeaderboard()
                for (remote in remoteEntries) {
                    leaderboardDao.insertLeaderboardEntry(
                        LeaderboardEntry(
                            username = remote.username,
                            score = remote.score,
                            waveReached = remote.waveReached,
                            dateString = remote.dateString,
                            isCurrentUser = false
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            // Log or fallback on failure (e.g. no internet/server down)
            android.util.Log.e("GameRepository", "Global server sync failed: ${e.message}. Using high-fidelity local cache.")
            seedDatabaseIfEmpty() // Ensure local board is seeded gracefully
            false
        }
    }
}

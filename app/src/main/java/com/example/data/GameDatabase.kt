package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val rewardPoints: Long = 0,
    val highScore: Int = 0,
    val bulletDamageLevel: Int = 0,    // max 10
    val explosiveBlastLevel: Int = 0,  // max 10
    val flakFireRateLevel: Int = 0,    // max 10
    val startingGoldLevel: Int = 0,    // max 10
    val fortressHealthLevel: Int = 0,   // max 10
    val googleEmail: String? = null,
    val googleDisplayName: String? = null,
    val gameTagName: String? = null
) {
    // Helper calculation properties for actual game stats
    fun getBulletDamageMultiplier(): Float = 1.0f + (bulletDamageLevel * 0.15f)
    fun getExplosiveBlastMultiplier(): Float = 1.0f + (explosiveBlastLevel * 0.15f)
    fun getFlakFireRateMultiplier(): Float = 1.0f + (flakFireRateLevel * 0.15f)
    fun getStartingGoldMultiplier(): Float = 1.0f + (startingGoldLevel * 0.20f)
    fun getFortressHealthBonus(): Int = fortressHealthLevel * 5
}

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val score: Int,
    val waveReached: Int,
    val dateString: String,
    val isCurrentUser: Boolean = false
)

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getProfileFlow(): Flow<PlayerProfile?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileSync(): PlayerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: PlayerProfile)
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_entries ORDER BY score DESC, waveReached DESC LIMIT 50")
    fun getLeaderboardFlow(): Flow<List<LeaderboardEntry>>

    @Query("SELECT * FROM leaderboard_entries ORDER BY score DESC, waveReached DESC")
    suspend fun getLeaderboardSync(): List<LeaderboardEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntry)

    @Query("DELETE FROM leaderboard_entries")
    suspend fun clearLeaderboard()
}

@Database(entities = [PlayerProfile::class, LeaderboardEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "robo_defense_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

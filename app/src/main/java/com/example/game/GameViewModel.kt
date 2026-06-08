package com.example.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.PlayerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class GameState {
    MainMenu,
    Upgrades,
    Playing,
    Help,
    GameOver,
    Leaderboard
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val playerProfile: StateFlow<PlayerProfile>

    var gameState by mutableStateOf(GameState.MainMenu)
        private set

    var selectedMapSector by mutableStateOf(MapSector.Classic)


    // Real-time Gameplay State
    private val _towers = MutableStateFlow<List<Tower>>(emptyList())
    val towers = _towers.asStateFlow()

    private val _enemies = MutableStateFlow<List<Enemy>>(emptyList())
    val enemies = _enemies.asStateFlow()

    private val _projectiles = MutableStateFlow<List<Projectile>>(emptyList())
    val projectiles = _projectiles.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles = _particles.asStateFlow()

    private val _explosions = MutableStateFlow<List<Explosion>>(emptyList())
    val explosions = _explosions.asStateFlow()

    // Interface metrics
    var lives by mutableStateOf(20)
        private set
    var gold by mutableStateOf(400)
        private set
    var score by mutableStateOf(0)
        private set
    var currentWave by mutableStateOf(0)
        private set
    var isWaveActive by mutableStateOf(false)
        private set
    var autoStartSecondsLeft by mutableStateOf<Int?>(null)
        private set
    private var autoStartTicksCount = 0
    var gameSpeed by mutableStateOf(1) // 1x, 2x, 4x speed multipliers
        private set

    // Grid details
    val gridWidth = 9
    val gridHeight = 12
    val spawnPoint = GridPos(4, 0)
    val exitPoint = GridPos(4, 11)

    // Maze details
    var currentShortestPath by mutableStateOf<List<GridPos>>(emptyList())
        private set

    // Temporary warning feedback
    var blockingWarningMessage by mutableStateOf<String?>(null)
        private set

    // Selection
    var selectedCell by mutableStateOf<GridPos?>(null)
    var selectedTower by mutableStateOf<Tower?>(null)

    // Generator IDs
    private var nextEntityId = 1L

    // Spawning Queue State
    private var spawnQueue = mutableListOf<EnemyType>()
    private var ticksSinceLastSpawn = 0
    private val spawnIntervalTicks = 45 // spawn about every 1.5s (at 30fps)

    // Active Loop Job
    private var gameLoopJob: Job? = null

    val leaderboard: StateFlow<List<com.example.data.LeaderboardEntry>>
    var isSyncingLeaderboard by mutableStateOf(false)
        private set

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.playerProfileDao(), database.leaderboardDao())
        playerProfile = repository.playerProfile.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerProfile()
        )
        leaderboard = repository.leaderboard.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDatabaseIfEmpty()
        }

        // Calculate initial maze path on empty board
        recomputeGlobalPath()
    }

    fun signInWithGoogle(email: String, displayName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getProfile()
            // generate clean username/gamer-tag
            val preferredTag = current.gameTagName ?: (email.substringBefore("@") + "_Surge")
            val updated = current.copy(
                googleEmail = email,
                googleDisplayName = displayName,
                gameTagName = preferredTag
            )
            repository.saveProfile(updated)
            repository.seedDatabaseIfEmpty() // Ensure it's populated
            if (current.highScore > 0) {
                repository.submitLocalScore(preferredTag, current.highScore, currentWave.coerceAtLeast(1), isCurrentUser = true)
            }
        }
    }

    fun updateGameTagName(newTag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getProfile()
            val updated = current.copy(gameTagName = newTag)
            repository.saveProfile(updated)
            if (current.highScore > 0) {
                repository.submitLocalScore(newTag, current.highScore, currentWave.coerceAtLeast(1), isCurrentUser = true)
            }
        }
    }

    fun signOutOfGoogle() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getProfile()
            val updated = current.copy(
                googleEmail = null,
                googleDisplayName = null,
                gameTagName = null
            )
            repository.saveProfile(updated)
        }
    }

    fun syncLeaderboardFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            isSyncingLeaderboard = true
            repository.syncLeaderboardWithServer()
            isSyncingLeaderboard = false
        }
    }

    fun setScreen(target: GameState) {
        if (target == GameState.Playing && gameState != GameState.Playing) {
            startGame()
        } else {
            stopGameLoop()
        }
        gameState = target
        selectedCell = null
        selectedTower = null
    }

    private fun startGame() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfile()
            launch(Dispatchers.Main) {
                lives = 20 + profile.getFortressHealthBonus()
                gold = (400 * profile.getStartingGoldMultiplier()).toInt()
                score = 0
                currentWave = 0
                isWaveActive = false
                gameSpeed = 1
                autoStartSecondsLeft = null
                autoStartTicksCount = 0
                _towers.value = emptyList()
                _enemies.value = emptyList()
                _projectiles.value = emptyList()
                _particles.value = emptyList()
                _explosions.value = emptyList()
                selectedCell = null
                selectedTower = null
                recomputeGlobalPath()
                startGameLoop()
            }
        }
    }

    // Permanent upgrades (Research Shop)
    fun buyUpgrade(upgradeType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfile()
            var errorMsg: String? = null
            var updatedProfile = profile

            val cost = when (upgradeType) {
                "bullet" -> (profile.bulletDamageLevel + 1) * 200
                "explosive" -> (profile.explosiveBlastLevel + 1) * 250
                "flak" -> (profile.flakFireRateLevel + 1) * 220
                "gold" -> (profile.startingGoldLevel + 1) * 300
                "health" -> (profile.fortressHealthLevel + 1) * 150
                else -> 0
            }

            if (cost > 0 && profile.rewardPoints >= cost) {
                updatedProfile = when (upgradeType) {
                    "bullet" -> if (profile.bulletDamageLevel < 10) profile.copy(
                        bulletDamageLevel = profile.bulletDamageLevel + 1,
                        rewardPoints = profile.rewardPoints - cost
                    ) else profile
                    "explosive" -> if (profile.explosiveBlastLevel < 10) profile.copy(
                        explosiveBlastLevel = profile.explosiveBlastLevel + 1,
                        rewardPoints = profile.rewardPoints - cost
                    ) else profile
                    "flak" -> if (profile.flakFireRateLevel < 10) profile.copy(
                        flakFireRateLevel = profile.flakFireRateLevel + 1,
                        rewardPoints = profile.rewardPoints - cost
                    ) else profile
                    "gold" -> if (profile.startingGoldLevel < 10) profile.copy(
                        startingGoldLevel = profile.startingGoldLevel + 1,
                        rewardPoints = profile.rewardPoints - cost
                    ) else profile
                    "health" -> if (profile.fortressHealthLevel < 10) profile.copy(
                        fortressHealthLevel = profile.fortressHealthLevel + 1,
                        rewardPoints = profile.rewardPoints - cost
                    ) else profile
                    else -> profile
                }
                repository.saveProfile(updatedProfile)
            }
        }
    }

    fun startNextWave() {
        if (isWaveActive) return
        currentWave++
        isWaveActive = true
        selectedCell = null
        selectedTower = null
        autoStartSecondsLeft = null
        autoStartTicksCount = 0

        // Wave formula
        spawnQueue.clear()
        ticksSinceLastSpawn = 0

        val waveMultiplier = 1.0f + (currentWave - 1) * 0.15f
        val counts = when {
            currentWave in 1..19 -> {
                // Wave 1: 10, Wave 2: 15, ..., Wave 19: 100 scouts (incrementing by 5)
                val scoutsCount = 10 + (currentWave - 1) * 5
                listOf(EnemyType.Scout to scoutsCount)
            }
            else -> {
                // Wave 20+: Introducing Soldiers, Jets, and eventually Goliath Tanks, with dynamically scaled counts of all types
                val offset = currentWave - 20
                val scouts = (100 - offset * 5).coerceAtLeast(30)
                val soldiers = (15 + offset * 4).coerceAtMost(60)
                val jets = (8 + offset * 3).coerceAtMost(40)
                val tanks = if (currentWave >= 22) ((currentWave - 21) * 2).coerceAtMost(25) else 0
                
                val list = mutableListOf<Pair<EnemyType, Int>>()
                if (scouts > 0) list.add(EnemyType.Scout to scouts)
                if (soldiers > 0) list.add(EnemyType.Soldier to soldiers)
                if (jets > 0) list.add(EnemyType.Jet to jets)
                if (tanks > 0) list.add(EnemyType.Tank to tanks)
                list
            }
        }

        for ((type, count) in counts) {
            repeat(count) {
                spawnQueue.add(type)
            }
        }
        // Shuffle spawn types slightly for standard randomized spawns
        spawnQueue.shuffle()
    }

    fun toggleSpeed() {
        gameSpeed = when (gameSpeed) {
            1 -> 2
            2 -> 4
            else -> 1
        }
    }

    // Grid placement and mechanics
    fun selectCell(x: Int, y: Int) {
        val clickedPos = GridPos(x, y)
        val existing = _towers.value.find { it.x == x && it.y == y }
        selectedTower = existing
        selectedCell = clickedPos
    }

    fun buildTower(type: TowerType) {
        val cell = selectedCell ?: return
        if (gold < type.baseCost) {
            showWarning("Need $${type.baseCost} for this tower!")
            return
        }

        if (cell == spawnPoint || cell == exitPoint) {
            showWarning("Cannot build on Spawn or Exit!")
            return
        }

        // Check if occupied
        if (_towers.value.any { it.x == cell.x && it.y == cell.y }) {
            showWarning("Cell already occupied!")
            return
        }

        // Test if path exists if we block this tile
        val testOccupied = _towers.value.map { GridPos(it.x, it.y) }.toMutableSet()
        testOccupied.add(cell)

        val path = findPath(gridWidth, gridHeight, spawnPoint, exitPoint, testOccupied)
        if (path == null) {
            showWarning("Blocking exit! Path must remain open!")
            return
        }

        // Create new tower with stats adjusted by permanent profile stats
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfile()
            launch(Dispatchers.Main) {
                gold -= type.baseCost

                // Base parameters
                val isGun = type == TowerType.Gun
                val isRocket = type == TowerType.Rocket
                val isFlak = type == TowerType.AntiAir

                val damageMult = if (isGun) profile.getBulletDamageMultiplier() else 1.0f
                val cdMult = if (isFlak) profile.getFlakFireRateMultiplier() else 1.0f

                val newTower = Tower(
                    id = nextEntityId++,
                    x = cell.x,
                    y = cell.y,
                    type = type,
                    range = type.baseRange,
                    damage = type.baseDamage * damageMult,
                    cooldown = type.baseCooldown / cdMult
                )

                _towers.value = _towers.value + newTower
                selectedTower = newTower

                // Trigger path and routing recalculation
                recomputeGlobalPath()
                rerouteEnemies()
            }
        }
    }

    fun upgradeTower(tower: Tower) {
        val upgradeCost = (tower.type.baseCost * 0.8 * tower.tier).toInt()
        if (gold < upgradeCost) {
            showWarning("Requires $${upgradeCost} for upgrade!")
            return
        }

        if (tower.tier >= 3) {
            showWarning("Max tier reached!")
            return
        }

        gold -= upgradeCost
        val updatedTowers = _towers.value.map { t ->
            if (t.id == tower.id) {
                val nextTier = t.tier + 1
                val tag = when (t.type) {
                    TowerType.Gun -> if (nextTier == 2) "Machine Gun" else "Plasma Gatling"
                    TowerType.Rocket -> if (nextTier == 2) "Guided Missile" else "Devastator MIRV"
                    TowerType.AntiAir -> if (nextTier == 2) "Heavy Flak Storm" else "SAM Decimator"
                }
                t.copy(
                    tier = nextTier,
                    upgradeName = tag,
                    range = t.range * 1.15f,
                    damage = t.damage * 1.5f,
                    cooldown = t.cooldown * 0.85f // speed up reload
                ).also { selectedTower = it }
            } else {
                t
            }
        }
        _towers.value = updatedTowers
    }

    fun sellTower(tower: Tower) {
        val refund = (tower.type.baseCost * 0.7 * tower.tier).toInt()
        gold += refund
        _towers.value = _towers.value.filter { it.id != tower.id }
        selectedTower = null
        selectedCell = null

        recomputeGlobalPath()
        rerouteEnemies()
    }

    private fun showWarning(msg: String) {
        blockingWarningMessage = msg
        viewModelScope.launch {
            delay(2500)
            if (blockingWarningMessage == msg) {
                blockingWarningMessage = null
            }
        }
    }

    // Dynamic routing during rebuilds
    private fun recomputeGlobalPath() {
        val occupied = _towers.value.map { GridPos(it.x, it.y) }.toSet()
        val path = findPath(gridWidth, gridHeight, spawnPoint, exitPoint, occupied)
        if (path != null) {
            currentShortestPath = path
        }
    }

    private fun rerouteEnemies() {
        val occupied = _towers.value.map { GridPos(it.x, it.y) }.toSet()
        val currentList = _enemies.value
        for (enemy in currentList) {
            if (enemy.isAir) continue // Air enemies bypass all mazes!

            // Current coordinate rounded grid tile
            val cx = enemy.x.toInt().coerceIn(0, gridWidth - 1)
            val cy = enemy.y.toInt().coerceIn(0, gridHeight - 1)

            val nextPath = findPath(gridWidth, gridHeight, GridPos(cx, cy), exitPoint, occupied)
            if (nextPath != null) {
                enemy.path = nextPath
                enemy.pathIndex = 0
            }
        }
    }

    // Dynamic Loop Methods
    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                // Tick based on Speed setting
                repeat(gameSpeed) {
                    tickGame()
                }
                delay(30) // ~33FPS continuous tick
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun tickGame() {
        if (gameState != GameState.Playing) return

        // Auto start timer ticking
        if (!isWaveActive && autoStartSecondsLeft != null) {
            autoStartTicksCount++
            if (autoStartTicksCount >= 33) {
                autoStartTicksCount = 0
                val nextLeft = autoStartSecondsLeft!! - 1
                if (nextLeft <= 0) {
                    autoStartSecondsLeft = null
                    startNextWave()
                } else {
                    autoStartSecondsLeft = nextLeft
                }
            }
        }

        // 1. Spawning Mechanics
        if (isWaveActive) {
            if (spawnQueue.isNotEmpty()) {
                ticksSinceLastSpawn++
                if (ticksSinceLastSpawn >= spawnIntervalTicks) {
                    ticksSinceLastSpawn = 0
                    val nextType = spawnQueue.removeAt(0)
                    spawnEnemy(nextType)
                }
            } else if (_enemies.value.isEmpty()) {
                // Wave fully cleared! Reward player with Wave Points!
                isWaveActive = false
                val completionReward = 10 * currentWave
                gold += waveGoldBonus(currentWave)
                score += 100 * currentWave

                viewModelScope.launch(Dispatchers.IO) {
                    repository.addRewardPoints(completionReward.toLong())
                    repository.updateHighScore(score)
                }

                autoStartSecondsLeft = 60
                autoStartTicksCount = 0
            }
        }

        // 2. Continuous Enemy Positions
        val enemyList = _enemies.value.toMutableList()
        val iterator = enemyList.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            if (enemy.isAir) {
                // Air flies direct: Spawn (0, 4) to Exit (11, 4) in custom straight line
                val dx = exitPoint.x - enemy.x
                val dy = exitPoint.y - enemy.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < enemy.speed) {
                    // Reached target exit base! Subtract lives
                    lives = (lives - 1).coerceAtLeast(0)
                    iterator.remove()
                    validateGameOver()
                } else {
                    val angle = atan2(dy, dx)
                    enemy.x += (enemy.speed * cos(angle))
                    enemy.y += (enemy.speed * sin(angle))
                }
            } else {
                // Ground follows their local route path
                if (enemy.pathIndex < enemy.path.size) {
                    val target = enemy.path[enemy.pathIndex]
                    val dx = target.x - enemy.x
                    val dy = target.y - enemy.y
                    val dist = sqrt(dx * dx + dy * dy)

                    if (dist < enemy.speed) {
                        // Move right to cell center to align routing turn
                        enemy.x = target.x.toFloat()
                        enemy.y = target.y.toFloat()
                        enemy.pathIndex++
                        if (enemy.pathIndex >= enemy.path.size) {
                            lives = (lives - 1).coerceAtLeast(0)
                            iterator.remove()
                            validateGameOver()
                        }
                    } else {
                        val angle = atan2(dy, dx)
                        enemy.x += (enemy.speed * cos(angle))
                        enemy.y += (enemy.speed * sin(angle))
                    }
                } else {
                    // Edge case path overflow
                    lives = (lives - 1).coerceAtLeast(0)
                    iterator.remove()
                    validateGameOver()
                }
            }
        }
        _enemies.value = enemyList

        // 3. Firing Cooldown timers & Shooting decisions
        val activeTowers = _towers.value
        val projectileList = _projectiles.value.toMutableList()

        for (tower in activeTowers) {
            if (tower.fireCooldown > 0) {
                tower.fireCooldown -= 0.03f // matching elapsed timing per tick
            }

            if (tower.fireCooldown <= 0) {
                // Match targets
                val candidates = _enemies.value.filter { enemy ->
                    val isAirOnlyTower = tower.type == TowerType.AntiAir
                    val isGroundOnlyTower = tower.type == TowerType.Gun

                    // Anti-air fits air-only, Gun ground-only, Rocket hits everything!
                    val typeMatch = when {
                        isAirOnlyTower -> enemy.isAir
                        isGroundOnlyTower -> !enemy.isAir
                        else -> true
                    }

                    if (typeMatch) {
                        val dx = enemy.x - tower.x
                        val dy = enemy.y - tower.y
                        val dist = sqrt(dx * dx + dy * dy)
                        dist <= tower.range
                    } else false
                }

                if (candidates.isNotEmpty()) {
                    // Pick closest to exit (i.e. path index or progress)
                    val target = candidates.maxByOrNull { it.pathIndex }!!
                    tower.fireCooldown = tower.cooldown

                    // Spawn matching projectile
                    val pType = when (tower.type) {
                        TowerType.Gun -> ProjectileType.Bullet
                        TowerType.Rocket -> ProjectileType.Rocket
                        TowerType.AntiAir -> ProjectileType.Flak
                    }

                    val splash = if (tower.type == TowerType.Rocket) 1.5f else 0f
                    projectileList.add(
                        Projectile(
                            id = nextEntityId++,
                            type = pType,
                            x = tower.x.toFloat(),
                            y = tower.y.toFloat(),
                            startX = tower.x.toFloat(),
                            startY = tower.y.toFloat(),
                            targetEnemyId = target.id,
                            progressSpeed = 0.15f,
                            damage = tower.damage,
                            splashRadius = splash
                        )
                    )
                }
            }
        }

        // 4. Moving Projectiles
        val remainingProjectiles = mutableListOf<Projectile>()
        val explosionsList = _explosions.value.toMutableList()

        for (proj in projectileList) {
            val target = _enemies.value.find { it.id == proj.targetEnemyId }
            if (target == null) {
                // Target disappeared / defeated. Let rocket trigger explosion at current position, bullet disappears
                if (proj.type == ProjectileType.Rocket) {
                    triggerExplosion(proj.x, proj.y, proj.damage, proj.splashRadius, explosionsList)
                }
                continue // remove projectile
            }

            proj.currentProgress += proj.progressSpeed
            if (proj.currentProgress >= 1.0f) {
                // Target hit!
                if (proj.type == ProjectileType.Rocket) {
                    triggerExplosion(target.x, target.y, proj.damage, proj.splashRadius, explosionsList)
                } else {
                    // Standard hit damage
                    applyDamageToEnemy(target.id, proj.damage)
                    spawnSparkHit(target.x, target.y)
                }
            } else {
                // Line interpolation progress
                proj.x = proj.startX + (target.x - proj.startX) * proj.currentProgress
                proj.y = proj.startY + (target.y - proj.startY) * proj.currentProgress
                remainingProjectiles.add(proj)
            }
        }
        _projectiles.value = remainingProjectiles

        // 5. Explode updates
        val remainingExplosions = mutableListOf<Explosion>()
        for (exp in explosionsList) {
            exp.radius += 0.15f
            exp.alpha -= 0.08f
            if (exp.alpha > 0) {
                remainingExplosions.add(exp)
            }
        }
        _explosions.value = remainingExplosions

        // 6. Particles decay
        val activeParticles = _particles.value.toMutableList()
        val particleIterator = activeParticles.iterator()
        while (particleIterator.hasNext()) {
            val part = particleIterator.next()
            part.age++
            if (part.age >= part.maxLifetime) {
                particleIterator.remove()
            } else {
                // Custom physics displacement
                // Modify position inside object
                val px = part.x + part.vx
                val py = part.y + part.vy
                // Workaround properties update
                activeParticles[activeParticles.indexOf(part)] = part.copy(
                    x = px,
                    y = py,
                    vx = part.vx * 0.95f,
                    vy = part.vy * 0.95f
                )
            }
        }
        _particles.value = activeParticles
    }

    private fun spawnEnemy(type: EnemyType) {
        val healthScale = Math.pow(1.15, (currentWave - 1).coerceAtMost(3).toDouble()).toFloat()
        val hp = type.baseHealth * healthScale

        val newEnemy = Enemy(
            id = nextEntityId++,
            type = type,
            x = spawnPoint.x.toFloat(),
            y = spawnPoint.y.toFloat(),
            health = hp,
            maxHealth = hp,
            path = currentShortestPath,
            pathIndex = 0,
            speed = type.baseSpeed,
            goldReward = type.goldReward,
            rpReward = type.rpReward
        )
        _enemies.value = _enemies.value + newEnemy
    }

    private fun triggerExplosion(
        ex: Float,
        ey: Float,
        damage: Float,
        radius: Float,
        explosionsList: MutableList<Explosion>
    ) {
        explosionsList.add(Explosion(nextEntityId++, ex, ey, 0.4f, radius))

        // Apply Splash to all robots nearby
        val activeList = _enemies.value
        val profile = playerProfile.value
        val finalRadius = radius * profile.getExplosiveBlastMultiplier()

        for (enemy in activeList) {
            val dx = enemy.x - ex
            val dy = enemy.y - ey
            val dist = sqrt(dx * dx + dy * dy)
            if (dist <= finalRadius) {
                // Scale damage slightly based on proximity (clamped 40% up to 100%)
                val falloff = (1.0f - (dist / finalRadius)).coerceIn(0.4f, 1.0f)
                applyDamageToEnemy(enemy.id, damage * falloff)
            }
        }

        // Spawn bright sparkling particle cloud
        repeat(12) {
            val angle = Math.random() * Math.PI * 2
            val speed = Math.random() * 0.15 + 0.05
            _particles.value = _particles.value + Particle(
                id = nextEntityId++,
                x = ex,
                y = ey,
                vx = (speed * cos(angle)).toFloat(),
                vy = (speed * sin(angle)).toFloat(),
                color = 0, // Orange fire
                maxLifetime = 15 + (Math.random() * 15).toInt()
            )
        }
    }

    private fun applyDamageToEnemy(enemyId: Long, amt: Float) {
        val list = _enemies.value.map { enemy ->
            if (enemy.id == enemyId) {
                val nextHp = (enemy.health - amt).coerceAtLeast(0f)
                enemy.copy(health = nextHp).also {
                    if (nextHp <= 0f) {
                        // Reward payouts immediately
                        gold += enemy.goldReward
                        score += (enemy.type.baseHealth * 0.5f).toInt()
                        viewModelScope.launch(Dispatchers.IO) {
                            repository.addRewardPoints(enemy.rpReward.toLong())
                        }
                    }
                }
            } else {
                enemy
            }
        }.filter { it.health > 0f }
        _enemies.value = list
    }

    private fun spawnSparkHit(ex: Float, ey: Float) {
        repeat(4) {
            val angle = Math.random() * Math.PI * 2
            val speed = Math.random() * 0.1 + 0.03
            _particles.value = _particles.value + Particle(
                id = nextEntityId++,
                x = ex,
                y = ey,
                vx = (speed * cos(angle)).toFloat(),
                vy = (speed * sin(angle)).toFloat(),
                color = 1, // Cyan spark
                maxLifetime = 8 + (Math.random() * 8).toInt()
            )
        }
    }

    private fun waveGoldBonus(wave: Int) = 45 + wave * 5

    private fun validateGameOver() {
        if (lives <= 0 && gameState == GameState.Playing) {
            gameState = GameState.GameOver
            stopGameLoop()
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateHighScore(score)
                val profile = repository.getProfile()
                val tag = profile.gameTagName ?: "Def_Defender"
                repository.submitLocalScore(tag, score, currentWave, isCurrentUser = true)
            }
        }
    }

    // Short pathfinding BFS on a grid with obstacle set
    private fun findPath(
        width: Int,
        height: Int,
        start: GridPos,
        end: GridPos,
        occupied: Set<GridPos>
    ): List<GridPos>? {
        val queue = ArrayDeque<List<GridPos>>()
        queue.add(listOf(start))
        val visited = mutableSetOf(start)

        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val current = path.last()

            if (current == end) return path

            val neighbors = listOf(
                GridPos(current.x + 1, current.y),
                GridPos(current.x - 1, current.y),
                GridPos(current.x, current.y + 1),
                GridPos(current.x, current.y - 1)
            )

            for (next in neighbors) {
                if (next.x in 0 until width && next.y in 0 until height) {
                    if (next !in visited && (next == end || next !in occupied)) {
                        visited.add(next)
                        queue.add(path + next)
                    }
                }
            }
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}

package com.example.game

import androidx.compose.ui.graphics.Color

data class GridPos(val x: Int, val y: Int)

enum class TowerType(
    val label: String,
    val baseCost: Int,
    val description: String,
    val baseRange: Float,
    val baseDamage: Float,
    val baseCooldown: Float // in seconds
) {
    Gun("Gun Tower", 100, "Rapid single-target bullets.", 2.5f, 15f, 0.8f),
    Rocket("Rocket Tower", 150, "Heavy splash rockets. Targets ground & air.", 4.0f, 45f, 2.2f),
    AntiAir("SAM Flak", 120, "Ground-to-air anti-vehicle missiles.", 4.5f, 35f, 0.5f)
}

enum class EnemyType(
    val label: String,
    val baseSpeed: Float, // grid tiles per game step (~30fps)
    val baseHealth: Float,
    val goldReward: Int,
    val rpReward: Int,
    val isAir: Boolean
) {
    Scout("Scout Drone", 0.055f, 40f, 12, 1, false),
    Soldier("Robo Trooper", 0.03f, 85f, 20, 2, false),
    Tank("Iron Goliath", 0.012f, 300f, 50, 5, false),
    Jet("Phantom Flyer", 0.045f, 65f, 30, 3, true)
}

data class Enemy(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var health: Float,
    val maxHealth: Float,
    var path: List<GridPos>,
    var pathIndex: Int,
    var speed: Float,
    val goldReward: Int,
    val rpReward: Int
) {
    val isAir: Boolean get() = type.isAir
}

data class Tower(
    val id: Long,
    val x: Int,
    val y: Int,
    val type: TowerType,
    var tier: Int = 1,
    var upgradeName: String? = null,
    var fireCooldown: Float = 0f, // counting down in seconds or ticks
    var range: Float,
    var damage: Float,
    var cooldown: Float
)

enum class ProjectileType {
    Bullet,
    Rocket,
    Flak
}

data class Projectile(
    val id: Long,
    val type: ProjectileType,
    var x: Float,
    var y: Float,
    val startX: Float,
    val startY: Float,
    val targetEnemyId: Long,
    val progressSpeed: Float, // portion of distance animated per frame (e.g. 0.1 to 0.2)
    var currentProgress: Float = 0f, // 0.0 to 1.0
    val damage: Float,
    val splashRadius: Float = 0f
)

data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Int, // indices or hex colors
    var maxLifetime: Int,
    var age: Int = 0
)

data class Explosion(
    val id: Long,
    val x: Float,
    val y: Float,
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float = 1.0f
)

enum class MapSector(
    val idString: String,
    val label: String,
    val description: String,
    val bgColor: Color,
    val pathColor: Color,
    val gridDotColor: Color,
    val portalColor: Color
) {
    Classic(
        "classic",
        "Cyber Grid",
        "Immersive default high-tech cybernetic grid.",
        Color(0xFF08121A),
        Color(0xFF00E676),
        Color(0xFF00E5FF),
        Color(0xFFD500F9)
    ),
    Winter(
        "winter",
        "Frostbite Tundra",
        "Snow-covered grid with ice-cold neon pathways.",
        Color(0xFF0D2436),
        Color(0xFFE0F7FA),
        Color(0xFF80DEEA),
        Color(0xFF00E5FF)
    ),
    Desert(
        "desert",
        "Scorched Dunes",
        "Sandstorm-battered fortress on heated sands.",
        Color(0xFF2B1C10),
        Color(0xFFFFF3E0),
        Color(0xFFFFB74D),
        Color(0xFFFF9100)
    ),
    Forest(
        "forest",
        "Overgrown Core",
        "Lush dense wilderness reclaiming ancient hardware.",
        Color(0xFF081810),
        Color(0xFFE8F5E9),
        Color(0xFF81C784),
        Color(0xFF00E676)
    ),
    Lava(
        "lava",
        "Lava Crucible",
        "Active volcanic chambers surrounded by molten core paths.",
        Color(0xFF190505),
        Color(0xFFFFEBEE),
        Color(0xFFFF8A80),
        Color(0xFFF44336)
    ),
    Coastal(
        "coastal",
        "Neon Reef",
        "Bioluminescent ocean shelf with neon glowing coral pathways.",
        Color(0xFF001B2E),
        Color(0xFFE0FBFC),
        Color(0xFF00F5D4),
        Color(0xFF00B4D8)
    ),
    Garden(
        "garden",
        "Zen Sanctuary",
        "Harmonious botanical garden under glowing cherry blossoms.",
        Color(0xFF16231C),
        Color(0xFFF5F5F0),
        Color(0xFFFFB7C5),
        Color(0xFFFF4081)
    ),
    Highway(
        "highway",
        "Cyber Highway",
        "Fast-paced urban transit road with cybernetic light lanes.",
        Color(0xFF121214),
        Color(0xFF00E5FF),
        Color(0xFFFFEB3B),
        Color(0xFFFF9800)
    ),
    River(
        "river",
        "Digital Torrent",
        "Rushing river rapids of electric data flowing over steep rocks.",
        Color(0xFF000B1A),
        Color(0xFFCAF0F8),
        Color(0xFF0077B6),
        Color(0xFF90E0EF)
    ),
    Ruins(
        "ruins",
        "Ancient Tech",
        "Overgrown, broken monolithic stone blocks and relic cores.",
        Color(0xFF1F1F1A),
        Color(0xFFFFF9C4),
        Color(0xFFC5E1A5),
        Color(0xFF9CCC65)
    ),
    StoneBrickRoad(
        "stonebrickroad",
        "Stone Gridway",
        "Cobbled stone road with medieval neon-infused gold lanterns.",
        Color(0xFF1E1F22),
        Color(0xFFE2E2E2),
        Color(0xFFFFD54F),
        Color(0xFFD4AF37)
    ),
    Valley(
        "valley",
        "Echo Valley",
        "Mist-shrouded deep amethyst valleys and dark rocky cliffs.",
        Color(0xFF1A122E),
        Color(0xFFEDE7F6),
        Color(0xFFB39DDB),
        Color(0xFF7C4DFF)
    )
}


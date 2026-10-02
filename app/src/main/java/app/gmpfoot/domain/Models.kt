package app.gmpfoot.domain

enum class Position { GK, DEF, MID, ATT }

data class Player(
    val id: String,
    val name: String,
    val position: Position,
    val age: Int,
    val overall: Int,
    val marketValueEur: Long,
    val isYouth: Boolean = false,
)

data class Team(
    val id: String,
    val name: String,
    val country: String,
    val squad: List<Player>,
    val youthSquad: List<Player> = emptyList(),
)

data class Manager(
    val id: String,
    val name: String,
    val reputation: Int,      // 0..100
    val tactics: Int,         // 0..100
    val manManagement: Int,   // 0..100
    val youthDevelopment: Int // 0..100
)

enum class CareerMode { PROFESSIONAL, LIVE }

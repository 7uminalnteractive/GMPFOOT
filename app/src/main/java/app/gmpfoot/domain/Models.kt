package app.gmpfoot.domain

enum class Position { GK, DEF, MID, ATT }

data class Player(
    val id: String,
    val name: String,
    val position: Position,
    val age: Int,
    val technique: Int,
    val physical: Int,
    val intelligence: Int,
    val motivation: Int,
    val marketValueEur: Long = 0,
    val isYouth: Boolean = false,
) {
    /** Mesma ideia do Brasfoot base: média dos 4 atributos. */
    val overall: Int get() = (technique + physical + intelligence + motivation) / 4
}

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

package app.gmpfoot.domain

enum class TrophyKind { LEAGUE, CUP, CONTINENTAL, WORLD, SUPERCUP }

data class Trophy(
    val name: String,
    val season: String,
    val kind: TrophyKind,
)

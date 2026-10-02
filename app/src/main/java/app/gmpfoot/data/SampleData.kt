package app.gmpfoot.data

import app.gmpfoot.domain.*

/** Dados de EXEMPLO. Os dados reais virão de um TeamDataSource. */
object SampleData {
    val managers = listOf(
        Manager("m1", "Treinador A", 82, 85, 70, 55),
        Manager("m2", "Treinador B", 64, 70, 80, 90),
        Manager("m3", "Treinador C", 48, 60, 65, 75),
    )
    val clubs = listOf(
        ClubProfile("Clube Alfa", 90_000, 3, 40_000_000, wantsYouthFocus = false),
        ClubProfile("Clube Beta", 45_000, 2, 15_000_000, wantsYouthFocus = true),
    )
    val transferTarget = TransferTarget(
        playerName = "Jogador Exemplo",
        sellerName = "Clube Beta",
        marketValueEur = 12_000_000,
        sellerNeedsMoney = false,
        releaseClauseEur = 30_000_000,
    )
    val trophies = listOf(
        Trophy("Campeonato Nacional", "2026", TrophyKind.LEAGUE),
        Trophy("Copa Nacional", "2026", TrophyKind.CUP),
        Trophy("Campeonato Nacional", "2027", TrophyKind.LEAGUE),
        Trophy("Copa Continental", "2027", TrophyKind.CONTINENTAL),
        Trophy("Supercopa", "2028", TrophyKind.SUPERCUP),
        Trophy("Mundial de Clubes", "2028", TrophyKind.WORLD),
    )
}

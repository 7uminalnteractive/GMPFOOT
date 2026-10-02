package app.gmpfoot.data

import app.gmpfoot.domain.*

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
}

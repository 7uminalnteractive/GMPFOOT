package app.gmpfoot.domain

import kotlin.random.Random

enum class OfferKind(val label: String) {
    SHIRT("Patrocínio master"),
    KIT("Fornecedor de material"),
    NAMING("Naming rights do estádio"),
    SAF("Proposta de SAF"),
}

/**
 * Proposta de contrato. Para patrocínio/naming: perSeasonEur por temporada, seasons de duração,
 * bonusEur por vitória. Para SAF: investmentEur à vista em troca de stakePercent do clube.
 */
data class Offer(
    val id: String,
    val kind: OfferKind,
    val company: String,
    val perSeasonEur: Long = 0,
    val seasons: Int = 0,
    val bonusEur: Long = 0,
    val stakePercent: Int = 0,
    val investmentEur: Long = 0,
    val stadiumName: String? = null,
)

data class Stadium(val name: String, val capacity: Int, val ticketPrice: Int)

data class Finance(
    val balance: Long,
    val stadium: Stadium,
    val deals: List<Offer> = emptyList(),
    val saf: Offer? = null,
    val seasonStartBalance: Long = balance,
)

data class RoundFinance(
    val home: Boolean,
    val attendance: Int,
    val tickets: Long,
    val sponsors: Long,
    val wages: Long,
) {
    val net: Long get() = tickets + sponsors - wages
}

object Economy {
    const val EXPAND_COST = 8_000_000L
    const val EXPAND_SEATS = 5_000
    const val MAX_CAPACITY = 90_000

    private val brands = listOf(
        "Vértice Esportes", "Lumina Seguros", "Atlas Bank", "Brava Energia",
        "Costa Azul Telecom", "Orion Tech", "Aurora Foods", "Titã Motors",
    )
    private val investors = listOf("Atlas Capital", "Orion Partners", "Grupo Aurora", "Titã Investimentos")

    /** Salário por rodada. Usa o valor de mercado quando existe; senão, uma curva pelo overall. */
    fun wage(p: Player): Long =
        if (p.marketValueEur > 0) (p.marketValueEur * 0.005).toLong()
        else {
            val x = (p.overall - 40).coerceAtLeast(0).toLong()
            x * x * 60
        }

    fun wagesPerRound(t: Team): Long = t.squad.sumOf { wage(it) }

    fun startingFinance(t: Team): Finance {
        val r = t.rating()
        val k = (r - 50).coerceAtLeast(5).toLong()
        val cap = 8_000 + (r - 55).coerceAtLeast(5) * 1_500
        val price = if (r < 65) 20 else 50
        return Finance(balance = k * k * 40_000, stadium = Stadium("Arena ${t.name}", cap, price))
    }

    fun attendance(st: Stadium, rating: Int, rank: Int, n: Int): Int {
        val base = 0.55 + (rating - 75) / 50.0
        val pos = 0.15 - 0.25 * (rank - 1) / (n - 1).coerceAtLeast(1)
        val price = (st.ticketPrice - 50) / 150.0
        val d = (base + pos - price).coerceIn(0.15, 1.0)
        return (st.capacity * d).toInt()
    }

    fun dealsPerRound(f: Finance, rounds: Int): Long =
        f.deals.sumOf { it.perSeasonEur } / rounds.coerceAtLeast(1)

    fun winBonus(f: Finance): Long = f.deals.sumOf { it.bonusEur }

    fun generateOffers(team: Team, f: Finance, rnd: Random, tag: String): List<Offer> {
        val r = team.rating()
        val x = (r - 55).coerceAtLeast(5).toLong()
        val shirt = x * x * 30_000
        fun jitter(v: Long) = (v * (0.85 + rnd.nextDouble() * 0.3)).toLong()
        val b = brands.shuffled(rnd)
        val out = mutableListOf<Offer>()
        out += Offer("$tag-shirt-0", OfferKind.SHIRT, b[0], jitter(shirt), 3)
        out += Offer("$tag-shirt-1", OfferKind.SHIRT, b[1], jitter(shirt * 8 / 10), 1, bonusEur = jitter(shirt / 150))
        out += Offer("$tag-kit-0", OfferKind.KIT, b[2], jitter(shirt * 6 / 10), 3)
        out += Offer("$tag-naming-0", OfferKind.NAMING, b[3], jitter(shirt * 35 / 100), 4, stadiumName = "Arena ${b[3].substringBefore(' ')}")
        out += Offer("$tag-naming-1", OfferKind.NAMING, b[4], jitter(shirt * 55 / 100), 8, stadiumName = "${b[4].substringBefore(' ')} Stadium")
        if (f.saf == null) {
            out += Offer(
                "$tag-saf-0", OfferKind.SAF, investors.random(rnd),
                stakePercent = 25 + rnd.nextInt(25),
                investmentEur = jitter(x * x * 120_000),
            )
        }
        return out
    }

    fun accept(f: Finance, o: Offer): Finance = when (o.kind) {
        OfferKind.SAF -> f.copy(balance = f.balance + o.investmentEur, saf = o)
        OfferKind.NAMING -> f.copy(
            deals = f.deals.filter { it.kind != o.kind } + o,
            stadium = f.stadium.copy(name = o.stadiumName ?: f.stadium.name),
        )
        else -> f.copy(deals = f.deals.filter { it.kind != o.kind } + o)
    }

    /** Fecha a temporada: contratos perdem 1 ano, dividendo da SAF sai do lucro. Retorna (finanças, dividendo). */
    fun endSeason(f: Finance, clubName: String): Pair<Finance, Long> {
        val profit = f.balance - f.seasonStartBalance
        val dividend = if (f.saf != null && profit > 0) profit * f.saf.stakePercent / 100 else 0L
        val aged = f.deals.map { it.copy(seasons = it.seasons - 1) }
        val kept = aged.filter { it.seasons > 0 }
        val namingGone = aged.any { it.kind == OfferKind.NAMING && it.seasons <= 0 }
        val st = if (namingGone) f.stadium.copy(name = "Arena $clubName") else f.stadium
        val balance = f.balance - dividend
        return f.copy(balance = balance, deals = kept, stadium = st, seasonStartBalance = balance) to dividend
    }

    fun expand(f: Finance): Finance? {
        if (f.balance < EXPAND_COST || f.stadium.capacity >= MAX_CAPACITY) return null
        return f.copy(
            balance = f.balance - EXPAND_COST,
            stadium = f.stadium.copy(capacity = (f.stadium.capacity + EXPAND_SEATS).coerceAtMost(MAX_CAPACITY)),
        )
    }
}

fun Long.money(): String {
    val a = kotlin.math.abs(this)
    val s = when {
        a >= 1_000_000 -> "%.1f mi".format(a / 1_000_000.0)
        a >= 1_000 -> "%d mil".format(a / 1_000)
        else -> "$a"
    }
    return (if (this < 0) "-€" else "€") + s
}

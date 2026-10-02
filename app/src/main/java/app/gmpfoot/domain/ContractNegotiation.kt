package app.gmpfoot.domain

/** Cláusulas negociáveis do contrato de um manager. */
data class ContractOffer(
    val weeklySalaryEur: Int,
    val years: Int,
    val winBonusEur: Int,
    val releaseClauseEur: Int,
    val transferBudgetEur: Long,
    val youthPromise: Boolean,
)

/** Perfil do clube: o que ele valoriza e o limite do que aceita pagar. */
data class ClubProfile(
    val name: String,
    val maxWeeklySalaryEur: Int,
    val idealYears: Int,
    val baseTransferBudgetEur: Long,
    val wantsYouthFocus: Boolean,
)

sealed interface NegotiationResult {
    data object Accepted : NegotiationResult
    data class Counter(val offer: ContractOffer, val message: String) : NegotiationResult
    data class Rejected(val message: String) : NegotiationResult
}

/**
 * Negociação em rodadas: o clube pontua a proposta (0..100). Acima do limiar aceita,
 * perto dele devolve contraproposta, longe demais rompe a negociação.
 * A paciência do clube cai a cada rodada.
 */
class ContractNegotiation(
    private val manager: Manager,
    private val club: ClubProfile,
    private val maxRounds: Int = 4,
) {
    var round = 0
        private set

    fun score(o: ContractOffer): Int {
        var s = 60
        val salaryRatio = o.weeklySalaryEur.toDouble() / club.maxWeeklySalaryEur
        s -= ((salaryRatio - 1.0) * 80).toInt().coerceAtLeast(0)
        s += ((1.0 - salaryRatio) * 20).toInt().coerceIn(0, 20)
        s -= kotlin.math.abs(o.years - club.idealYears) * 4
        s -= (o.releaseClauseEur / 5_000_000)
        s -= (o.winBonusEur / 50_000)
        if (o.transferBudgetEur > club.baseTransferBudgetEur) {
            s -= ((o.transferBudgetEur - club.baseTransferBudgetEur) / 10_000_000).toInt()
        }
        if (o.youthPromise && club.wantsYouthFocus) s += 8
        s += (manager.reputation - 50) / 5
        s -= round * 5
        return s.coerceIn(0, 100)
    }

    fun propose(offer: ContractOffer): NegotiationResult {
        round++
        val s = score(offer)
        return when {
            s >= 65 -> NegotiationResult.Accepted
            round >= maxRounds -> NegotiationResult.Rejected("${club.name} encerrou as negociações.")
            s >= 40 -> NegotiationResult.Counter(
                offer.copy(
                    weeklySalaryEur = minOf(offer.weeklySalaryEur, club.maxWeeklySalaryEur),
                    years = club.idealYears,
                    releaseClauseEur = offer.releaseClauseEur / 2,
                    transferBudgetEur = minOf(offer.transferBudgetEur, club.baseTransferBudgetEur),
                ),
                "${club.name} gostou, mas devolveu uma contraproposta."
            )
            else -> NegotiationResult.Rejected("${club.name} achou a proposta distante demais.")
        }
    }
}

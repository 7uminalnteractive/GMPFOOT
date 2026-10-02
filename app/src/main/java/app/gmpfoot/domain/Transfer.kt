package app.gmpfoot.domain

/** Cláusulas de uma negociação de compra/venda de jogador. */
data class TransferOffer(
    val feeEur: Long,
    val installments: Int,          // parcelas (1 = à vista)
    val sellOnPercent: Int,         // % de futura venda que vai para o clube vendedor
    val performanceBonusEur: Long,  // pago por metas (jogos, gols)
    val buyBackEur: Long?,          // opção de recompra do vendedor
    val loanWithOption: Boolean,    // empréstimo com opção de compra
)

data class TransferTarget(
    val playerName: String,
    val sellerName: String,
    val marketValueEur: Long,
    val sellerNeedsMoney: Boolean,
    val releaseClauseEur: Long?,
)

sealed interface TransferResult {
    data class Accepted(val message: String) : TransferResult
    data class Counter(val offer: TransferOffer, val message: String) : TransferResult
    data class Rejected(val message: String) : TransferResult
}

/** O clube vendedor compara o valor efetivo da proposta com o preço pedido. */
class TransferNegotiation(
    private val target: TransferTarget,
    private val maxRounds: Int = 4,
) {
    var round = 0
        private set

    private fun asking(): Double {
        val base = target.marketValueEur * if (target.sellerNeedsMoney) 1.0 else 1.15
        return base * (1.0 + 0.02 * round)
    }

    fun effectiveValue(o: TransferOffer): Double {
        var v = o.feeEur.toDouble()
        v -= o.feeEur * 0.03 * (o.installments - 1)
        v += o.performanceBonusEur * 0.4
        v += target.marketValueEur * (o.sellOnPercent / 100.0) * 0.5
        if (o.buyBackEur != null) v += target.marketValueEur * 0.05
        if (o.loanWithOption) v *= 0.85
        return v
    }

    fun propose(o: TransferOffer): TransferResult {
        round++
        val clause = target.releaseClauseEur
        if (clause != null && o.feeEur >= clause && !o.loanWithOption) {
            return TransferResult.Accepted("Cláusula de rescisão paga. ${target.playerName} é seu.")
        }
        val ratio = effectiveValue(o) / asking()
        return when {
            ratio >= 1.0 -> TransferResult.Accepted("${target.sellerName} aceitou a proposta.")
            round >= maxRounds -> TransferResult.Rejected("${target.sellerName} encerrou a negociação.")
            ratio >= 0.8 -> TransferResult.Counter(
                o.copy(
                    feeEur = (asking() * 0.95).toLong(),
                    installments = minOf(o.installments, 2),
                ),
                "${target.sellerName} pede um valor maior e menos parcelas."
            )
            else -> TransferResult.Rejected("${target.sellerName} achou a oferta muito baixa.")
        }
    }
}

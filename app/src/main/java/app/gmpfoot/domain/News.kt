package app.gmpfoot.domain

enum class NewsKind(val label: String) {
    RESULT("Resultado"),
    PLAYER("Destaque"),
    OFFER("Proposta recebida"),
    TABLE("Classificação"),
    FINANCE("Finanças"),
}

data class NewsItem(val kind: NewsKind, val title: String, val body: String = "")

/** Últimos [n] resultados do time: 'V', 'E' ou 'D' (mais antigo primeiro). */
fun List<MatchResult>.formOf(teamId: String, n: Int = 5): List<Char> =
    filter { it.homeId == teamId || it.awayId == teamId }
        .takeLast(n)
        .map { m ->
            val mine = if (m.homeId == teamId) m.homeGoals else m.awayGoals
            val theirs = if (m.homeId == teamId) m.awayGoals else m.homeGoals
            when {
                mine > theirs -> 'V'
                mine < theirs -> 'D'
                else -> 'E'
            }
        }

/** Notícias e alertas gerados SOMENTE a partir do estado real do jogo (nada inventado). */
object NewsFeed {

    fun build(
        teamId: String,
        lastMatch: MatchResult?,
        offers: List<Offer>,
        standings: List<Standing>,
        finance: Finance,
    ): List<NewsItem> {
        val out = mutableListOf<NewsItem>()

        if (lastMatch != null) {
            val home = lastMatch.homeId == teamId
            val mine = if (home) lastMatch.homeGoals else lastMatch.awayGoals
            val theirs = if (home) lastMatch.awayGoals else lastMatch.homeGoals
            val opp = if (home) lastMatch.awayName else lastMatch.homeName
            val title = when {
                mine > theirs -> "Vitória sobre $opp"
                mine < theirs -> "Derrota para $opp"
                else -> "Empate com $opp"
            }
            out += NewsItem(
                NewsKind.RESULT, title,
                "${lastMatch.homeName} ${lastMatch.homeGoals} x ${lastMatch.awayGoals} ${lastMatch.awayName}",
            )

            val top = lastMatch.events
                .filter { it.goal && it.homeSide == home && it.player != null }
                .groupingBy { it.player!! }
                .eachCount()
                .maxByOrNull { it.value }
            if (top != null) {
                val goals = if (top.value == 1) "1 gol" else "${top.value} gols"
                out += NewsItem(NewsKind.PLAYER, "${top.key} marcou $goals", "Contra $opp")
            }
        }

        offers.take(3).forEach { o ->
            out += NewsItem(NewsKind.OFFER, "${o.company} quer negociar", o.kind.label)
        }

        if (standings.any { it.played > 0 }) {
            val pos = standings.indexOfFirst { it.teamId == teamId } + 1
            val me = standings.firstOrNull { it.teamId == teamId }
            if (pos > 0 && me != null) {
                out += NewsItem(
                    NewsKind.TABLE, "Você é o ${pos}º colocado",
                    "${me.points} pontos em ${me.played} jogos",
                )
            }
        }

        if (finance.balance < 0) {
            out += NewsItem(NewsKind.FINANCE, "Caixa no vermelho", finance.balance.money())
        }
        return out
    }

    /** Pontos que exigem ação do jogador. */
    fun alerts(lineupError: String?, finance: Finance, pendingOffers: Int): List<String> {
        val out = mutableListOf<String>()
        if (lineupError != null) out += lineupError
        if (finance.balance < 0) out += "Caixa negativo: ${finance.balance.money()}."
        if (pendingOffers > 0) {
            out += if (pendingOffers == 1) "1 proposta aguardando resposta." else "$pendingOffers propostas aguardando resposta."
        }
        return out
    }
}

package app.gmpfoot.domain

import kotlin.random.Random

enum class Formation(val label: String, val def: Int, val mid: Int, val att: Int) {
    F442("4-4-2", 4, 4, 2),
    F433("4-3-3", 4, 3, 3),
    F352("3-5-2", 3, 5, 2),
    F451("4-5-1", 4, 5, 1),
    F532("5-3-2", 5, 3, 2),
}

/** Mesmos 5 esquemas do Brasfoot base (Esquema.java). */
enum class Tactic(val label: String, val attack: Int, val defense: Int) {
    ALL_OUT("Ataque Total", 80, 20),
    OFFENSIVE("Ofensivo", 65, 35),
    BALANCED("Balanceado", 50, 50),
    DEFENSIVE("Defensivo", 35, 65),
    PARK_BUS("Defesa Total", 20, 80),
}

data class Lineup(val formation: Formation, val tactic: Tactic, val starters: List<String>)

fun Team.lineupError(l: Lineup): String? {
    val picked = squad.filter { it.id in l.starters }
    if (picked.size != 11) return "Escale 11 jogadores (${picked.size}/11)."
    if (picked.count { it.position == Position.GK } != 1) return "A escalação precisa de 1 goleiro."
    return null
}

fun Team.bestLineup(formation: Formation, tactic: Tactic): Lineup {
    fun top(p: Position, n: Int) = squad.filter { it.position == p }.sortedByDescending { it.overall }.take(n)
    val chosen = (top(Position.GK, 1) + top(Position.DEF, formation.def) +
        top(Position.MID, formation.mid) + top(Position.ATT, formation.att)).toMutableList()
    if (chosen.size < 11) {
        val rest = squad.filter { p -> chosen.none { it.id == p.id } && p.position != Position.GK }
            .sortedByDescending { it.overall }
        chosen += rest.take(11 - chosen.size)
    }
    return Lineup(formation, tactic, chosen.map { it.id })
}

data class Strength(val attack: Double, val defense: Double)

fun Team.strength(l: Lineup): Strength {
    val s = squad.filter { it.id in l.starters }
    fun avg(p: Position, fallback: Double): Double {
        val v = s.filter { it.position == p }.map { it.overall }
        return if (v.isEmpty()) fallback else v.average()
    }
    val gk = avg(Position.GK, 40.0)
    val d = avg(Position.DEF, 50.0)
    val m = avg(Position.MID, 50.0)
    val f = avg(Position.ATT, 50.0)
    val aF = 0.6 + l.tactic.attack / 100.0 * 0.8
    val dF = 0.6 + l.tactic.defense / 100.0 * 0.8
    return Strength((f * 0.6 + m * 0.4) * aF, (d * 0.5 + m * 0.2 + gk * 0.3) * dF)
}

/** Nota geral do time (média dos 11 melhores), usada na tela de escolha. */
fun Team.rating(): Int =
    squad.sortedByDescending { it.overall }.take(11).map { it.overall }.average().toInt()

data class MatchEvent(
    val minute: Int,
    val text: String,
    val homeSide: Boolean,
    val goal: Boolean,
    /** Autor do lance (nulo em saves antigos). */
    val player: String? = null,
)

data class MatchResult(
    val homeId: String,
    val awayId: String,
    val homeName: String,
    val awayName: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val events: List<MatchEvent>,
)

object MatchEngine {
    fun play(home: Team, hl: Lineup, away: Team, al: Lineup, rnd: Random = Random.Default): MatchResult {
        val hs = home.strength(hl)
        val aS = away.strength(al)
        val hAtt = hs.attack * 1.05 // fator casa
        val events = mutableListOf<MatchEvent>()
        var hg = 0
        var ag = 0

        for (minute in 1..90) {
            for (homeSide in listOf(true, false)) {
                val att = if (homeSide) hAtt else aS.attack
                val def = if (homeSide) aS.defense else hs.defense
                val r = att / def
                val pChance = (0.09 * r * r).coerceIn(0.03, 0.20)
                if (rnd.nextDouble() >= pChance) continue

                val atkTeam = if (homeSide) home else away
                val defTeam = if (homeSide) away else home
                val atkLineup = if (homeSide) hl else al
                val defLineup = if (homeSide) al else hl
                val shooters = atkTeam.squad.filter { it.id in atkLineup.starters && it.position != Position.GK }
                if (shooters.isEmpty()) continue
                val shooter = pickShooter(shooters, rnd)
                val keeper = defTeam.squad.firstOrNull { it.id in defLineup.starters && it.position == Position.GK }

                val conv = (0.17 * r).coerceIn(0.07, 0.35)
                if (rnd.nextDouble() < conv) {
                    if (homeSide) hg++ else ag++
                    events += MatchEvent(minute, "GOL! ${shooter.name} marca para o ${atkTeam.name}.", homeSide, true, shooter.name)
                } else if (rnd.nextBoolean()) {
                    val k = keeper?.name ?: "o goleiro"
                    events += MatchEvent(minute, "${shooter.name} finaliza e $k defende.", homeSide, false, shooter.name)
                } else {
                    events += MatchEvent(minute, "${shooter.name} chuta para fora.", homeSide, false, shooter.name)
                }
            }
        }
        return MatchResult(home.id, away.id, home.name, away.name, hg, ag, events)
    }

    private fun pickShooter(list: List<Player>, rnd: Random): Player {
        val weights = list.map {
            val w = when (it.position) { Position.ATT -> 5.0; Position.MID -> 3.0; else -> 1.0 }
            w * it.overall
        }
        var x = rnd.nextDouble() * weights.sum()
        for (i in list.indices) {
            x -= weights[i]
            if (x <= 0) return list[i]
        }
        return list.last()
    }
}

data class Fixture(val homeId: String, val awayId: String)

/** Turno e returno, método do círculo. Exige número par de times. */
fun roundRobin(ids: List<String>): List<List<Fixture>> {
    require(ids.size % 2 == 0) { "Número par de times" }
    val n = ids.size
    val list = ids.toMutableList()
    val rounds = mutableListOf<List<Fixture>>()
    for (r in 0 until n - 1) {
        val fx = mutableListOf<Fixture>()
        for (i in 0 until n / 2) {
            val a = list[i]
            val b = list[n - 1 - i]
            fx += if ((r + i) % 2 == 0) Fixture(a, b) else Fixture(b, a)
        }
        rounds += fx
        val last = list.removeAt(n - 1)
        list.add(1, last)
    }
    return rounds + rounds.map { rd -> rd.map { Fixture(it.awayId, it.homeId) } }
}

data class Standing(
    val teamId: String,
    val name: String,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val gf: Int,
    val ga: Int,
) {
    val played get() = won + drawn + lost
    val points get() = won * 3 + drawn
    val goalDiff get() = gf - ga
}

fun buildStandings(teams: List<Team>, results: List<MatchResult>): List<Standing> {
    val rows = teams.associate { it.id to intArrayOf(0, 0, 0, 0, 0) } // w d l gf ga
    for (m in results) {
        val h = rows[m.homeId] ?: continue
        val a = rows[m.awayId] ?: continue
        h[3] += m.homeGoals; h[4] += m.awayGoals
        a[3] += m.awayGoals; a[4] += m.homeGoals
        when {
            m.homeGoals > m.awayGoals -> { h[0]++; a[2]++ }
            m.homeGoals < m.awayGoals -> { a[0]++; h[2]++ }
            else -> { h[1]++; a[1]++ }
        }
    }
    return teams.map {
        val r = rows.getValue(it.id)
        Standing(it.id, it.name, r[0], r[1], r[2], r[3], r[4])
    }.sortedWith(
        compareByDescending<Standing> { it.points }
            .thenByDescending { it.goalDiff }
            .thenByDescending { it.gf }
            .thenBy { it.name }
    )
}

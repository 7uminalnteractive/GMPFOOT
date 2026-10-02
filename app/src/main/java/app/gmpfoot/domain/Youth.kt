package app.gmpfoot.domain

import kotlin.random.Random

/** Base PROCEDURAL (fictícia) usada enquanto não há dados reais de categorias de base. */
object Youth {
    private val first = listOf("Lucas", "Mateus", "Gabriel", "Pedro", "Rafael", "Davi", "Enzo", "Caio",
        "Bruno", "Thiago", "João", "Felipe", "Vitor", "Arthur", "Kauã", "Henrique")
    private val last = listOf("Silva", "Souza", "Oliveira", "Santos", "Lima", "Costa", "Pereira", "Almeida",
        "Ribeiro", "Carvalho", "Gomes", "Martins", "Rocha", "Barbosa", "Moreira", "Nunes")

    fun generate(team: Team): List<Player> {
        val rnd = Random(team.id.hashCode())
        val base = (team.rating() - 25).coerceAtLeast(35)
        val layout = listOf(Position.GK to 2, Position.DEF to 6, Position.MID to 6, Position.ATT to 4)
        val out = mutableListOf<Player>()
        for ((pos, n) in layout) repeat(n) {
            fun attr() = (base + rnd.nextInt(-8, 9)).coerceIn(30, 80)
            out += Player(
                id = "${team.id}_y${out.size}",
                name = "${first.random(rnd)} ${last.random(rnd)}",
                position = pos,
                age = rnd.nextInt(15, 20),
                technique = attr(), physical = attr(), intelligence = attr(), motivation = attr(),
                isYouth = true,
            )
        }
        return out
    }
}

fun Player.nextSeason(rnd: Random): Player {
    val a = age + 1
    fun g(v: Int): Int = when {
        a <= 21 -> v + rnd.nextInt(0, 5)
        a >= 33 -> v - rnd.nextInt(0, 4)
        else -> v + rnd.nextInt(-1, 2)
    }.coerceIn(1, 99)
    return copy(age = a, technique = g(technique), physical = g(physical),
        intelligence = g(intelligence), motivation = g(motivation))
}

fun Team.nextSeason(rnd: Random): Team =
    copy(squad = squad.map { it.nextSeason(rnd) }, youthSquad = youthSquad.map { it.nextSeason(rnd) })

fun Team.youthRating(): Int =
    if (youthSquad.isEmpty()) 0
    else youthSquad.sortedByDescending { it.overall }.take(11).map { it.overall }.average().toInt()

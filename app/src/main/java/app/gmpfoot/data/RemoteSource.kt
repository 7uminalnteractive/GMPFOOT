package app.gmpfoot.data

import app.gmpfoot.domain.Player
import app.gmpfoot.domain.Position
import app.gmpfoot.domain.Team
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

/**
 * Importa um teams.json (gerado pelo workflow do GitHub a partir da sua fonte de dados).
 * Formato: lista de times; cada jogador pode trazer "overall" OU technique/physical/intelligence/motivation.
 */
object RemoteSource {
    private data class PlayerDto(
        val id: String?, val name: String?, val position: String?, val age: Int?,
        val technique: Int?, val physical: Int?, val intelligence: Int?, val motivation: Int?,
        val overall: Int?, val marketValueEur: Long?,
    )

    private data class TeamDto(
        val id: String?, val name: String?, val country: String?,
        val squad: List<PlayerDto>?, val youthSquad: List<PlayerDto>?,
    )

    fun download(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000
        c.readTimeout = 20_000
        try {
            if (c.responseCode !in 200..299) error("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader(Charsets.UTF_8).readText()
        } finally {
            c.disconnect()
        }
    }

    private fun pos(s: String?) = when (s?.trim()?.uppercase()) {
        "GK", "GOLEIRO" -> Position.GK
        "DEF", "ZAGUEIRO", "LATERAL" -> Position.DEF
        "MID", "MEIA", "MEIO-CAMPO" -> Position.MID
        else -> Position.ATT
    }

    private fun player(d: PlayerDto, id: String, youth: Boolean): Player {
        val o = d.overall ?: 60
        return Player(
            id = d.id ?: id,
            name = d.name ?: "Jogador",
            position = pos(d.position),
            age = d.age ?: 25,
            technique = d.technique ?: o,
            physical = d.physical ?: o,
            intelligence = d.intelligence ?: o,
            motivation = d.motivation ?: o,
            marketValueEur = d.marketValueEur ?: 0,
            isYouth = youth,
        )
    }

    fun parse(json: String): List<Team> {
        val type = object : TypeToken<List<TeamDto>>() {}.type
        val dtos: List<TeamDto> = Gson().fromJson(json, type) ?: emptyList()
        return dtos.mapIndexed { i, t ->
            val tid = t.id ?: "t$i"
            Team(
                id = tid,
                name = t.name ?: "Time $i",
                country = t.country ?: "",
                squad = (t.squad ?: emptyList()).mapIndexed { k, p -> player(p, "${tid}_$k", false) },
                youthSquad = (t.youthSquad ?: emptyList()).mapIndexed { k, p -> player(p, "${tid}_y$k", true) },
            )
        }.filter { it.squad.size >= 11 }
    }
}

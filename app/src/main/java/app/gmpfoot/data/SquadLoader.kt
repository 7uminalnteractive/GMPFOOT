package app.gmpfoot.data

import android.content.Context
import app.gmpfoot.R
import app.gmpfoot.domain.Player
import app.gmpfoot.domain.Position
import app.gmpfoot.domain.Team

/** Lê o jogadores.txt do Brasfoot base: linha de nome do time, depois "titular,nome,posição,idade,téc,fís,int,mot". */
object SquadLoader {
    private fun position(s: String) = when (s.trim().lowercase()) {
        "goleiro" -> Position.GK
        "zagueiro" -> Position.DEF
        "meia" -> Position.MID
        else -> Position.ATT
    }

    /** Prefere o teams.json importado (dados reais); senão usa o jogadores.txt do Brasfoot base. */
    fun load(ctx: Context): List<Team> {
        val local = java.io.File(ctx.filesDir, "teams.json")
        if (local.exists()) {
            try {
                val t = RemoteSource.parse(local.readText())
                if (t.size >= 2 && t.size % 2 == 0) return t
            } catch (e: Exception) { /* cai para o arquivo base */ }
        }
        return loadBase(ctx)
    }

    private fun loadBase(ctx: Context): List<Team> {
        val teams = mutableListOf<Team>()
        var name: String? = null
        var players = mutableListOf<Player>()

        fun flush() {
            val n = name ?: return
            teams += Team("t${teams.size}", n, "", players.toList())
        }

        ctx.resources.openRawResource(R.raw.jogadores).bufferedReader(Charsets.UTF_8).useLines { lines ->
            for (raw in lines) {
                val line = raw.trim().trimStart('\uFEFF')
                if (line.isEmpty()) continue
                if (line[0].isDigit()) {
                    val p = line.split(",")
                    if (p.size < 8) continue
                    players += Player(
                        id = "t${teams.size}_${players.size}",
                        name = p[1].trim(),
                        position = position(p[2]),
                        age = p[3].trim().toIntOrNull() ?: 25,
                        technique = p[4].trim().toIntOrNull() ?: 50,
                        physical = p[5].trim().toIntOrNull() ?: 50,
                        intelligence = p[6].trim().toIntOrNull() ?: 50,
                        motivation = p[7].trim().toIntOrNull() ?: 50,
                    )
                } else {
                    flush()
                    name = line
                    players = mutableListOf()
                }
            }
        }
        flush()
        return teams
    }
}

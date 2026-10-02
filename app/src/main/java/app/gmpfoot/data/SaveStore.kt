package app.gmpfoot.data

import app.gmpfoot.domain.*
import com.google.gson.Gson
import java.io.File

data class SaveData(
    val live: Boolean,
    val base: Boolean,
    val myTeamId: String,
    val round: Int,
    val season: Int,
    val lineup: Lineup?,
    val results: List<MatchResult>,
    val trophies: List<Trophy>,
    val fixtures: List<List<Fixture>>,
    val teams: List<Team>,
    val finance: Finance,
    val offers: List<Offer>,
    val lastRoundFinance: RoundFinance?,
    val lastDividend: Long,
)

class SaveStore(private val dir: File) {
    private val gson = Gson()
    private val file get() = File(dir, "save.json")

    fun exists() = file.exists()
    fun toJson(d: SaveData): String = gson.toJson(d)

    fun write(json: String) {
        val tmp = File(dir, "save.json.tmp")
        tmp.writeText(json)
        tmp.renameTo(file)
    }

    fun read(): SaveData? = try {
        gson.fromJson(file.readText(), SaveData::class.java)
    } catch (e: Exception) {
        null
    }
}

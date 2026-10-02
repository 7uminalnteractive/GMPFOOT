package app.gmpfoot.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.gmpfoot.data.RemoteSource
import app.gmpfoot.data.SaveData
import app.gmpfoot.data.SaveStore
import app.gmpfoot.data.SquadLoader
import app.gmpfoot.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

class GameViewModel(private val app: Application) : AndroidViewModel(app) {
    private val store = SaveStore(app.filesDir)
    private val prefs = app.getSharedPreferences("gmpfoot", Context.MODE_PRIVATE)
    private val rnd = Random.Default

    /** Times disponíveis para iniciar um jogo (dados base ou importados). */
    var proTeams by mutableStateOf(prepare(SquadLoader.load(app)))
        private set

    /** Liga em uso (times principais ou Sub-20, conforme o modo). */
    var teams by mutableStateOf(proTeams)
        private set

    var live by mutableStateOf(false)
    var base by mutableStateOf(false)
    var myTeamId by mutableStateOf<String?>(null)
    var round by mutableIntStateOf(0)
    var season by mutableIntStateOf(1)
    var lineup by mutableStateOf<Lineup?>(null)
    var lastRound by mutableStateOf<List<MatchResult>>(emptyList())
    var finance by mutableStateOf(Finance(0, Stadium("", 0, 0)))
    var offers by mutableStateOf<List<Offer>>(emptyList())
    var lastRoundFinance by mutableStateOf<RoundFinance?>(null)
    var lastDividend by mutableStateOf(0L)
    var hasSave by mutableStateOf(store.exists())
        private set
    var academyLessons by mutableIntStateOf(prefs.getInt("academy_lessons", 0))
        private set

    val results = mutableStateListOf<MatchResult>()
    val trophies = mutableStateListOf<Trophy>()
    private var fixtures: List<List<Fixture>> = emptyList()

    val myTeam: Team? get() = teams.firstOrNull { it.id == myTeamId }
    val totalRounds: Int get() = fixtures.size
    val finished: Boolean get() = fixtures.isNotEmpty() && round >= fixtures.size
    val standings: List<Standing> get() = buildStandings(teams, results)
    fun team(id: String): Team = teams.first { it.id == id }

    private fun prepare(list: List<Team>): List<Team> =
        list.map { if (it.youthSquad.isEmpty()) it.copy(youthSquad = Youth.generate(it)) else it }

    // ---------- ciclo do jogo ----------

    fun start(teamId: String, live: Boolean, base: Boolean) {
        this.live = live
        this.base = base
        teams = if (base) proTeams.map { Team(it.id, "${it.name} Sub-20", it.country, it.youthSquad) } else proTeams
        myTeamId = teamId
        season = 1
        trophies.clear()
        finance = Economy.startingFinance(myTeam!!)
        lastDividend = 0
        beginSeason()
    }

    fun newSeason() {
        val t = myTeam ?: return
        season++
        val (f, dividend) = Economy.endSeason(finance, t.name)
        finance = f
        lastDividend = dividend
        teams = teams.map { it.nextSeason(rnd) }
        beginSeason()
    }

    private fun beginSeason() {
        round = 0
        results.clear()
        lastRound = emptyList()
        lastRoundFinance = null
        fixtures = roundRobin(teams.map { it.id })
        lineup = myTeam?.bestLineup(Formation.F442, Tactic.BALANCED)
        finance = finance.copy(seasonStartBalance = finance.balance)
        offers = Economy.generateOffers(myTeam!!, finance, rnd, "s$season-a")
        persist()
    }

    fun nextFixture(): Fixture? {
        val id = myTeamId ?: return null
        if (finished) return null
        return fixtures[round].firstOrNull { it.homeId == id || it.awayId == id }
    }

    fun lineupError(): String? {
        val t = myTeam ?: return "Sem time."
        val l = lineup ?: return "Sem escalação."
        return t.lineupError(l)
    }

    fun myMatch(): MatchResult? {
        val id = myTeamId ?: return null
        return lastRound.firstOrNull { it.homeId == id || it.awayId == id }
    }

    fun playRound() {
        if (finished || lineupError() != null) return
        val me = myTeamId ?: return
        val played = fixtures[round].map { f ->
            val home = team(f.homeId)
            val away = team(f.awayId)
            val hl = if (home.id == me) lineup!! else aiLineup(home)
            val al = if (away.id == me) lineup!! else aiLineup(away)
            MatchEngine.play(home, hl, away, al, rnd)
        }
        results += played
        lastRound = played
        round++
        applyRoundFinance(played, me)

        if (round == totalRounds / 2 && !finished) {
            offers = Economy.generateOffers(myTeam!!, finance, rnd, "s$season-b")
        }
        if (finished && standings.firstOrNull()?.teamId == me) {
            trophies += Trophy(if (base) "Campeonato Sub-20" else "Campeonato", "Temporada $season", TrophyKind.LEAGUE)
        }
        persist()
    }

    private fun applyRoundFinance(played: List<MatchResult>, me: String) {
        val m = played.firstOrNull { it.homeId == me || it.awayId == me } ?: return
        val t = myTeam ?: return
        val home = m.homeId == me
        val rank = standings.indexOfFirst { it.teamId == me } + 1
        val att = if (home) Economy.attendance(finance.stadium, t.rating(), rank, teams.size) else 0
        val tickets = att.toLong() * finance.stadium.ticketPrice
        val won = if (home) m.homeGoals > m.awayGoals else m.awayGoals > m.homeGoals
        val sponsors = Economy.dealsPerRound(finance, totalRounds) + if (won) Economy.winBonus(finance) else 0L
        val wages = Economy.wagesPerRound(t)
        val rf = RoundFinance(home, att, tickets, sponsors, wages)
        lastRoundFinance = rf
        finance = finance.copy(balance = finance.balance + rf.net)
    }

    private fun aiLineup(t: Team): Lineup =
        t.bestLineup(Formation.values().random(rnd), Tactic.values().random(rnd))

    // ---------- finanças, propostas, estádio ----------

    fun acceptOffer(o: Offer) {
        finance = Economy.accept(finance, o)
        offers = offers.filter { it.id != o.id && !(o.kind == OfferKind.SAF && it.kind == OfferKind.SAF) }
        persist()
    }

    fun declineOffer(o: Offer) {
        offers = offers.filter { it.id != o.id }
        persist()
    }

    fun setTicketPrice(price: Int) {
        finance = finance.copy(stadium = finance.stadium.copy(ticketPrice = price))
    }

    fun commitTicketPrice() = persist()

    fun expandStadium(): Boolean {
        val f = Economy.expand(finance) ?: return false
        finance = f
        persist()
        return true
    }

    // ---------- base ----------

    fun promote(p: Player) {
        val me = myTeamId ?: return
        if (base) return
        teams = teams.map {
            if (it.id == me) it.copy(
                squad = it.squad + p.copy(isYouth = false),
                youthSquad = it.youthSquad.filter { y -> y.id != p.id },
            ) else it
        }
        persist()
    }

    // ---------- salvar / carregar ----------

    private fun persist() {
        val id = myTeamId ?: return
        val data = SaveData(
            live, base, id, round, season, lineup, results.toList(), trophies.toList(),
            fixtures, teams, finance, offers, lastRoundFinance, lastDividend,
        )
        val json = store.toJson(data)
        hasSave = true
        viewModelScope.launch(Dispatchers.IO) { store.write(json) }
    }

    fun loadSave(): Boolean {
        val d = store.read() ?: return false
        live = d.live
        base = d.base
        teams = d.teams
        myTeamId = d.myTeamId
        round = d.round
        season = d.season
        lineup = d.lineup
        results.clear(); results.addAll(d.results)
        trophies.clear(); trophies.addAll(d.trophies)
        fixtures = d.fixtures
        finance = d.finance
        offers = d.offers
        lastRoundFinance = d.lastRoundFinance
        lastDividend = d.lastDividend
        lastRound = emptyList()
        return true
    }

    // ---------- dados reais ----------

    fun importData(url: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val msg = try {
                val json = withContext(Dispatchers.IO) { RemoteSource.download(url) }
                val parsed = RemoteSource.parse(json)
                when {
                    parsed.size < 2 -> "Nenhum time válido encontrado (cada time precisa de 11+ jogadores)."
                    parsed.size % 2 != 0 -> "Preciso de número par de times (recebi ${parsed.size})."
                    else -> {
                        withContext(Dispatchers.IO) { File(app.filesDir, "teams.json").writeText(json) }
                        proTeams = prepare(parsed)
                        "${parsed.size} times importados. Inicie uma nova carreira para usar."
                    }
                }
            } catch (e: Exception) {
                "Falha ao importar: ${e.message}"
            }
            onDone(msg)
        }
    }

    fun resetData() {
        File(app.filesDir, "teams.json").delete()
        proTeams = prepare(SquadLoader.load(app))
    }

    // ---------- academia ----------

    fun completeLesson(n: Int) {
        if (n > academyLessons) {
            academyLessons = n
            prefs.edit().putInt("academy_lessons", n).apply()
        }
    }
}

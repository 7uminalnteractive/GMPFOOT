package app.gmpfoot.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.gmpfoot.domain.Team
import app.gmpfoot.domain.money
import app.gmpfoot.domain.rating
import app.gmpfoot.ui.components.*

private val Tabs = listOf("Central", "Elenco", "Transferências", "Temporada", "Clube", "Finanças")
private const val TAB_CENTRAL = 0
private const val TAB_SQUAD = 1
private const val TAB_MARKET = 2

/**
 * Tela principal da carreira (substitui o antigo GameHubScreen): cabeçalho do clube,
 * 6 categorias em abas e o conteúdo da categoria escolhida.
 * Toda a lógica vem do GameViewModel; aqui só há apresentação.
 */
@Composable
fun CentralScreen(vm: GameViewModel, onOpen: (String) -> Unit, onExit: () -> Unit) {
    val team = vm.myTeam ?: return
    var tab by rememberSaveable { mutableStateOf(TAB_CENTRAL) }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        ClubHeader(vm, team, onExit)
        TabNavigation(Tabs, tab, { tab = it })
        HorizontalDivider(color = Gmp.Line)
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "central-tab",
            modifier = Modifier.weight(1f),
        ) { t ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (t) {
                    TAB_CENTRAL -> CentralTab(vm, team, onOpen) { tab = it }
                    TAB_SQUAD -> SquadTab(vm, team, onOpen)
                    TAB_MARKET -> MarketTab(vm, onOpen)
                    3 -> SeasonTab(vm, onOpen)
                    4 -> ClubTab(vm, onOpen)
                    else -> FinanceTab(vm, onOpen)
                }
            }
        }
    }
}

@Composable
private fun ClubHeader(vm: GameViewModel, team: Team, onExit: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Crest(team.name, 48.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                team.name.uppercase(), style = MaterialTheme.typography.headlineSmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                "TEMPORADA ${vm.season} · ${vm.competitionName.uppercase()}",
                style = MaterialTheme.typography.labelMedium, color = Gmp.TextDim,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("OVR", style = MaterialTheme.typography.labelMedium, color = Gmp.TextDim)
            Text("${team.rating()}", style = MaterialTheme.typography.headlineSmall, color = Gmp.Accent)
        }
        IconButton(onClick = onExit) { Icon(Icons.Filled.Menu, contentDescription = "Menu principal") }
    }
}

// ---------------------------------------------------------------- CENTRAL

@Composable
private fun CentralTab(vm: GameViewModel, team: Team, onOpen: (String) -> Unit, onTab: (Int) -> Unit) {
    SectionHeader("Próxima partida")
    val fixture = vm.nextFixture()
    val err = vm.lineupError()

    if (vm.finished) {
        val champ = vm.standings.first()
        GameCard(Modifier.fillMaxWidth(), highlight = true) {
            Text("TEMPORADA ENCERRADA", style = MaterialTheme.typography.labelMedium, color = Gmp.TextDim)
            Spacer(Modifier.height(6.dp))
            Text(
                if (champ.teamId == team.id) "VOCÊ É O CAMPEÃO!" else "CAMPEÃO: ${champ.name.uppercase()}",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text("${champ.points} pontos", color = Gmp.TextDim)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { vm.newSeason() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) { Text("NOVA TEMPORADA", style = MaterialTheme.typography.labelLarge) }
        }
    } else if (fixture != null) {
        val home = fixture.homeId == team.id
        val opp = vm.team(if (home) fixture.awayId else fixture.homeId)
        GameCard(Modifier.fillMaxWidth(), highlight = true) {
            Text(
                "RODADA ${vm.round + 1} DE ${vm.totalRounds} · ${vm.competitionName.uppercase()}",
                style = MaterialTheme.typography.labelMedium, color = Gmp.TextDim,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TeamBadge(team.name, team.rating(), Modifier.weight(1f))
                Text("VS", style = MaterialTheme.typography.titleLarge, color = Gmp.TextDim)
                TeamBadge(opp.name, opp.rating(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (home) "EM CASA · ${vm.finance.stadium.name.uppercase()}" else "FORA DE CASA",
                style = MaterialTheme.typography.labelMedium, color = Gmp.TextDim,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (err != null) {
                Spacer(Modifier.height(8.dp))
                Text(err, color = Gmp.Warn, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { vm.playRound(); onOpen("match") },
                enabled = err == null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("JOGAR PARTIDA", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    SectionHeader("Forma recente")
    val form = vm.myForm
    if (form.isEmpty()) {
        Text("Nenhuma partida disputada ainda.", color = Gmp.TextDim)
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { form.forEach { FormBadge(it) } }
    }

    val alerts = vm.alerts()
    if (alerts.isNotEmpty()) {
        SectionHeader("Atenção")
        GameCard(Modifier.fillMaxWidth()) {
            alerts.forEachIndexed { i, a ->
                if (i > 0) Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Warning, null, tint = Gmp.Warn, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(a, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    SectionHeader("Atividade recente")
    val news = vm.newsFeed()
    if (news.isEmpty()) Text("Sem novidades por enquanto.", color = Gmp.TextDim)
    news.forEach { NewsCard(it) }

    SectionHeader("Acesso rápido")
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickTile("Elenco", Icons.Filled.Groups, { onTab(TAB_SQUAD) }, Modifier.weight(1f))
        QuickTile("Escalação", Icons.Filled.SportsSoccer, { onOpen("lineup") }, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickTile("Táticas", Icons.Filled.Tune, { onOpen("lineup") }, Modifier.weight(1f))
        QuickTile("Mercado", Icons.Filled.SwapHoriz, { onTab(TAB_MARKET) }, Modifier.weight(1f))
    }
}

// ---------------------------------------------------------------- ELENCO

@Composable
private fun SquadTab(vm: GameViewModel, team: Team, onOpen: (String) -> Unit) {
    SectionHeader("Resumo do elenco")
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Jogadores", "${team.squad.size}", Modifier.weight(1f))
        StatCard("OVR", "${team.rating()}", Modifier.weight(1f), Gmp.Accent)
        StatCard("Idade média", "%.1f".format(team.squad.map { it.age }.average()), Modifier.weight(1f))
    }
    SectionHeader("Gerenciar")
    MenuTile("Escalação e táticas", "Formação, esquema e titulares", Icons.Filled.Tune, { onOpen("lineup") })
    MenuTile(
        "Base", if (vm.base) "Você já comanda a base" else "Promova talentos ao elenco",
        Icons.Filled.Star, { onOpen("youth") }, enabled = !vm.base,
    )
}

// ---------------------------------------------------------------- TRANSFERÊNCIAS

@Composable
private fun MarketTab(vm: GameViewModel, onOpen: (String) -> Unit) {
    SectionHeader("Mercado")
    MenuTile("Jogadores", "Compra e venda com cláusulas", Icons.Filled.SwapHoriz, { onOpen("transfer") })
    MenuTile("Técnicos", "Demitidos e disponíveis", Icons.Filled.Person, { onOpen("managers") })
    MenuTile(
        "Propostas", "Patrocínio, naming rights e SAF", Icons.Filled.Mail, { onOpen("offers") },
        badge = vm.offers.size.takeIf { it > 0 }?.toString(),
    )
}

// ---------------------------------------------------------------- TEMPORADA

@Composable
private fun SeasonTab(vm: GameViewModel, onOpen: (String) -> Unit) {
    val me = vm.standings.firstOrNull { it.teamId == vm.myTeamId }
    val pos = vm.standings.indexOfFirst { it.teamId == vm.myTeamId } + 1
    SectionHeader("Sua campanha")
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Posição", if (me != null && me.played > 0) "${pos}º" else "—", Modifier.weight(1f), Gmp.Accent)
        StatCard("Pontos", "${me?.points ?: 0}", Modifier.weight(1f))
        StatCard("Rodada", "${minOf(vm.round + 1, vm.totalRounds)}/${vm.totalRounds}", Modifier.weight(1f))
    }
    SectionHeader("Competição")
    MenuTile("Classificação", "Tabela do campeonato", Icons.Filled.Leaderboard, { onOpen("standings") })
}

// ---------------------------------------------------------------- CLUBE

@Composable
private fun ClubTab(vm: GameViewModel, onOpen: (String) -> Unit) {
    SectionHeader("Clube")
    MenuTile("Sala de troféus", "Suas conquistas", Icons.Filled.EmojiEvents, { onOpen("trophies") },
        badge = vm.trophies.size.takeIf { it > 0 }?.toString())
    MenuTile("Estádio", "${vm.finance.stadium.name} · ${vm.finance.stadium.capacity} lugares",
        Icons.Filled.Home, { onOpen("finance") })
    MenuTile("Academia de treinadores", "Aulas e licença", Icons.Filled.School, { onOpen("academy") })
}

// ---------------------------------------------------------------- FINANÇAS

@Composable
private fun FinanceTab(vm: GameViewModel, onOpen: (String) -> Unit) {
    val f = vm.finance
    SectionHeader("Caixa")
    StatCard(
        "Saldo", f.balance.money(), Modifier.fillMaxWidth(),
        if (f.balance < 0) Gmp.Danger else Gmp.Accent,
    )
    vm.lastRoundFinance?.let { r ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Receitas", (r.tickets + r.sponsors).money(), Modifier.weight(1f))
            StatCard("Salários", "-" + r.wages.money(), Modifier.weight(1f), Gmp.Warn)
        }
    }
    SectionHeader("Gestão")
    MenuTile("Finanças e estádio", "Ingressos, ampliação e contratos", Icons.Filled.AccountBalance, { onOpen("finance") })
    MenuTile(
        "Propostas", "Patrocínio, naming rights e SAF", Icons.Filled.Mail, { onOpen("offers") },
        badge = vm.offers.size.takeIf { it > 0 }?.toString(),
    )
}

package app.gmpfoot.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gmpfoot.domain.money
import app.gmpfoot.domain.rating

@Composable
private fun Tile(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun GameHubScreen(vm: GameViewModel, onOpen: (String) -> Unit, onBack: () -> Unit) {
    val team = vm.myTeam ?: return
    val fixture = vm.nextFixture()
    val err = vm.lineupError()

    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Menu") }
        Text(team.name, style = MaterialTheme.typography.headlineMedium)
        Text(
            (if (vm.live) "Ao Vivo" else "Profissional") + (if (vm.base) " · Base" else "") +
                " · Temporada ${vm.season} · nota ${team.rating()}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Caixa ${vm.finance.balance.money()}",
            color = if (vm.finance.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )

        if (vm.finished) {
            val champ = vm.standings.first()
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Temporada encerrada", style = MaterialTheme.typography.titleMedium)
                    Text("Campeão: ${champ.name} (${champ.points} pts)")
                    Button(onClick = { vm.newSeason() }, Modifier.fillMaxWidth()) { Text("Nova temporada") }
                }
            }
        } else if (fixture != null) {
            val home = fixture.homeId == team.id
            val opp = vm.team(if (home) fixture.awayId else fixture.homeId)
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Rodada ${vm.round + 1}/${vm.totalRounds} · ${if (home) "Em casa" else "Fora"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("vs ${opp.name} (nota ${opp.rating()})", style = MaterialTheme.typography.titleMedium)
                    if (err != null) Text(err, color = MaterialTheme.colorScheme.error)
                    Button(
                        onClick = { vm.playRound(); onOpen("match") },
                        enabled = err == null,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Jogar rodada") }
                }
            }
        }

        Tile("Escalação", "Formação, esquema tático e titulares") { onOpen("lineup") }
        Tile("Classificação", "Tabela do campeonato") { onOpen("standings") }
        Tile("Finanças e estádio", "Caixa, ingressos e ampliação") { onOpen("finance") }
        Tile("Propostas", "${vm.offers.size} pendentes: patrocínio, naming rights, SAF") { onOpen("offers") }
        if (!vm.base) Tile("Categorias de Base", "Promova talentos ao elenco") { onOpen("youth") }
        Tile("Mercado de Jogadores", "Compra e venda com cláusulas") { onOpen("transfer") }
        Tile("Mercado de Técnicos", "Demitidos e disponíveis") { onOpen("managers") }
        Tile("Sala de Troféus", "${vm.trophies.size} conquistas") { onOpen("trophies") }
    }
}

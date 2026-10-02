package app.gmpfoot.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gmpfoot.domain.*

private fun Position.short() = when (this) {
    Position.GK -> "GOL"
    Position.DEF -> "ZAG"
    Position.MID -> "MEI"
    Position.ATT -> "ATA"
}

@Composable
fun LineupScreen(vm: GameViewModel, onBack: () -> Unit) {
    val team = vm.myTeam ?: return
    val lineup = vm.lineup ?: return
    val err = vm.lineupError()
    val ordered = team.squad.sortedWith(compareBy({ it.position.ordinal }, { -it.overall }))

    Column(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Escalação", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Formation.values().forEach { f ->
                FilterChip(
                    selected = lineup.formation == f,
                    onClick = { vm.lineup = team.bestLineup(f, lineup.tactic) },
                    label = { Text(f.label) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tactic.values().forEach { t ->
                FilterChip(
                    selected = lineup.tactic == t,
                    onClick = { vm.lineup = lineup.copy(tactic = t) },
                    label = { Text(t.label) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            err ?: "Escalação válida · ${lineup.formation.label} · ${lineup.tactic.label}",
            color = if (err != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
        TextButton(onClick = { vm.lineup = team.bestLineup(lineup.formation, lineup.tactic) }) {
            Text("Escalar melhores")
        }

        LazyColumn {
            items(ordered) { p ->
                val on = p.id in lineup.starters
                Row(
                    Modifier.fillMaxWidth().clickable {
                        val s = if (on) lineup.starters - p.id else lineup.starters + p.id
                        vm.lineup = lineup.copy(starters = s)
                    }.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = on, onCheckedChange = null)
                    Spacer(Modifier.width(12.dp))
                    Text(p.position.short(), Modifier.width(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(p.name, Modifier.weight(1f))
                    Text("${p.overall}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

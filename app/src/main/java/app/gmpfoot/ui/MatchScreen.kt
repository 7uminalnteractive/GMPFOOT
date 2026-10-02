package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MatchScreen(vm: GameViewModel, onDone: () -> Unit) {
    val match = vm.myMatch()
    if (match == null) {
        LaunchedEffect(Unit) { onDone() }
        return
    }
    var shown by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(match) {
        shown = 0
        finished = false
        for (i in match.events.indices) {
            delay(450)
            shown = i + 1
        }
        delay(300)
        finished = true
    }

    val visible = match.events.take(shown)
    val hg = visible.count { it.goal && it.homeSide }
    val ag = visible.count { it.goal && !it.homeSide }
    val minute = if (finished) 90 else visible.lastOrNull()?.minute ?: 0

    Column(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        Text(
            if (finished) "Fim de jogo" else "$minute'",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(match.homeName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("$hg x $ag", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(match.awayName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.End)
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visible.reversed()) { e ->
                Row {
                    Text("${e.minute}'", Modifier.width(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        e.text,
                        fontWeight = if (e.goal) FontWeight.Bold else null,
                        color = if (e.goal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            if (finished) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Outros resultados", style = MaterialTheme.typography.titleSmall)
                }
                items(vm.lastRound.filter { it !== match }) { r ->
                    Text("${r.homeName} ${r.homeGoals} x ${r.awayGoals} ${r.awayName}")
                }
            }
        }

        Button(onClick = onDone, Modifier.fillMaxWidth()) {
            Text(if (finished) "Continuar" else "Pular")
        }
    }
}

package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
private fun Cell(text: String, w: Int, bold: Boolean = false, color: androidx.compose.ui.graphics.Color? = null) {
    Text(
        text,
        Modifier.width(w.dp),
        fontWeight = if (bold) FontWeight.Bold else null,
        color = color ?: MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun StandingsScreen(vm: GameViewModel, onBack: () -> Unit) {
    val rows = vm.standings
    Column(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Classificação", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Row {
            Cell("#", 24); Text("Time", Modifier.weight(1f))
            Cell("P", 28, true); Cell("J", 24); Cell("V", 24); Cell("E", 24); Cell("D", 24); Cell("SG", 32)
        }
        HorizontalDivider(Modifier.padding(vertical = 6.dp))
        LazyColumn {
            itemsIndexed(rows) { i, s ->
                val me = s.teamId == vm.myTeamId
                val c = if (me) MaterialTheme.colorScheme.primary else null
                Row(Modifier.padding(vertical = 5.dp)) {
                    Cell("${i + 1}", 24, me, c)
                    Text(
                        s.name, Modifier.weight(1f),
                        fontWeight = if (me) FontWeight.Bold else null,
                        color = c ?: MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                    )
                    Cell("${s.points}", 28, true, c)
                    Cell("${s.played}", 24, false, c)
                    Cell("${s.won}", 24, false, c)
                    Cell("${s.drawn}", 24, false, c)
                    Cell("${s.lost}", 24, false, c)
                    Cell("${s.goalDiff}", 32, false, c)
                }
            }
        }
    }
}

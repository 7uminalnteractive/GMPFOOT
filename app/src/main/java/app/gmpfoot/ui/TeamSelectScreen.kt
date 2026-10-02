package app.gmpfoot.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gmpfoot.domain.rating
import app.gmpfoot.domain.youthRating
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun TeamSelectScreen(vm: GameViewModel, live: Boolean, onStart: () -> Unit, onBack: () -> Unit) {
    var base by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Escolha seu time", style = MaterialTheme.typography.headlineSmall)
        Text(
            if (live) "Carreira Ao Vivo" else "Carreira Profissional",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !base, onClick = { base = false }, label = { Text("Clube principal") })
            FilterChip(selected = base, onClick = { base = true }, label = { Text("Clube de base (Sub-20)") })
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.proTeams.sortedByDescending { if (base) it.youthRating() else it.rating() }) { t ->
                Card(
                    Modifier.fillMaxWidth().clickable {
                        vm.start(t.id, live, base)
                        onStart()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(t.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (base) "${t.youthSquad.size} jogadores da base" else "${t.squad.size} jogadores",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            "${if (base) t.youthRating() else t.rating()}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

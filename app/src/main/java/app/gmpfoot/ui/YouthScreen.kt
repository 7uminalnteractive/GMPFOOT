package app.gmpfoot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun YouthScreen(vm: GameViewModel, onBack: () -> Unit) {
    val team = vm.myTeam ?: return
    Column(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Categorias de Base", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Promova talentos ao elenco principal. (Base fictícia até importar dados reais.)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(team.youthSquad.sortedByDescending { it.overall }) { p ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.name)
                        Text("${p.age} anos · ${p.position}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${p.overall}", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(onClick = { vm.promote(p) }) { Text("Promover") }
                }
            }
        }
    }
}

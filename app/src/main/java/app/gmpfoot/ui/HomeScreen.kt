package app.gmpfoot.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Entry(val title: String, val subtitle: String, val route: String)

private val entries = listOf(
    Entry("Carreira Profissional", "Temporada, finanças, diretoria e metas", "select/pro"),
    Entry("Carreira Ao Vivo", "Elenco e calendário seguindo o mundo real", "select/live"),
    Entry("Academia de Treinadores", "Tutorial com aulas e licença", "academy"),
    Entry("Editor de Times", "Importe times, jogadores e a base real", "editor"),
)

@Composable
fun HomeScreen(hasSave: Boolean, onOpen: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("GMPFoot", style = MaterialTheme.typography.displaySmall)
        Text(
            "Gerencie. Negocie. Evolua.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        val all = if (hasSave) listOf(Entry("Continuar jogo", "Retome sua carreira salva", "continue")) + entries else entries
        all.forEach { e ->
            Card(
                Modifier.fillMaxWidth().clickable { onOpen(e.route) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(e.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        e.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

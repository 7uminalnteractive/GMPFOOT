package app.gmpfoot.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class HubItem(val title: String, val subtitle: String, val route: String)

private val items = listOf(
    HubItem("Mercado de Jogadores", "Compra e venda com cláusulas", "transfer"),
    HubItem("Mercado de Técnicos", "Demitidos e disponíveis", "managers"),
    HubItem("Sala de Troféus", "Suas conquistas", "trophies"),
)

@Composable
fun CareerHubScreen(live: Boolean, onOpen: (String) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text(
            if (live) "Carreira Ao Vivo" else "Carreira Profissional",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            if (live) "Elencos, valores e calendário sincronizados com o mundo real."
            else "Temporada, finanças e diretoria.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        items.forEach { e ->
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

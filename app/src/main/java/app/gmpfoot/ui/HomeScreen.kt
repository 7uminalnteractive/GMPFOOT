package app.gmpfoot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.gmpfoot.ui.components.MenuTile
import app.gmpfoot.ui.components.SectionHeader

private data class Entry(val title: String, val subtitle: String, val route: String, val icon: ImageVector)

private val entries = listOf(
    Entry("Carreira Profissional", "Temporada, finanças, diretoria e metas", "select/pro", Icons.Filled.SportsSoccer),
    Entry("Carreira Ao Vivo", "Elenco e calendário seguindo o mundo real", "select/live", Icons.Filled.Public),
    Entry("Academia de Treinadores", "Tutorial com aulas e licença", "academy", Icons.Filled.School),
    Entry("Editor de Times", "Importe times, jogadores e a base real", "editor", Icons.Filled.Edit),
)

@Composable
fun HomeScreen(hasSave: Boolean, onOpen: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(38.dp).background(Gmp.Accent))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("GMPFOOT", style = MaterialTheme.typography.displaySmall)
                Text(
                    "GERENCIE. NEGOCIE. EVOLUA.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gmp.TextDim,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        if (hasSave) {
            SectionHeader("Sua carreira")
            MenuTile(
                "Continuar", "Retome de onde parou", Icons.Filled.PlayArrow,
                { onOpen("continue") },
            )
            Spacer(Modifier.height(4.dp))
        }
        SectionHeader("Novo jogo")
        entries.forEach { e -> MenuTile(e.title, e.subtitle, e.icon, { onOpen(e.route) }) }
    }
}

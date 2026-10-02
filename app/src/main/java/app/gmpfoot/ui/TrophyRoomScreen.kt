package app.gmpfoot.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import app.gmpfoot.domain.Trophy
import app.gmpfoot.domain.TrophyKind

private fun colorOf(kind: TrophyKind): Color = when (kind) {
    TrophyKind.LEAGUE -> Color(0xFFFFC83D)
    TrophyKind.CUP -> Color(0xFFD8DEE4)
    TrophyKind.CONTINENTAL -> Color(0xFF5AA9FF)
    TrophyKind.WORLD -> Color(0xFF4ADE80)
    TrophyKind.SUPERCUP -> Color(0xFFC084FC)
}

@Composable
private fun TrophyIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(56.dp, 72.dp)) {
        val w = size.width
        val h = size.height
        // taça
        val bowl = Path().apply {
            moveTo(w * 0.18f, 0f)
            lineTo(w * 0.82f, 0f)
            lineTo(w * 0.70f, h * 0.50f)
            lineTo(w * 0.30f, h * 0.50f)
            close()
        }
        drawPath(bowl, color)
        // alças
        drawCircle(color, radius = w * 0.14f, center = Offset(w * 0.10f, h * 0.18f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        drawCircle(color, radius = w * 0.14f, center = Offset(w * 0.90f, h * 0.18f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        // haste e base
        drawRect(color, topLeft = Offset(w * 0.44f, h * 0.50f), size = Size(w * 0.12f, h * 0.28f))
        drawRect(color, topLeft = Offset(w * 0.25f, h * 0.78f), size = Size(w * 0.50f, h * 0.14f))
    }
}

@Composable
private fun Shelf(trophies: List<Trophy>) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            trophies.forEach { t ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    TrophyIcon(colorOf(t.kind))
                    Text(t.season, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // completa a prateleira para manter o alinhamento
            repeat(3 - trophies.size) { Spacer(Modifier.weight(1f)) }
        }
        Box(
            Modifier.fillMaxWidth().height(8.dp).padding(top = 2.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) { drawRect(Color(0xFF2A332E)) }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun TrophyRoomScreen(trophies: List<Trophy>, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).statusBarsPadding().verticalScroll(rememberScrollState()),
    ) {
        TextButton(onClick = onBack) { Text("‹ Voltar") }
        Text("Sala de Troféus", style = MaterialTheme.typography.headlineSmall)
        Text("${trophies.size} conquistas", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        if (trophies.isEmpty()) {
            Text("Ganhe um título para preencher a prateleira.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trophies.chunked(3).forEach { Shelf(it) }
    }
}

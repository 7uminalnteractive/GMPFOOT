package app.gmpfoot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gmpfoot.domain.NewsItem
import app.gmpfoot.ui.Gmp

/** Bloco base de todo o app: superfície escura, borda discreta, cantos pequenos. */
@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val base = modifier
        .clip(shape)
        .background(Gmp.Surface)
        .border(1.dp, if (highlight) Gmp.Accent else Gmp.Line, shape)
    Column(if (onClick != null) base.clickable(onClick = onClick) else base) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(16.dp).background(Gmp.Accent))
        Spacer(Modifier.width(8.dp))
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (action != null && onAction != null) {
            Text(
                action.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Gmp.Accent,
                modifier = Modifier.clickable(onClick = onAction).padding(4.dp),
            )
        }
    }
}

/** Abas horizontais com indicador animado (categorias do menu de carreira). */
@Composable
fun TabNavigation(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        tabs.forEachIndexed { i, label ->
            val on = i == selected
            val color by animateColorAsState(if (on) Gmp.Accent else Gmp.TextDim, tween(160), label = "tabColor")
            val bar by animateDpAsState(if (on) 28.dp else 0.dp, tween(200), label = "tabBar")
            Column(
                Modifier.clickable { onSelect(i) }.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(label.uppercase(), style = MaterialTheme.typography.labelLarge, color = color, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.height(3.dp).width(bar).background(Gmp.Accent))
            }
        }
    }
}

/** Escudo provisório (iniciais). Sem assets de terceiros: o GMPFOOT usa identidade própria. */
@Composable
fun Crest(name: String, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val initials = name.split(" ").filter { it.isNotBlank() && it.first().isLetter() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    Box(
        modifier.size(size).clip(CircleShape).background(Gmp.Surface2).border(2.dp, Gmp.Accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, fontWeight = FontWeight.Black, fontSize = (size.value * 0.36f).sp, color = Gmp.Accent)
    }
}

@Composable
fun TeamBadge(name: String, rating: Int, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Crest(name, 56.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
        Text("OVR $rating", style = MaterialTheme.typography.labelMedium, color = Gmp.TextDim)
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    GameCard(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = Gmp.TextDim, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Linha de menu: ícone, título, descrição curta e seta. */
@Composable
fun MenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    enabled: Boolean = true,
) {
    GameCard(modifier.alpha(if (enabled) 1f else 0.4f), onClick = if (enabled) onClick else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Gmp.Surface2),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = Gmp.Accent) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Gmp.TextDim, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (badge != null) {
                Box(Modifier.clip(RoundedCornerShape(50)).background(Gmp.Accent).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(badge, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.width(6.dp))
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Gmp.TextDim)
        }
    }
}

/** Atalho vertical (ícone em cima, título embaixo) para grades 2x2 de acesso rápido. */
@Composable
fun QuickTile(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GameCard(modifier, onClick = onClick) {
        Icon(icon, null, tint = Gmp.Accent, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(10.dp))
        Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun FormBadge(result: Char, modifier: Modifier = Modifier) {
    val color = when (result) {
        'V' -> Gmp.Accent
        'D' -> Gmp.Danger
        else -> Gmp.Draw
    }
    Box(
        modifier.size(30.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(result.toString(), fontWeight = FontWeight.Black, color = Color(0xFF05100A), fontSize = 14.sp)
    }
}

@Composable
fun NewsCard(item: NewsItem, modifier: Modifier = Modifier) {
    GameCard(modifier.fillMaxWidth()) {
        Text(item.kind.label.uppercase(), style = MaterialTheme.typography.labelMedium, color = Gmp.Accent)
        Spacer(Modifier.height(2.dp))
        Text(item.title, style = MaterialTheme.typography.titleMedium)
        if (item.body.isNotEmpty()) {
            Text(item.body, style = MaterialTheme.typography.bodySmall, color = Gmp.TextDim)
        }
    }
}

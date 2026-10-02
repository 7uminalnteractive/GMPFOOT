package app.gmpfoot.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = darkColorScheme(
    primary = Color(0xFF4ADE80),
    onPrimary = Color(0xFF05260F),
    background = Color(0xFF0B0F0D),
    onBackground = Color(0xFFE8EEEA),
    surface = Color(0xFF141A17),
    onSurface = Color(0xFFE8EEEA),
    surfaceVariant = Color(0xFF1C2420),
    onSurfaceVariant = Color(0xFF9AA8A0),
)

@Composable
fun GMPFootTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Scheme, content = content)

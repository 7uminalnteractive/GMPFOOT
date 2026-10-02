package app.gmpfoot.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Paleta do GMPFOOT: fundo quase preto, verde-campo como acento, âmbar para atenção. */
object Gmp {
    val Bg = Color(0xFF080B10)
    val Surface = Color(0xFF111722)
    val Surface2 = Color(0xFF182131)
    val Line = Color(0xFF263145)
    val Accent = Color(0xFF4ADE80)
    val Warn = Color(0xFFFBBF24)
    val Danger = Color(0xFFF87171)
    val Draw = Color(0xFF94A3B8)
    val TextMain = Color(0xFFE9EEF5)
    val TextDim = Color(0xFF8B98AD)
}

private val Scheme = darkColorScheme(
    primary = Gmp.Accent,
    onPrimary = Color(0xFF04210F),
    background = Gmp.Bg,
    onBackground = Gmp.TextMain,
    surface = Gmp.Surface,
    onSurface = Gmp.TextMain,
    surfaceVariant = Gmp.Surface2,
    onSurfaceVariant = Gmp.TextDim,
    outline = Gmp.Line,
    error = Gmp.Danger,
)

private val baseType = Typography()
private val GmpType = baseType.copy(
    displaySmall = baseType.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 34.sp, letterSpacing = 3.sp),
    headlineMedium = baseType.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = baseType.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
    titleLarge = baseType.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = baseType.titleMedium.copy(fontWeight = FontWeight.Bold),
    labelLarge = baseType.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
    labelMedium = baseType.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
)

@Composable
fun GMPFootTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Scheme, typography = GmpType, content = content)

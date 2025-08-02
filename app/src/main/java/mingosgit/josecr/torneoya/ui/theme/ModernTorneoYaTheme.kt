package mingosgit.josecr.torneoya.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Blue = Color(0xFF296DFF)
private val Violet = Color(0xFF8F5CFF)
private val Accent = Color(0xFFFFB531)
private val BackgroundDark = Color(0xFF181B26)
private val BackgroundLight = Color(0xFFF4F6FF)
private val CardDark = Color(0xFF22263B)
private val CardLight = Color(0xFFF7F7FF)
private val SurfaceDark = Color(0xFF191B27)
private val SurfaceLight = Color(0xFFE7EAF9)
private val ChipBgDark = Color(0xFF24294A)
private val ChipBgLight = Color(0xFFE0E5FF)
private val TextLight = Color(0xFFF7F7FF)
private val TextDark = Color(0xFF13142C)
private val MutedText = Color(0xFFB7B7D1)

private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    onPrimary = TextLight,
    secondary = Violet,
    onSecondary = TextLight,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = CardDark,
    onSurfaceVariant = MutedText,
    outline = MutedText,
    tertiary = Accent,
)


@Composable
fun ModernTorneoYaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(
            displayLarge = MaterialTheme.typography.displayLarge.copy(letterSpacing = 0.5.sp),
            titleLarge = MaterialTheme.typography.titleLarge.copy(letterSpacing = 0.15.sp),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp),
            labelLarge = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.2.sp)
            // Puedes personalizar más según tus necesidades
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(6.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(19.dp),
            extraLarge = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}

// Colores de ayuda para usar en backgrounds personalizados, chips, etc.
object TorneoYaPalette {
    val blue get() = Blue
    val violet get() = Violet
    val accent get() = Accent
    val chipBgDark get() = ChipBgDark
    val chipBgLight get() = ChipBgLight
    val textLight get() = TextLight
    val mutedText get() = MutedText
}

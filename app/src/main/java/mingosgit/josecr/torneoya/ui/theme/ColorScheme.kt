package mingosgit.josecr.torneoya.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores base
private val Blue = Color(0xFF296DFF)
private val Violet = Color(0xFF8F5CFF)
private val Accent = Color(0xFFFFB531)
private val AccentDark = Color(0xFFA26500)
private val Yellow = Color(0xFFFFB531)
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
private val Errorw = Color(0xFFF44336)

// Paletas de Material 3
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
    error = Errorw,
)

private val WhiteColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = TextLight,
    secondary = Violet,
    onSecondary = TextDark,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = CardLight,
    onSurfaceVariant = MutedText,
    outline = MutedText,
    tertiary = AccentDark,
    error = Errorw,
)

// Gradientes para fondos
private val DarkBackgroundGradient = Brush.verticalGradient(
    0.0f to Color(0xFF1B1D29),
    0.28f to Color(0xFF212442),
    0.58f to Color(0xFF191A23),
    1.0f to Color(0xFF14151B)
)

private val LightBackgroundGradient = Brush.verticalGradient(
    0.0f to Color(0xFFFFFFFF), // Blanco puro
    0.3f to Color(0xFFF8FAFF), // Azul muy muy claro
    0.7f to Color(0xFFEAEFFF), // Azul/violeta muy tenue
    1.0f to Color(0xFFDAD6FF)  // Un toque de violeta suave
)

@Composable
fun ModernTorneoYaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (useDarkTheme) DarkColorScheme else WhiteColorScheme

    // Actualizamos el gradiente global en función del tema
    TorneoYaPalette.currentBackgroundGradient =
        if (useDarkTheme) DarkBackgroundGradient else LightBackgroundGradient

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(
            displayLarge = MaterialTheme.typography.displayLarge.copy(letterSpacing = 0.5.sp),
            titleLarge = MaterialTheme.typography.titleLarge.copy(letterSpacing = 0.15.sp),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp),
            labelLarge = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.2.sp)
        ),
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(19.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ),
        content = content
    )
}

// Paleta auxiliar con gradiente dinámico
object TorneoYaPalette {
    val blue get() = Blue
    val violet get() = Violet
    val accent get() = Accent
    val yellow get() = Yellow
    val chipBgDark get() = ChipBgDark
    val chipBgLight get() = ChipBgLight
    val textLight get() = TextLight
    val mutedText get() = MutedText
    val lightBottomBarColor = Color(0xFFDAD6FF)
    val darkBottomBarColor = Color(0xFF14151B)

    // Se actualiza en ModernTorneoYaTheme()
    internal var currentBackgroundGradient: Brush = DarkBackgroundGradient
    val backgroundGradient get() = currentBackgroundGradient
}

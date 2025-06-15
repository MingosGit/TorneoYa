package mingosgit.josecr.torneoya.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import kotlin.text.Typography

private val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Color.White,
    secondary = AzulClaro,
    background = GrisClaro,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro
)

@Composable
fun TorneoYaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        shapes = Shapes,
        content = content
    )
}

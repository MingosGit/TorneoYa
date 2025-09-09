package mingosgit.josecr.torneoya.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText

/**
 * Barra inferior de navegación. Muestra los items y navega a su route al pulsar.
 * - Cambia colores/estilos según item seleccionado.
 * - Conserva estado al volver (save/restore).
 */
@Composable
fun BottomNavigationBar(
    navController: NavController, // Controlador de navegación para realizar navigate()
    isDarkTheme: Boolean,         // Flag para elegir paleta clara/oscura de la bottom bar
    modifier: Modifier = Modifier // Modifier externo para personalización desde el caller
) {
    // Lista de pestañas a mostrar en la barra
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Online,
        BottomNavItem.Amigos,
        BottomNavItem.Usuario
    )

    // Color de fondo de la bottom bar según tema
    val bottomBarColor = if (!isDarkTheme) {
        TorneoYaPalette.lightBottomBarColor
    } else {
        TorneoYaPalette.darkBottomBarColor
    }

    // Route actual para saber qué item está seleccionado
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Contenedor base con color/elevación de la barra
    Surface(
        color = bottomBarColor,
        tonalElevation = 3.dp,
        shadowElevation = 12.dp,
        modifier = modifier
    ) {
        // Fila que reparte los items a lo ancho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(bottomBarColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Por cada item de la barra...
            items.forEach { item ->
                val selected = currentRoute == item.route
                // Animación del color entre seleccionado/no seleccionado
                val color by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.mutedText,
                    label = "nav-item"
                )

                // Columna clicable que contiene icono, texto e indicador
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)                 // Cada item ocupa el mismo ancho
                        .fillMaxHeight()
                        .clickable {
                            // Navega solo si no está ya seleccionado, con restore del estado
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true } // Guarda estado del destino raíz
                                    launchSingleTop = true  // Evita duplicar destino en el back stack
                                    restoreState = true     // Restaura estado si existía
                                }
                            }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    // Icono del item
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.titleResId),
                        tint = color,
                        modifier = Modifier.size(26.dp)
                    )
                    // Texto del item (cambia tamaño/estilo si está seleccionado)
                    Text(
                        text = stringResource(item.titleResId),
                        color = color,
                        fontSize = if (selected) 13.sp else 12.sp,
                        style = if (selected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    // Box: indicador inferior (barra) visible solo si está seleccionado
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .height(if (selected) 3.dp else 0.dp)
                            .width(32.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                    )
                }
            }
        }
    }
}

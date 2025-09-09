package mingosgit.josecr.torneoya.ui.screens.cuentaLocal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
// Pantalla principal: fondo con gradiente y columna con cuatro botones de navegación
fun CuentaLocalScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Fondo: gradiente vertical suave entre primaryContainer y background
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de la sección
        Text(
            text = "Gestión Local",
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 40.dp, top = 18.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Botón: navegar a creación/gestión de partidos locales
        LocalMenuButtonClean(
            title = "Partidos Locales",
            icon = Icons.Filled.SportsSoccer,
            description = "Juega, crea y gestiona tus partidos locales.",
            borderColor = Color(0xFF1976D2),
            onClick = { navController.navigate("partido") }
        )
        Spacer(Modifier.height(20.dp))
        // Botón: navegar a lista de jugadores personales
        LocalMenuButtonClean(
            title = "Mis Jugadores",
            icon = Icons.Filled.Person,
            description = "Administra tus jugadores personales.",
            borderColor = Color(0xFF388E3C),
            onClick = { navController.navigate("mis_jugadores") }
        )
        Spacer(Modifier.height(20.dp))
        // Botón: navegar a gestión de equipos predefinidos
        LocalMenuButtonClean(
            title = "Equipos Predefinidos",
            icon = Icons.Filled.Groups,
            description = "Crea y edita equipos para tus partidos.",
            borderColor = Color(0xFFF9A825),
            onClick = { navController.navigate("equipos_predefinidos") }
        )
        Spacer(Modifier.height(20.dp))
        // Botón: navegar a administración/consulta de partidos creados
        LocalMenuButtonClean(
            title = "Administrar Partidos",
            icon = Icons.Filled.ListAlt,
            description = "Consulta y administra todos tus partidos.",
            borderColor = Color(0xFFD84315),
            onClick = { navController.navigate("administrar_partidos") }
        )
    }
}

@Composable
// Botón de menú sin relleno: icono + textos, borde coloreado, ejecuta onClick
fun LocalMenuButtonClean(
    title: String,
    icon: ImageVector,
    description: String,
    borderColor: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick, // Acción del botón (navegación)
        shape = RoundedCornerShape(18.dp), // Esquinas redondeadas del botón
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        border = BorderStroke(2.dp, borderColor), // Borde con color temático
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent, // Sin fondo
            contentColor = borderColor // Color de contenido por defecto
        ),
        // Sin sombra ni fondo ni degradados
    ) {
        // Fila interna: icono a la izquierda y textos a la derecha
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del botón
            Icon(
                icon,
                contentDescription = title,
                tint = borderColor,
                modifier = Modifier
                    .size(34.dp)
                    .padding(end = 16.dp)
            )
            // Columna con título y descripción
            Column(
                Modifier.weight(1f)
            ) {
                // Título del botón
                Text(
                    title,
                    fontSize = 19.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = borderColor
                )
                // Descripción corta del destino/acción
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = borderColor.copy(alpha = 0.82f),
                    maxLines = 2
                )
            }
        }
    }
}

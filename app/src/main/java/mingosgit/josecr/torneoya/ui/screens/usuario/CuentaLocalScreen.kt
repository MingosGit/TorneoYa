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
fun CuentaLocalScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Gestión Local",
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 40.dp, top = 18.dp)
                .align(Alignment.CenterHorizontally)
        )

        LocalMenuButtonClean(
            title = "Partidos Locales",
            icon = Icons.Filled.SportsSoccer,
            description = "Juega, crea y gestiona tus partidos locales.",
            borderColor = Color(0xFF1976D2),
            onClick = { navController.navigate("partido") }
        )
        Spacer(Modifier.height(20.dp))
        LocalMenuButtonClean(
            title = "Mis Jugadores",
            icon = Icons.Filled.Person,
            description = "Administra tus jugadores personales.",
            borderColor = Color(0xFF388E3C),
            onClick = { navController.navigate("mis_jugadores") }
        )
        Spacer(Modifier.height(20.dp))
        LocalMenuButtonClean(
            title = "Equipos Predefinidos",
            icon = Icons.Filled.Groups,
            description = "Crea y edita equipos para tus partidos.",
            borderColor = Color(0xFFF9A825),
            onClick = { navController.navigate("equipos_predefinidos") }
        )
        Spacer(Modifier.height(20.dp))
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
fun LocalMenuButtonClean(
    title: String,
    icon: ImageVector,
    description: String,
    borderColor: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = borderColor
        ),
        // Sin sombra ni fondo ni degradados
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = borderColor,
                modifier = Modifier
                    .size(34.dp)
                    .padding(end = 16.dp)
            )
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontSize = 19.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = borderColor
                )
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

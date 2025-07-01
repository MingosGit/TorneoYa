package mingosgit.josecr.torneoya.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FFFD), Color(0xFFE9F2FF), Color(0xFFD3E2FF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Bienvenida",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "¡Hola,",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF2E4A5A)
                    )
                    Text(
                        text = uiState.nombreUsuario + "!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Resumen general",
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HomeStatCard(
                    icon = Icons.Filled.SportsSoccer,
                    label = "Partidos",
                    value = uiState.partidosTotales.toString(),
                    color = Color(0xFF02C39A),
                    background = Brush.horizontalGradient(listOf(Color(0xFFCBF3F0), Color(0xFFD7FFF1)))
                )
                HomeStatCard(
                    icon = Icons.Filled.Group,
                    label = "Equipos",
                    value = uiState.equiposTotales.toString(),
                    color = Color(0xFF4361EE),
                    background = Brush.horizontalGradient(listOf(Color(0xFFE0E8FF), Color(0xFFD4F1F9)))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                HomeStatCard(
                    icon = Icons.Filled.Person,
                    label = "Jugadores",
                    value = uiState.jugadoresTotales.toString(),
                    color = Color(0xFFFFB300),
                    background = Brush.horizontalGradient(listOf(Color(0xFFFFE29A), Color(0xFFFFF6E0)))
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn()
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(22.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEEF7FE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(26.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Listo para disfrutar el torneo!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Consulta tus partidos, equipos y jugadores usando el menú inferior.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF3C4858)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    background: Brush
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .padding(end = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = background, shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier
                            .size(38.dp)
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = value,
                        fontSize = 22.sp,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

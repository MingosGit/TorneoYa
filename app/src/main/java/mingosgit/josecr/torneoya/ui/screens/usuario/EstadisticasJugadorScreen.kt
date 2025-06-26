package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.database.AppDatabase
import mingosgit.josecr.torneoya.repository.EstadisticasRepository
import mingosgit.josecr.torneoya.data.entities.EstadisticasJugador

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasJugadorScreen(
    navController: NavController,
    jugadorId: Long
) {
    val context = LocalContext.current
    val estadisticasDao = remember { AppDatabase.getInstance(context).estadisticasDao() }
    val estadisticasRepo = remember { EstadisticasRepository(estadisticasDao) }
    var estadisticas by remember { mutableStateOf<EstadisticasJugador?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(jugadorId) {
        scope.launch {
            estadisticas = estadisticasRepo.getEstadisticasJugador(jugadorId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas del Jugador") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color(0xFF90CAF9))
                    )
                )
                .padding(padding)
        ) {
            if (estadisticas == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier
                            .size(70.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = estadisticas?.nombre.orEmpty(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(bottom = 30.dp)
                    )
                    EstadisticaItemStyled("Partidos Jugados: ", estadisticas?.partidosJugados ?: 0)
                    EstadisticaItemStyled("Goles: ", estadisticas?.goles ?: 0)
                    EstadisticaItemStyled("Asistencias: ", estadisticas?.asistencias ?: 0)
                }
            }
        }
    }
}

@Composable
fun EstadisticaItemStyled(label: String, value: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = MaterialTheme.shapes.extraLarge)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.97f)
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 20.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 18.sp, color = Color.Gray)
            Text(
                value.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

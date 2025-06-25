package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.JugadorEntity
import mingosgit.josecr.torneoya.repository.JugadorRepository
import mingosgit.josecr.torneoya.data.database.AppDatabase
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisJugadoresScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val jugadorDao = remember { AppDatabase.getInstance(context).jugadorDao() }
    val jugadorRepo = remember { JugadorRepository(jugadorDao) }

    var jugadores by remember { mutableStateOf<List<JugadorEntity>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            jugadores = jugadorRepo.getAll()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Jugadores") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (jugadores.isEmpty()) {
                Text(
                    text = "No tienes jugadores creados aún.",
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                jugadores.forEach { jugador ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(jugador.nombre, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

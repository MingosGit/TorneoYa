package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import mingosgit.josecr.torneoya.viewmodel.usuario.MisJugadoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisJugadoresScreen(
    navController: NavController,
    viewModel: MisJugadoresViewModel
) {
    val jugadores by viewModel.jugadores.collectAsState()
    var searchText by remember { mutableStateOf("") }

    // Filtrado por búsqueda (ignora mayúsculas/minúsculas y espacios)
    val filteredJugadores = jugadores.filter {
        it.nombre.trim().contains(searchText.trim(), ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.cargarJugadores()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Jugadores") },
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Buscador SIEMPRE visible
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
                        .shadow(4.dp, shape = MaterialTheme.shapes.large),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Buscar jugador...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF1976D2)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF1976D2).copy(alpha = 0.4f),
                            focusedBorderColor = Color(0xFF1976D2)
                        )
                    )
                }

                if (jugadores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "No tienes jugadores creados aún.",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredJugadores) { jugador ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, shape = MaterialTheme.shapes.extraLarge)
                                    .clickable {
                                        navController.navigate("estadisticas_jugador/${jugador.id}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.96f)
                                ),
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            jugador.nombre,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1976D2)
                                        )
                                        Text(
                                            "Toca para ver estadísticas",
                                            fontSize = 13.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

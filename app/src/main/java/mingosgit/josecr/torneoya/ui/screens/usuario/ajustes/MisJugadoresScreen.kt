package mingosgit.josecr.torneoya.ui.screens.usuario.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.MisJugadoresViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisJugadoresScreen(
    navController: NavController,
    viewModel: MisJugadoresViewModel
) {
    val jugadores by viewModel.jugadores.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val filteredJugadores = jugadores.filter {
        it.nombre.trim().contains(searchText.trim(), ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.cargarJugadores()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Jugadores", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = TorneoYaPalette.violet
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF14151B),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF1B1D29),
                        0.28f to Color(0xFF212442),
                        0.58f to Color(0xFF191A23),
                        1.0f to Color(0xFF14151B)
                    )
                )
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color(0xFF23273D))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                            shape = RoundedCornerShape(15.dp)
                        )
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Buscar jugador...", color = Color(0xFF8F5CFF)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = TorneoYaPalette.blue
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors( // FIX: usar TextFieldDefaults
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            containerColor = Color.Transparent,
                            cursorColor = TorneoYaPalette.blue,
                        )
                    )
                }

                if (jugadores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "No tienes jugadores creados aún.",
                            fontSize = 18.sp,
                            color = Color(0xFF8F5CFF),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredJugadores) { jugador ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(14.dp, shape = RoundedCornerShape(17.dp))
                                    .clip(RoundedCornerShape(17.dp))
                                    .clickable {
                                        navController.navigate("estadisticas_jugador/${jugador.id}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF23273D)
                                ),
                                shape = RoundedCornerShape(17.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = TorneoYaPalette.blue,
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
                                            color = TorneoYaPalette.blue
                                        )
                                        Text(
                                            "Toca para ver estadísticas",
                                            fontSize = 14.sp,
                                            color = Color(0xFF8F5CFF),
                                            modifier = Modifier.padding(top = 4.dp)
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

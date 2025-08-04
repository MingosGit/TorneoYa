package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AsignarJugadoresOnlineViewModel
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@Composable
fun AsignarJugadoresOnlineScreen(
    navController: NavController,
    vm: AsignarJugadoresOnlineViewModel
) {
    LaunchedEffect(Unit) {
        vm.cargarJugadoresExistentes()
    }

    val miUid = FirebaseAuth.getInstance().currentUser?.uid

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = modernBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            // HEADER
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF22243A),
                    shadowElevation = 14.dp,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Asignar jugadores",
                        tint = Color(0xFF8F5CFF),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Asignar jugadores",
                    fontSize = 23.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(12.dp))

            // SELECCIÓN DE EQUIPO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EquipoGradientButton(
                    text = "Equipo A",
                    selected = vm.equipoSeleccionado == "A",
                    color = TorneoYaPalette.blue,
                    onClick = { vm.equipoSeleccionado = "A" },
                    modifier = Modifier.weight(1f)
                )
                EquipoGradientButton(
                    text = "Equipo B",
                    selected = vm.equipoSeleccionado == "B",
                    color = Color(0xFFFF7675),
                    onClick = { vm.equipoSeleccionado = "B" },
                    modifier = Modifier.weight(1f)
                )
            }

            // MODO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EquipoGradientButton(
                    text = "Manual",
                    selected = !vm.modoAleatorio,
                    color = TorneoYaPalette.violet,
                    onClick = { vm.cambiarModo(false) },
                    modifier = Modifier.weight(1f)
                )
                EquipoGradientButton(
                    text = "Aleatorio",
                    selected = vm.modoAleatorio,
                    color = TorneoYaPalette.blue,
                    onClick = { vm.cambiarModo(true) },
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(
                visible = !vm.modoAleatorio,
                enter = fadeIn(), exit = fadeOut()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        if (vm.equipoSeleccionado == "A") "Jugadores Equipo A" else "Jugadores Equipo B",
                        fontSize = 17.sp,
                        color = Color(0xFFB7B7D1),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp, bottom = 7.dp)
                    )
                    val jugadores =
                        if (vm.equipoSeleccionado == "A") vm.equipoAJugadores else vm.equipoBJugadores

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF22243A).copy(alpha = 0.62f))
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .padding(12.dp)
                                .heightIn(min = 100.dp, max = 350.dp)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(jugadores + listOf(JugadorFirebase())) { idx, value ->
                                var expanded by remember { mutableStateOf(false) }
                                var searchQuery by remember { mutableStateOf("") }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                ) {
                                    OutlinedTextField(
                                        value = value.nombre,
                                        onValueChange = { newValue ->
                                            if (idx == jugadores.size) {
                                                if (newValue.isNotBlank()) {
                                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(JugadorFirebase(nombre = newValue))
                                                    else vm.equipoBJugadores.add(JugadorFirebase(nombre = newValue))
                                                }
                                            } else {
                                                if (newValue.isEmpty()) {
                                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.removeAt(idx)
                                                    else vm.equipoBJugadores.removeAt(idx)
                                                } else {
                                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores[idx] = JugadorFirebase(nombre = newValue)
                                                    else vm.equipoBJugadores[idx] = JugadorFirebase(nombre = newValue)
                                                }
                                            }
                                        },
                                        label = { Text(if (idx == jugadores.size) "Agregar jugador" else "Jugador ${idx + 1}") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1C1D25))
                                    )
                                    IconButton(
                                        onClick = { expanded = true }
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir jugador", tint = TorneoYaPalette.violet)
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            label = { Text("Buscar") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                        vm.jugadoresDisponiblesManual(vm.equipoSeleccionado, idx)
                                            .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                                            .forEach { jugador ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(jugador.nombre)
                                                            if (miUid != null && jugador.uid == miUid) {
                                                                Text(" /Tú", color = TorneoYaPalette.blue, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        if (idx == jugadores.size) {
                                                            if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(jugador)
                                                            else vm.equipoBJugadores.add(jugador)
                                                        } else {
                                                            if (vm.equipoSeleccionado == "A") vm.equipoAJugadores[idx] = jugador
                                                            else vm.equipoBJugadores[idx] = jugador
                                                        }
                                                        expanded = false
                                                        searchQuery = ""
                                                    }
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = vm.modoAleatorio,
                enter = fadeIn(), exit = fadeOut()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "Pon todos los jugadores, se asignarán aleatoriamente",
                        fontSize = 16.sp,
                        color = Color(0xFFB7B7D1),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF22243A).copy(alpha = 0.62f))
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .padding(12.dp)
                                .heightIn(min = 100.dp, max = 350.dp)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(vm.listaNombres + listOf(JugadorFirebase())) { idx, value ->
                                var expanded by remember { mutableStateOf(false) }
                                var searchQuery by remember { mutableStateOf("") }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                ) {
                                    OutlinedTextField(
                                        value = value.nombre,
                                        onValueChange = { newValue ->
                                            if (idx == vm.listaNombres.size) {
                                                if (newValue.isNotBlank()) {
                                                    vm.listaNombres.add(JugadorFirebase(nombre = newValue))
                                                }
                                            } else {
                                                if (newValue.isEmpty()) {
                                                    vm.listaNombres.removeAt(idx)
                                                } else {
                                                    vm.listaNombres[idx] = JugadorFirebase(nombre = newValue)
                                                }
                                            }
                                        },
                                        label = { Text(if (idx == vm.listaNombres.size) "Agregar jugador" else "Jugador ${idx + 1}") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1C1D25))
                                    )
                                    IconButton(
                                        onClick = { expanded = true }
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir jugador", tint = TorneoYaPalette.violet)
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            label = { Text("Buscar") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                        vm.jugadoresDisponiblesAleatorio(idx)
                                            .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                                            .forEach { jugador ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(jugador.nombre)
                                                            if (miUid != null && jugador.uid == miUid) {
                                                                Text(" /Tú", color = TorneoYaPalette.blue, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        if (idx == vm.listaNombres.size) {
                                                            vm.listaNombres.add(jugador)
                                                        } else {
                                                            vm.listaNombres[idx] = jugador
                                                        }
                                                        expanded = false
                                                        searchQuery = ""
                                                    }
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(19.dp))

            // BOTÓN GUARDAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                        )
                    )
                    .clickable {
                        if (vm.modoAleatorio) {
                            val jugadoresLimpios = vm.listaNombres.filter { it.nombre.isNotBlank() }
                            if (jugadoresLimpios.size >= 2) {
                                vm.repartirAleatoriamente(jugadoresLimpios)
                                vm.cambiarModo(false)
                            }
                        }
                        vm.guardarEnBD {
                            navController.navigate("partido_online") {
                                popUpTo("partido_online") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                    .height(51.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Guardar asignación",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun EquipoGradientButton(
    text: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    if (selected)
                        listOf(color, TorneoYaPalette.violet)
                    else
                        listOf(Color(0xFF353659), Color(0xFF353659))
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .background(
                Brush.horizontalGradient(
                    if (selected)
                        listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                    else
                        listOf(Color(0xFF181921), Color(0xFF191A23))
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) color else Color(0xFFB7B7D1),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AsignarJugadoresOnlineViewModel
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.ui.theme.text

@Composable
fun AsignarJugadoresOnlineScreen(
    navController: NavController,
    vm: AsignarJugadoresOnlineViewModel
) {
    LaunchedEffect(Unit) {
        vm.cargarJugadoresExistentes()
    }

    val miUid = FirebaseAuth.getInstance().currentUser?.uid
    val cs = MaterialTheme.colorScheme
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val gradientError = Brush.horizontalGradient(listOf(cs.error, cs.secondary))
    val listBg = cs.surface.copy(alpha = 0.62f)
    val fieldBg = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = TorneoYaPalette.backgroundGradient)
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
                    color = cs.surface,
                    shadowElevation = 14.dp,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = stringResource(id = R.string.asignjug_title),
                        tint = cs.secondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(id = R.string.asignjug_title),
                    fontSize = 23.sp,
                    color = cs.text,
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
                    text = stringResource(id = R.string.asignjug_equipo_a),
                    selected = vm.equipoSeleccionado == "A",
                    color = cs.primary,
                    onClick = { vm.equipoSeleccionado = "A" },
                    modifier = Modifier.weight(1f)
                )
                EquipoGradientButton(
                    text = stringResource(id = R.string.asignjug_equipo_b),
                    selected = vm.equipoSeleccionado == "B",
                    color = cs.error,
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
                    text = stringResource(id = R.string.asignjug_modo_manual),
                    selected = !vm.modoAleatorio,
                    color = cs.secondary,
                    onClick = { vm.cambiarModo(false) },
                    modifier = Modifier.weight(1f)
                )
                EquipoGradientButton(
                    text = stringResource(id = R.string.asignjug_modo_aleatorio),
                    selected = vm.modoAleatorio,
                    color = cs.primary,
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
                        if (vm.equipoSeleccionado == "A") stringResource(id = R.string.asignjug_jugadores_equipo_a) else stringResource(
                            id = R.string.asignjug_jugadores_equipo_b
                        ),
                        fontSize = 17.sp,
                        color = cs.mutedText,
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
                            .background(listBg)
                            .border(
                                width = 2.dp,
                                brush = gradientPrimary,
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
                                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(
                                                        JugadorFirebase(nombre = newValue)
                                                    )
                                                    else vm.equipoBJugadores.add(JugadorFirebase(nombre = newValue))
                                                }
                                            } else {
                                                if (newValue.isEmpty()) {
                                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.removeAt(
                                                        idx
                                                    )
                                                    else vm.equipoBJugadores.removeAt(idx)
                                                } else {
                                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores[idx] =
                                                        JugadorFirebase(nombre = newValue)
                                                    else vm.equipoBJugadores[idx] =
                                                        JugadorFirebase(nombre = newValue)
                                                }
                                            }
                                        },
                                        label = {
                                            Text(
                                                if (idx == jugadores.size)
                                                    stringResource(id = R.string.asignjug_agregar_jugador)
                                                else
                                                    stringResource(
                                                        id = R.string.asignjug_jugador_num,
                                                        idx + 1
                                                    ),
                                                color = cs.mutedText
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            disabledBorderColor = Color.Transparent,
                                            errorBorderColor = Color.Transparent,
                                            focusedLabelColor = cs.primary,
                                            errorLabelColor = cs.error,
                                            cursorColor = cs.primary,
                                            focusedTextColor = cs.text,
                                            unfocusedTextColor = cs.text,
                                            errorTextColor = cs.text,
                                            disabledTextColor = cs.mutedText
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(fieldBg)
                                    )
                                    IconButton(
                                        onClick = { expanded = true }
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = stringResource(id = R.string.asignjug_elegir_jugador),
                                            tint = cs.secondary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            label = { Text(stringResource(id = R.string.gen_buscar)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedBorderColor = Color.Transparent,
                                                disabledBorderColor = Color.Transparent,
                                                errorBorderColor = Color.Transparent,
                                                focusedLabelColor = cs.primary,
                                                errorLabelColor = cs.error,
                                                cursorColor = cs.primary,
                                                focusedTextColor = cs.text,
                                                unfocusedTextColor = cs.text,
                                                errorTextColor = cs.text,
                                                disabledTextColor = cs.mutedText
                                            ),
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
                                                            Text(jugador.nombre, color = cs.text)
                                                            if (miUid != null && jugador.uid == miUid) {
                                                                Text(
                                                                    stringResource(id = R.string.asignjug_tu),
                                                                    color = cs.primary,
                                                                    fontSize = 14.sp,
                                                                    modifier = Modifier.padding(start = 4.dp)
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        if (idx == jugadores.size) {
                                                            if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(
                                                                jugador
                                                            )
                                                            else vm.equipoBJugadores.add(jugador)
                                                        } else {
                                                            if (vm.equipoSeleccionado == "A") vm.equipoAJugadores[idx] =
                                                                jugador
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
                        stringResource(id = R.string.asignjug_poner_todos_aleatorio),
                        fontSize = 16.sp,
                        color = cs.mutedText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(listBg)
                            .border(
                                width = 2.dp,
                                brush = gradientPrimary,
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
                                        label = {
                                            Text(
                                                if (idx == vm.listaNombres.size)
                                                    stringResource(id = R.string.asignjug_agregar_jugador)
                                                else
                                                    stringResource(id = R.string.asignjug_jugador_num, idx + 1)
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent,
                                            disabledBorderColor = Color.Transparent,
                                            errorBorderColor = Color.Transparent,
                                            focusedLabelColor = cs.primary,
                                            errorLabelColor = cs.error,
                                            cursorColor = cs.primary,
                                            focusedTextColor = cs.text,
                                            unfocusedTextColor = cs.text,
                                            errorTextColor = cs.text,
                                            disabledTextColor = cs.mutedText
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(fieldBg)
                                    )
                                    IconButton(
                                        onClick = { expanded = true }
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = stringResource(id = R.string.asignjug_elegir_jugador),
                                            tint = cs.secondary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            label = { Text(stringResource(id = R.string.gen_buscar)) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedBorderColor = Color.Transparent,
                                                disabledBorderColor = Color.Transparent,
                                                errorBorderColor = Color.Transparent,
                                                focusedLabelColor = cs.primary,
                                                errorLabelColor = cs.error,
                                                cursorColor = cs.primary,
                                                focusedTextColor = cs.text,
                                                unfocusedTextColor = cs.text,
                                                errorTextColor = cs.text,
                                                disabledTextColor = cs.mutedText
                                            ),
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
                                                            Text(jugador.nombre, color = cs.text)
                                                            if (miUid != null && jugador.uid == miUid) {
                                                                Text(
                                                                    stringResource(id = R.string.asignjug_tu),
                                                                    color = cs.primary,
                                                                    fontSize = 14.sp,
                                                                    modifier = Modifier.padding(start = 4.dp)
                                                                )
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
                        brush = gradientPrimary,
                        shape = RoundedCornerShape(15.dp)
                    )
                    .background(
                        Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))
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
                    stringResource(id = R.string.asignjug_guardar_asignacion),
                    color = cs.text,
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
    val cs = MaterialTheme.colorScheme
    val borderBrush = if (selected)
        Brush.horizontalGradient(listOf(color, cs.secondary))
    else
        Brush.horizontalGradient(listOf(cs.outline, cs.outline))
    val bgBrush = if (selected)
        Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))
    else
        Brush.horizontalGradient(listOf(cs.background, cs.surfaceVariant))

    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(13.dp)
            )
            .background(bgBrush)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) color else cs.mutedText,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

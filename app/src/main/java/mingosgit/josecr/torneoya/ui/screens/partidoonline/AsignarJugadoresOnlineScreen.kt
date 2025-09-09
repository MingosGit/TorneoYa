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
// Pantalla para asignar jugadores a equipos (modo manual o aleatorio) y guardar en BD.
fun AsignarJugadoresOnlineScreen(
    navController: NavController,
    vm: AsignarJugadoresOnlineViewModel
) {
    // Al entrar, carga la lista de jugadores existentes para usar en los desplegables.
    LaunchedEffect(Unit) {
        vm.cargarJugadoresExistentes()
    }

    // UID del usuario logueado (para marcar "tú" en los menús).
    val miUid = FirebaseAuth.getInstance().currentUser?.uid
    // Colores y pinceles de la UI.
    val cs = MaterialTheme.colorScheme
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val listBg = cs.surface.copy(alpha = 0.62f)
    val fieldBg = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    // Contenedor raíz con fondo degradado de la app.
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
            // HEADER: icono + título de pantalla.
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

            // SELECCIÓN DE EQUIPO: alterna entre equipo A y B para editar su lista.
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

            // SELECCIÓN DE MODO: manual (editas listas por equipo) o aleatorio (lista única para repartir).
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

            // BLOQUE MODO MANUAL: edición directa de jugadores del equipo seleccionado.
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
                    // Lista de jugadores del equipo activo (A o B).
                    val jugadores =
                        if (vm.equipoSeleccionado == "A") vm.equipoAJugadores else vm.equipoBJugadores

                    // Caja con borde y fondo para la lista editable.
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
                        // Lista con filas editables + una fila extra para "agregar".
                        LazyColumn(
                            modifier = Modifier
                                .padding(12.dp)
                                .heightIn(min = 100.dp, max = 350.dp)
                                .fillMaxWidth()
                        ) {
                            // itemsIndexed: recorre jugadores y añade una fila vacía final.
                            itemsIndexed(jugadores + listOf(JugadorFirebase())) { idx, value ->
                                var expanded by remember { mutableStateOf(false) } // estado menú desplegable
                                var searchQuery by remember { mutableStateOf("") }  // búsqueda en el menú
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                ) {
                                    // Campo de texto: edita nombre del jugador o crea/elimina según estado.
                                    OutlinedTextField(
                                        value = value.nombre,
                                        onValueChange = { newValue ->
                                            if (idx == jugadores.size) {
                                                // Fila "agregar": si escribes algo, inserta nuevo jugador.
                                                if (newValue.isNotBlank()) {
                                                    if (vm.equipoSeleccionado == "A") vm.equipoAJugadores.add(
                                                        JugadorFirebase(nombre = newValue)
                                                    )
                                                    else vm.equipoBJugadores.add(JugadorFirebase(nombre = newValue))
                                                }
                                            } else {
                                                // Fila existente: vacío => borra; otro texto => actualiza.
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
                                            // Etiqueta: "Agregar jugador" en la fila extra; si no, "Jugador #".
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
                                    // Botón para abrir el menú de selección desde jugadores existentes.
                                    IconButton(
                                        onClick = { expanded = true }
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = stringResource(id = R.string.asignjug_elegir_jugador),
                                            tint = cs.secondary
                                        )
                                    }
                                    // Menú desplegable con buscador y opciones filtradas.
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        // Cuadro de búsqueda dentro del menú.
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
                                        // Opciones disponibles según equipo/posición y filtro de búsqueda.
                                        vm.jugadoresDisponiblesManual(vm.equipoSeleccionado, idx)
                                            .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                                            .forEach { jugador ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(jugador.nombre, color = cs.text)
                                                            // Marca "tú" si coincide tu UID.
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
                                                        // Sustituye o añade el jugador seleccionado en la fila actual.
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
            // BLOQUE MODO ALEATORIO: introduces una lista común y luego el VM reparte en A/B.
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
                    // Caja con lista editable de nombres para el reparto aleatorio.
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
                            // itemsIndexed: recorre la lista y añade fila extra para "agregar".
                            itemsIndexed(vm.listaNombres + listOf(JugadorFirebase())) { idx, value ->
                                var expanded by remember { mutableStateOf(false) } // estado menú desplegable
                                var searchQuery by remember { mutableStateOf("") }  // filtro de búsqueda
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                ) {
                                    // Campo de texto para editar/añadir/eliminar nombres de la lista común.
                                    OutlinedTextField(
                                        value = value.nombre,
                                        onValueChange = { newValue ->
                                            if (idx == vm.listaNombres.size) {
                                                // Fila "agregar": inserta si no está en blanco.
                                                if (newValue.isNotBlank()) {
                                                    vm.listaNombres.add(JugadorFirebase(nombre = newValue))
                                                }
                                            } else {
                                                // Fila existente: vacío => borra; otro texto => actualiza.
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
                                    // Botón para abrir menú con jugadores existentes y añadirlos rápido.
                                    IconButton(
                                        onClick = { expanded = true }
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = stringResource(id = R.string.asignjug_elegir_jugador),
                                            tint = cs.secondary
                                        )
                                    }
                                    // Menú con buscador y candidatos disponibles (según VM).
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
                                                            // Marca "tú" si el UID coincide con el del usuario.
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
                                                        // Inserta/sustituye el jugador elegido en la lista común.
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

            // BOTÓN GUARDAR: si es aleatorio, reparte y pasa a manual; luego guarda en BD y navega.
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
                                vm.repartirAleatoriamente(jugadoresLimpios) // reparte entre A/B
                                vm.cambiarModo(false) // vuelve a manual para revisión
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
// Botón con gradiente para seleccionar opciones (equipo o modo); muestra estado 'selected'.
fun EquipoGradientButton(
    text: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    // Pinceles de borde y fondo según si está seleccionado.
    val borderBrush = if (selected)
        Brush.horizontalGradient(listOf(color, cs.secondary))
    else
        Brush.horizontalGradient(listOf(cs.outline, cs.outline))
    val bgBrush = if (selected)
        Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))
    else
        Brush.horizontalGradient(listOf(cs.background, cs.surfaceVariant))

    // Caja clicable que actúa como botón estilizado.
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
        // Texto del botón con color/peso según selección.
        Text(
            text,
            color = if (selected) color else cs.mutedText,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

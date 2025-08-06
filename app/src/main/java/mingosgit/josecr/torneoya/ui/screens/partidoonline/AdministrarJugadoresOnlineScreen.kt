package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarJugadoresOnlineViewModel
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@Composable
fun AdministrarJugadoresOnlineScreen(
    navController: NavController,
    vm: AdministrarJugadoresOnlineViewModel
) {
    LaunchedEffect(Unit) { vm.cargarJugadoresExistentes() }
    val miUid = FirebaseAuth.getInstance().currentUser?.uid
    var idxParaEliminar by remember { mutableStateOf<Int?>(null) }

    val equipoANombre by remember { derivedStateOf { vm.equipoANombre } }
    val equipoBNombre by remember { derivedStateOf { vm.equipoBNombre } }

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
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF22243A),
                    shadowElevation = 11.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = stringResource(R.string.adminj_contentdesc_icono),
                        tint = TorneoYaPalette.blue,
                        modifier = Modifier.padding(11.dp)
                    )
                }
                Spacer(Modifier.width(13.dp))
                Text(
                    stringResource(R.string.adminj_title),
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
                    .padding(bottom = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EquipoGradientButton(
                    text = equipoANombre,
                    selected = vm.equipoSeleccionado.value == "A",
                    color = TorneoYaPalette.blue,
                    onClick = { vm.equipoSeleccionado.value = "A" },
                    modifier = Modifier.weight(1f)
                )
                EquipoGradientButton(
                    text = equipoBNombre,
                    selected = vm.equipoSeleccionado.value == "B",
                    color = Color(0xFFFF7675),
                    onClick = { vm.equipoSeleccionado.value = "B" },
                    modifier = Modifier.weight(1f)
                )
            }

            val jugadores = if (vm.equipoSeleccionado.value == "A") vm.equipoAJugadores else vm.equipoBJugadores

            Text(
                text = stringResource(R.string.adminj_texto_jugadores_equipo, if (vm.equipoSeleccionado.value == "A") equipoANombre else equipoBNombre),
                fontSize = 17.sp,
                color = Color(0xFFB7B7D1),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 17.dp, bottom = 7.dp, start = 2.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF22243A).copy(alpha = 0.70f))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp, bottom = 12.dp, start = 10.dp, end = 10.dp)
                ) {
                    itemsIndexed(jugadores + listOf(JugadorFirebase())) { idx, value ->
                        var expanded by remember { mutableStateOf(false) }
                        var searchQuery by remember { mutableStateOf("") }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp, horizontal = 0.dp)
                        ) {
                            OutlinedTextField(
                                value = value.nombre,
                                onValueChange = { newValue ->
                                    if (idx == jugadores.size) {
                                        if (newValue.isNotBlank()) {
                                            vm.agregarJugador(JugadorFirebase(nombre = newValue), vm.equipoSeleccionado.value)
                                        }
                                    } else {
                                        if (newValue.isEmpty()) {
                                            idxParaEliminar = idx
                                        } else {
                                            vm.cambiarNombreJugador(idx, vm.equipoSeleccionado.value, newValue)
                                        }
                                    }
                                },
                                label = {
                                    if (idx == jugadores.size) Text(stringResource(R.string.adminj_label_agregar_jugador))
                                    else Text(stringResource(R.string.adminj_label_jugador, idx + 1))
                                },
                                singleLine = false,
                                minLines = 1,
                                maxLines = 2,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1C1D25))
                                    .heightIn(min = 51.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp
                                ),
                                enabled = true
                            )
                            IconButton(
                                onClick = { expanded = true },
                                modifier = Modifier.size(37.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.adminj_contentdesc_elegir_jugador),
                                    tint = TorneoYaPalette.violet
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text(stringResource(R.string.gen_buscar)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                vm.jugadoresDisponiblesManual(vm.equipoSeleccionado.value, idx)
                                    .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                                    .forEach { jugador ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(jugador.nombre, color = Color.White)
                                                    if (miUid != null && jugador.uid == miUid) {
                                                        Text(
                                                            " /Tú",
                                                            color = TorneoYaPalette.blue,
                                                            fontSize = 13.sp,
                                                            modifier = Modifier.padding(start = 4.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                if (idx == jugadores.size) {
                                                    vm.agregarJugador(jugador, vm.equipoSeleccionado.value)
                                                } else {
                                                    vm.cambiarNombreJugador(idx, vm.equipoSeleccionado.value, jugador.nombre)
                                                }
                                                expanded = false
                                                searchQuery = ""
                                            }
                                        )
                                    }
                            }
                            if (idx != jugadores.size) {
                                IconButton(
                                    onClick = { idxParaEliminar = idx },
                                    modifier = Modifier.size(37.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.adminj_texto_titulo_eliminar),
                                        tint = Color(0xFFF25A6D)
                                    )
                                }
                            }
                        }

                        if (idxParaEliminar == idx) {
                            AlertDialog(
                                onDismissRequest = { idxParaEliminar = null },
                                title = { Text(stringResource(R.string.adminj_texto_titulo_eliminar), color = Color.White) },
                                text = { Text(stringResource(R.string.adminj_texto_mensaje_eliminar), color = Color(0xFFB7B7D1)) },
                                containerColor = Color(0xFF1C1D25),
                                confirmButton = {
                                    TextButton(onClick = {
                                        vm.eliminarJugador(idx, vm.equipoSeleccionado.value)
                                        idxParaEliminar = null
                                    }) { Text(stringResource(R.string.gen_eliminar), color = Color(0xFFF25A6D)) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { idxParaEliminar = null }) {
                                        Text(stringResource(R.string.gen_cancelar), color = TorneoYaPalette.violet)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        vm.guardarEnBD {
                            navController.popBackStack()
                        }
                    }
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.adminj_boton_guardar_volver),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
            Spacer(Modifier.height(17.dp))
        }
    }
}

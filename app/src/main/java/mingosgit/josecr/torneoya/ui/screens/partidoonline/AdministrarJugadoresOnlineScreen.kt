package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.partidoonline.AdministrarJugadoresOnlineViewModel
import mingosgit.josecr.torneoya.data.firebase.JugadorFirebase
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdministrarJugadoresOnlineScreen(
    navController: NavController,
    vm: AdministrarJugadoresOnlineViewModel
) {
    LaunchedEffect(Unit) { vm.cargarJugadoresExistentes() }
    val miUid = FirebaseAuth.getInstance().currentUser?.uid
    var idxParaEliminar by remember { mutableStateOf<Int?>(null) }

    // ASÍ VALE SIEMPRE: usa getValue (importa androidx.compose.runtime.getValue)
    val equipoANombre by remember { derivedStateOf { vm.equipoANombre } }
    val equipoBNombre by remember { derivedStateOf { vm.equipoBNombre } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF181B26), Color(0xFF1C2030))
                )
            )
    ) {
        Text(
            "Administrar jugadores",
            fontSize = 25.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier
                .padding(top = 22.dp, bottom = 10.dp)
                .align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 20.dp)
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EquipoChip(
                text = equipoANombre,
                selected = vm.equipoSeleccionado.value == "A",
                onClick = { vm.equipoSeleccionado.value = "A" }
            )
            Spacer(Modifier.width(12.dp))
            EquipoChip(
                text = equipoBNombre,
                selected = vm.equipoSeleccionado.value == "B",
                onClick = { vm.equipoSeleccionado.value = "B" }
            )
        }

        val jugadores = if (vm.equipoSeleccionado.value == "A") vm.equipoAJugadores else vm.equipoBJugadores

        Text(
            if (vm.equipoSeleccionado.value == "A") "Jugadores $equipoANombre" else "Jugadores $equipoBNombre",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = 22.dp, bottom = 6.dp, start = 24.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                itemsIndexed(jugadores + listOf(JugadorFirebase())) { idx, value ->
                    var expanded by remember { mutableStateOf(false) }
                    var searchQuery by remember { mutableStateOf("") }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 8.dp)
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
                                if (idx == jugadores.size) Text("Agregar nuevo jugador")
                                else Text("Jugador ${idx + 1}")
                            },
                            singleLine = false,
                            minLines = 1,
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 2.dp)
                                .heightIn(min = 54.dp)
                                .defaultMinSize(minHeight = 54.dp),
                            textStyle = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                lineHeight = 22.sp
                            ),
                            enabled = true
                        )
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Elegir jugador",
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                            vm.jugadoresDisponiblesManual(vm.equipoSeleccionado.value, idx)
                                .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                                .forEach { jugador ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(jugador.nombre, color = MaterialTheme.colorScheme.onSurface)
                                                if (miUid != null && jugador.uid == miUid) {
                                                    Text(
                                                        " /Tú",
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.padding(start = 3.dp)
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
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar jugador",
                                    tint = Color(0xFFF25A6D)
                                )
                            }
                        }
                    }

                    if (idxParaEliminar == idx) {
                        AlertDialog(
                            onDismissRequest = { idxParaEliminar = null },
                            title = { Text("¿Eliminar jugador?") },
                            text = { Text("¿Estás seguro de que quieres eliminar a este jugador?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    vm.eliminarJugador(idx, vm.equipoSeleccionado.value)
                                    idxParaEliminar = null
                                }) { Text("Eliminar", color = Color(0xFFF25A6D)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { idxParaEliminar = null }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                vm.guardarEnBD {
                    navController.popBackStack()
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text("Guardar y volver", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
fun EquipoChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = background,
        shadowElevation = if (selected) 5.dp else 0.dp,
        modifier = Modifier
            .defaultMinSize(minWidth = 0.dp)
            .height(36.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickableNoRipple { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            Text(
                text,
                color = content,
                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    composed {
        this.then(Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { onClick() })
        })
    }

package mingosgit.josecr.torneoya.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.EditPartidoViewModel
import kotlinx.coroutines.Dispatchers
import java.util.*

@Composable
fun EditPartidoScreen(
    partidoId: Long,
    navController: NavController,
    editPartidoViewModel: EditPartidoViewModel
) {
    val context = LocalContext.current

    val partido by editPartidoViewModel.partido.collectAsStateWithLifecycle()
    val loading by editPartidoViewModel.loading.collectAsStateWithLifecycle()
    val eliminado by editPartidoViewModel.eliminado.collectAsStateWithLifecycle()
    val guardado by editPartidoViewModel.guardado.collectAsStateWithLifecycle()
    val jugadoresEquipoA by editPartidoViewModel.jugadoresEquipoA.collectAsStateWithLifecycle()
    val jugadoresEquipoB by editPartidoViewModel.jugadoresEquipoB.collectAsStateWithLifecycle()
    val jugadoresCargados by editPartidoViewModel.jugadoresCargados.collectAsStateWithLifecycle()

    var fecha by rememberSaveable { mutableStateOf("") }
    var horaInicio by rememberSaveable { mutableStateOf("") }
    var numeroPartes by rememberSaveable { mutableStateOf("") }
    var tiempoPorParte by rememberSaveable { mutableStateOf("") }
    var camposError by rememberSaveable { mutableStateOf(mapOf<String, Boolean>()) }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }
    var errorGeneral by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    var equipoAEditando by rememberSaveable { mutableStateOf(false) }
    var equipoBEditando by rememberSaveable { mutableStateOf(false) }
    var equipoANombre by rememberSaveable { mutableStateOf("") }
    var equipoBNombre by rememberSaveable { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }
    val scope = rememberCoroutineScope()

    // ---- Cargar nombre de equipos al iniciar
    LaunchedEffect(partido) {
        partido?.let {
            fecha = it.fecha
            horaInicio = it.horaInicio
            numeroPartes = it.numeroPartes.toString()
            tiempoPorParte = it.tiempoPorParte.toString()

            // Cargar nombres usando ViewModel (se recomienda un flow, pero para este caso es suficiente así)
            equipoANombre = editPartidoViewModel.getEquipoNombre(it.equipoAId) ?: ""
            equipoBNombre = editPartidoViewModel.getEquipoNombre(it.equipoBId) ?: ""
        }
    }

    // --- NUEVO: Al eliminar o guardar, navega SIEMPRE a "partido"
    LaunchedEffect(eliminado, guardado) {
        if (eliminado || guardado) {
            navController.navigate("partido") {
                popUpTo("partido") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fecha = "%02d-%02d-%04d".format(dayOfMonth, month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                horaInicio = "%02d:%02d".format(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    fun validarCampos(): Boolean {
        val errores = mutableMapOf<String, Boolean>()
        errores["fecha"] = fecha.isBlank()
        errores["horaInicio"] = horaInicio.isBlank()
        errores["numeroPartes"] = numeroPartes.isBlank() || numeroPartes.toIntOrNull() == null
        errores["tiempoPorParte"] = tiempoPorParte.isBlank() || tiempoPorParte.toIntOrNull() == null
        camposError = errores
        return !errores.values.any { it }
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (partido == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontró el partido.", color = Color.Red)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Editar Partido", fontSize = 28.sp, modifier = Modifier.padding(bottom = 24.dp))

            // ---- CAMPO EDITABLE DEL NOMBRE DEL EQUIPO A ----
            OutlinedTextField(
                value = equipoANombre,
                onValueChange = { equipoANombre = it },
                label = { Text("Equipo A") },
                singleLine = true,
                enabled = equipoAEditando,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (equipoAEditando) {
                            scope.launch {
                                val exito = editPartidoViewModel.actualizarEquipoNombre(partido!!.equipoAId, equipoANombre)
                                if (!exito) errorGeneral = "No se pudo actualizar el nombre del equipo A"
                            }
                        }
                        equipoAEditando = !equipoAEditando
                    }) {
                        Icon(
                            imageVector = if (equipoAEditando) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (equipoAEditando) "Guardar" else "Editar"
                        )
                    }
                }
            )

            // ---- CAMPO EDITABLE DEL NOMBRE DEL EQUIPO B ----
            OutlinedTextField(
                value = equipoBNombre,
                onValueChange = { equipoBNombre = it },
                label = { Text("Equipo B") },
                singleLine = true,
                enabled = equipoBEditando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        if (equipoBEditando) {
                            scope.launch {
                                val exito = editPartidoViewModel.actualizarEquipoNombre(partido!!.equipoBId, equipoBNombre)
                                if (!exito) errorGeneral = "No se pudo actualizar el nombre del equipo B"
                            }
                        }
                        equipoBEditando = !equipoBEditando
                    }) {
                        Icon(
                            imageVector = if (equipoBEditando) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (equipoBEditando) "Guardar" else "Editar"
                        )
                    }
                }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (mostrarErrores && camposError["fecha"] == true) Color(0xFFFFCDD2) else Color.Transparent
                        )
                ) {
                    Text(if (fecha.isBlank()) "Seleccionar fecha" else fecha)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (mostrarErrores && camposError["horaInicio"] == true) Color(
                                0xFFFFCDD2
                            ) else Color.Transparent
                        )
                ) {
                    Text(if (horaInicio.isBlank()) "Seleccionar hora" else horaInicio)
                }
            }
            if (mostrarErrores && (camposError["fecha"] == true || camposError["horaInicio"] == true)) {
                Row(Modifier.fillMaxWidth()) {
                    if (camposError["fecha"] == true)
                        Text(
                            "Falta la fecha",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["horaInicio"] == true)
                        Text(
                            "Falta la hora",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                }
            }

            OutlinedTextField(
                value = numeroPartes,
                onValueChange = { numeroPartes = it.filter { c -> c.isDigit() } },
                label = { Text("Número de partes") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mostrarErrores && camposError["numeroPartes"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        if (mostrarErrores && camposError["numeroPartes"] == true) Color(0xFFFFCDD2) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["numeroPartes"] == true) {
                Text("Campo obligatorio o inválido", color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = tiempoPorParte,
                onValueChange = { tiempoPorParte = it.filter { c -> c.isDigit() } },
                label = { Text("Minutos por parte") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mostrarErrores && camposError["tiempoPorParte"] == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        if (mostrarErrores && camposError["tiempoPorParte"] == true) Color(
                            0xFFFFCDD2
                        ) else Color.Transparent
                    )
            )
            if (mostrarErrores && camposError["tiempoPorParte"] == true) {
                Text("Campo obligatorio o inválido", color = Color.Red, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = partido?.numeroJugadores?.toString() ?: "",
                onValueChange = {},
                enabled = false,
                label = { Text("Nº jugadores por equipo") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            if (errorGeneral != null) {
                Text(errorGeneral ?: "", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    mostrarErrores = true
                    if (validarCampos()) {
                        editPartidoViewModel.actualizarPartido(
                            fecha,
                            horaInicio,
                            numeroPartes.toInt(),
                            tiempoPorParte.toInt()
                        )
                    } else {
                        errorGeneral = "Revisa los campos obligatorios."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Guardar")
            }
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Cancelar")
            }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Eliminar Partido")
            }

            Spacer(modifier = Modifier.height(32.dp))

        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            editPartidoViewModel.eliminarPartido()
                        }
                    ) { Text("Eliminar", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                },
                title = { Text("Eliminar Partido") },
                text = { Text("¿Seguro que deseas eliminar este partido? Esta acción no se puede deshacer.") }
            )
        }
    }
}

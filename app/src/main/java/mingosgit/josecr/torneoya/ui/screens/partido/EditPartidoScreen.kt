package mingosgit.josecr.torneoya.ui.screens.partido

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.ui.screens.partido.editpartidoscreen.EditPartidoDeleteDialog
import mingosgit.josecr.torneoya.ui.screens.partido.editpartidoscreen.EditPartidoForm
import mingosgit.josecr.torneoya.viewmodel.partido.EditPartidoViewModel

@Composable
fun EditPartidoScreen(
    partidoId: Long,
    navController: NavController,
    editPartidoViewModel: EditPartidoViewModel,
    onFinish: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val partido by editPartidoViewModel.partido.collectAsStateWithLifecycle()
    val loading by editPartidoViewModel.loading.collectAsStateWithLifecycle()
    val eliminado by editPartidoViewModel.eliminado.collectAsStateWithLifecycle()
    val guardado by editPartidoViewModel.guardado.collectAsStateWithLifecycle()
    val jugadoresEquipoA by editPartidoViewModel.jugadoresEquipoA.collectAsStateWithLifecycle()
    val jugadoresEquipoB by editPartidoViewModel.jugadoresEquipoB.collectAsStateWithLifecycle()
    val jugadoresCargados by editPartidoViewModel.jugadoresCargados.collectAsStateWithLifecycle()
    val equipoANombreVM by editPartidoViewModel.nombreEquipoA.collectAsStateWithLifecycle()
    val equipoBNombreVM by editPartidoViewModel.nombreEquipoB.collectAsStateWithLifecycle()

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

    val calendar = remember { java.util.Calendar.getInstance() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(equipoANombreVM) {
        if (!equipoAEditando && equipoANombreVM != null) {
            equipoANombre = equipoANombreVM!!
        }
    }
    LaunchedEffect(equipoBNombreVM) {
        if (!equipoBEditando && equipoBNombreVM != null) {
            equipoBNombre = equipoBNombreVM!!
        }
    }

    LaunchedEffect(partido) {
        partido?.let {
            fecha = it.fecha
            horaInicio = it.horaInicio
            numeroPartes = it.numeroPartes.toString()
            tiempoPorParte = it.tiempoPorParte.toString()
        }
    }

    LaunchedEffect(eliminado, guardado) {
        if (eliminado || guardado) {
            navController.previousBackStackEntry?.arguments?.putBoolean("reload_partidos", true)
            navController.previousBackStackEntry?.arguments?.putBoolean("reload_partido", true)
            onFinish?.invoke()
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
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                horaInicio = "%02d:%02d".format(hourOfDay, minute)
            },
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
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
        EditPartidoForm(
            partido = partido!!,
            equipoANombre = equipoANombre,
            onEquipoANombreChange = { equipoANombre = it },
            equipoAEditando = equipoAEditando,
            onEquipoAEditandoChange = { equipoAEditando = it },
            equipoBNombre = equipoBNombre,
            onEquipoBNombreChange = { equipoBNombre = it },
            equipoBEditando = equipoBEditando,
            onEquipoBEditandoChange = { equipoBEditando = it },
            scope = scope,
            editPartidoViewModel = editPartidoViewModel,
            errorGeneral = errorGeneral,
            onErrorGeneralChange = { errorGeneral = it },
            fecha = fecha,
            onFechaChange = { fecha = it },
            horaInicio = horaInicio,
            onHoraInicioChange = { horaInicio = it },
            numeroPartes = numeroPartes,
            onNumeroPartesChange = { numeroPartes = it },
            tiempoPorParte = tiempoPorParte,
            onTiempoPorParteChange = { tiempoPorParte = it },
            camposError = camposError,
            mostrarErrores = mostrarErrores,
            onMostrarErroresChange = { mostrarErrores = it },
            datePickerDialog = datePickerDialog,
            timePickerDialog = timePickerDialog,
            navController = navController,
            partidoId = partidoId,
            validarCampos = ::validarCampos,
            setShowDeleteDialog = { showDeleteDialog = it }
        )
        if (showDeleteDialog) {
            EditPartidoDeleteDialog(
                onConfirmDelete = {
                    showDeleteDialog = false
                    editPartidoViewModel.eliminarPartido()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

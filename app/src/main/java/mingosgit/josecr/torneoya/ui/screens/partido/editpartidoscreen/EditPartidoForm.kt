package mingosgit.josecr.torneoya.ui.screens.partido.editpartidoscreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partido.EditPartidoViewModel
import mingosgit.josecr.torneoya.data.entities.PartidoEntity

@Composable
fun EditPartidoForm(
    partido: PartidoEntity,
    equipoANombre: String,
    onEquipoANombreChange: (String) -> Unit,
    equipoAEditando: Boolean,
    onEquipoAEditandoChange: (Boolean) -> Unit,
    equipoBNombre: String,
    onEquipoBNombreChange: (String) -> Unit,
    equipoBEditando: Boolean,
    onEquipoBEditandoChange: (Boolean) -> Unit,
    scope: CoroutineScope,
    editPartidoViewModel: EditPartidoViewModel,
    errorGeneral: String?,
    onErrorGeneralChange: (String?) -> Unit,
    fecha: String,
    onFechaChange: (String) -> Unit,
    horaInicio: String,
    onHoraInicioChange: (String) -> Unit,
    numeroPartes: String,
    onNumeroPartesChange: (String) -> Unit,
    tiempoPorParte: String,
    onTiempoPorParteChange: (String) -> Unit,
    camposError: Map<String, Boolean>,
    mostrarErrores: Boolean,
    onMostrarErroresChange: (Boolean) -> Unit,
    datePickerDialog: DatePickerDialog,
    timePickerDialog: TimePickerDialog,
    navController: NavController,
    partidoId: Long,
    validarCampos: () -> Boolean,
    setShowDeleteDialog: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Editar Partido", fontSize = 28.sp, modifier = Modifier.padding(bottom = 24.dp))
        EditPartidoTeamNameField(
            label = "Equipo A",
            nombre = equipoANombre,
            onNombreChange = onEquipoANombreChange,
            editando = equipoAEditando,
            onEditandoChange = onEquipoAEditandoChange,
            onGuardar = {
                scope.launch {
                    val exito = editPartidoViewModel.actualizarEquipoNombre(partido.equipoAId, equipoANombre)
                    if (!exito) onErrorGeneralChange("No se pudo actualizar el nombre del equipo A")
                }
            }
        )
        EditPartidoTeamNameField(
            label = "Equipo B",
            nombre = equipoBNombre,
            onNombreChange = onEquipoBNombreChange,
            editando = equipoBEditando,
            onEditandoChange = onEquipoBEditandoChange,
            onGuardar = {
                scope.launch {
                    val exito = editPartidoViewModel.actualizarEquipoNombre(partido.equipoBId, equipoBNombre)
                    if (!exito) onErrorGeneralChange("No se pudo actualizar el nombre del equipo B")
                }
            }
        )
        EditPartidoFields(
            fecha = fecha,
            onFechaChange = onFechaChange,
            horaInicio = horaInicio,
            onHoraInicioChange = onHoraInicioChange,
            numeroPartes = numeroPartes,
            onNumeroPartesChange = onNumeroPartesChange,
            tiempoPorParte = tiempoPorParte,
            onTiempoPorParteChange = onTiempoPorParteChange,
            camposError = camposError,
            mostrarErrores = mostrarErrores,
            datePickerDialog = datePickerDialog,
            timePickerDialog = timePickerDialog
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                navController.navigate("editar_jugadores/$partidoId?equipoAId=${partido.equipoAId}&equipoBId=${partido.equipoBId}")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text("Editar Jugadores")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                // Si está editando algún nombre, primero lo guarda (simula dar al tick)
                if (equipoAEditando) {
                    scope.launch {
                        editPartidoViewModel.actualizarEquipoNombre(partido.equipoAId, equipoANombre)
                    }
                    onEquipoAEditandoChange(false)
                }
                if (equipoBEditando) {
                    scope.launch {
                        editPartidoViewModel.actualizarEquipoNombre(partido.equipoBId, equipoBNombre)
                    }
                    onEquipoBEditandoChange(false)
                }
                onMostrarErroresChange(true)
                if (validarCampos()) {
                    editPartidoViewModel.actualizarPartido(
                        fecha,
                        horaInicio,
                        numeroPartes.toInt(),
                        tiempoPorParte.toInt()
                    )
                } else {
                    onErrorGeneralChange("Revisa los campos obligatorios.")
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
            onClick = { setShowDeleteDialog(true) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Eliminar Partido")
        }
        Spacer(modifier = Modifier.height(32.dp))
        if (errorGeneral != null) {
            Text(
                errorGeneral,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


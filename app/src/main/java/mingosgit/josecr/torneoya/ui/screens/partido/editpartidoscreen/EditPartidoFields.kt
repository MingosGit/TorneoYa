package mingosgit.josecr.torneoya.ui.screens.partido.editpartidoscreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditPartidoFields(
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
    datePickerDialog: DatePickerDialog,
    timePickerDialog: TimePickerDialog
) {
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
                    if (mostrarErrores && camposError["horaInicio"] == true) Color(0xFFFFCDD2) else Color.Transparent
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
        onValueChange = { onNumeroPartesChange(it.filter { c -> c.isDigit() }) },
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
        onValueChange = { onTiempoPorParteChange(it.filter { c -> c.isDigit() }) },
        label = { Text("Minutos por parte") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = mostrarErrores && camposError["tiempoPorParte"] == true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(
                if (mostrarErrores && camposError["tiempoPorParte"] == true) Color(0xFFFFCDD2) else Color.Transparent
            )
    )
    if (mostrarErrores && camposError["tiempoPorParte"] == true) {
        Text("Campo obligatorio o inválido", color = Color.Red, fontSize = 12.sp)
    }
}

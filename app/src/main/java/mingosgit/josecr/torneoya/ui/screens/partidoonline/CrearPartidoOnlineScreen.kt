package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.partidoonline.CreatePartidoOnlineViewModel
import mingosgit.josecr.torneoya.R
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalTextStyle
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.CustomDatePickerDialog
import mingosgit.josecr.torneoya.ui.screens.partidoonline.visualizacion.CustomTimePickerDialog
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.ui.theme.text

@Composable
fun CrearPartidoOnlineScreen(
    navController: NavController,
    viewModel: CreatePartidoOnlineViewModel
) {
    var equipoA by rememberSaveable { mutableStateOf("") }
    var equipoB by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var horaInicio by rememberSaveable { mutableStateOf("") }
    var numeroPartes by rememberSaveable { mutableStateOf("2") }
    var tiempoPorParte by rememberSaveable { mutableStateOf("25") }
    var tiempoDescanso by rememberSaveable { mutableStateOf("5") }
    val numeroJugadores = 5
    var isPublic by rememberSaveable { mutableStateOf(true) }

    var camposError by rememberSaveable { mutableStateOf(mapOf<String, Boolean>()) }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    fun validarCampos(): Boolean {
        val errores = mutableMapOf<String, Boolean>()
        errores["equipoA"] = equipoA.isBlank()
        errores["equipoB"] = equipoB.isBlank()
        errores["fecha"] = fecha.isBlank()
        errores["horaInicio"] = horaInicio.isBlank()
        errores["numeroPartes"] = numeroPartes.isBlank() || numeroPartes.toIntOrNull() == null
        errores["tiempoPorParte"] = tiempoPorParte.isBlank() || tiempoPorParte.toIntOrNull() == null
        errores["tiempoDescanso"] = tiempoDescanso.isBlank() || tiempoDescanso.toIntOrNull() == null
        camposError = errores
        return !errores.values.any { it }
    }

    val cs = MaterialTheme.colorScheme
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val gradientError = Brush.horizontalGradient(listOf(cs.error, cs.secondary))
    val fieldBg = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TorneoYaPalette.backgroundGradient)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.crearpartido_title),
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                color = cs.text,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            GradientOutlinedField(
                value = equipoA,
                onValueChange = { equipoA = it },
                label = stringResource(id = R.string.crearpartido_nombre_equipo_a),
                isError = mostrarErrores && camposError["equipoA"] == true,
                height = 64.dp
            )
            if (mostrarErrores && camposError["equipoA"] == true) {
                Text(
                    stringResource(id = R.string.crearpartido_campo_obligatorio),
                    color = cs.error,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(Modifier.height(10.dp))

            GradientOutlinedField(
                value = equipoB,
                onValueChange = { equipoB = it },
                label = stringResource(id = R.string.crearpartido_nombre_equipo_b),
                isError = mostrarErrores && camposError["equipoB"] == true,
                height = 64.dp
            )
            if (mostrarErrores && camposError["equipoB"] == true) {
                Text(
                    stringResource(id = R.string.crearpartido_campo_obligatorio),
                    color = cs.error,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(Modifier.height(13.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GradientButton(
                    text = if (fecha.isBlank()) stringResource(id = R.string.crearpartido_seleccionar_fecha) else fecha,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    isError = mostrarErrores && camposError["fecha"] == true,
                    height = 58.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                GradientButton(
                    text = if (horaInicio.isBlank()) stringResource(id = R.string.crearpartido_seleccionar_hora) else horaInicio,
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    isError = mostrarErrores && camposError["horaInicio"] == true,
                    height = 58.dp
                )
            }
            if (mostrarErrores && (camposError["fecha"] == true || camposError["horaInicio"] == true)) {
                Row(Modifier.fillMaxWidth()) {
                    if (camposError["fecha"] == true)
                        Text(
                            stringResource(id = R.string.crearpartido_falta_fecha),
                            color = cs.error,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["horaInicio"] == true)
                        Text(
                            stringResource(id = R.string.crearpartido_falta_hora),
                            color = cs.error,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                }
            }

            Spacer(modifier = Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(id = R.string.crearpartido_numero_partes),
                    color = cs.mutedText,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, bottom = 2.dp)
                )
                Text(
                    stringResource(id = R.string.crearpartido_min_por_parte),
                    color = cs.mutedText,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, bottom = 2.dp)
                )
                Text(
                    stringResource(id = R.string.crearpartido_descanso_min),
                    color = cs.mutedText,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GradientOutlinedField(
                    value = numeroPartes,
                    onValueChange = { numeroPartes = it.filter { c -> c.isDigit() } },
                    label = "",
                    isError = mostrarErrores && camposError["numeroPartes"] == true,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    height = 52.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                GradientOutlinedField(
                    value = tiempoPorParte,
                    onValueChange = { tiempoPorParte = it.filter { c -> c.isDigit() } },
                    label = "",
                    isError = mostrarErrores && camposError["tiempoPorParte"] == true,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    height = 52.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                GradientOutlinedField(
                    value = tiempoDescanso,
                    onValueChange = { tiempoDescanso = it.filter { c -> c.isDigit() } },
                    label = "",
                    isError = mostrarErrores && camposError["tiempoDescanso"] == true,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    height = 52.dp
                )
            }
            if (mostrarErrores && (camposError["numeroPartes"] == true || camposError["tiempoPorParte"] == true || camposError["tiempoDescanso"] == true)) {
                Row(Modifier.fillMaxWidth()) {
                    if (camposError["numeroPartes"] == true)
                        Text(
                            stringResource(id = R.string.crearpartido_obligatorio_invalido),
                            color = cs.error,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["tiempoPorParte"] == true)
                        Text(
                            stringResource(id = R.string.crearpartido_obligatorio_invalido),
                            color = cs.error,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["tiempoDescanso"] == true)
                        Text(
                            stringResource(id = R.string.crearpartido_obligatorio_invalido),
                            color = cs.error,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .background(cs.surface)
                    .border(
                        2.dp,
                        gradientPrimary,
                        RoundedCornerShape(15.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            ) {
                Checkbox(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = cs.primary,
                        uncheckedColor = cs.outline,
                        checkmarkColor = cs.onPrimary
                    )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(id = R.string.crearpartido_partido_publico),
                    color = cs.mutedText,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 2.dp,
                        brush = gradientPrimary,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(
                        Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))
                    )
                    .clickable(
                        enabled = !guardando,
                        onClick = {
                            if (validarCampos()) {
                                guardando = true
                                scope.launch {
                                    viewModel.crearPartidoOnline(
                                        equipoA = equipoA,
                                        equipoB = equipoB,
                                        fecha = fecha,
                                        horaInicio = horaInicio,
                                        numeroPartes = numeroPartes.toInt(),
                                        tiempoPorParte = tiempoPorParte.toInt(),
                                        tiempoDescanso = tiempoDescanso.toInt(),
                                        numeroJugadores = numeroJugadores,
                                        isPublic = isPublic
                                    ) { partidoUid, equipoAUid, equipoBUid ->
                                        navController.navigate("asignar_jugadores_online/$partidoUid?equipoAUid=$equipoAUid&equipoBUid=$equipoBUid") {
                                            popUpTo("partido_online") { inclusive = false }
                                        }
                                    }
                                    guardando = false
                                }
                                mostrarErrores = false
                            } else {
                                mostrarErrores = true
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!guardando) {
                    Text(
                        stringResource(id = R.string.crearpartido_crear_asignar),
                        color = cs.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                } else {
                    CircularProgressIndicator(
                        color = cs.primary,
                        strokeWidth = 2.2.dp,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        CustomDatePickerDialog(
            show = showDatePicker,
            initialDate = Calendar.getInstance(),
            onDismiss = { showDatePicker = false },
            onDateSelected = { cal ->
                fecha = "%02d-%02d-%04d".format(
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR)
                )
            }
        )
        CustomTimePickerDialog(
            show = showTimePicker,
            initialHour = horaInicio.takeIf { it.isNotBlank() }?.split(":")?.get(0)?.toIntOrNull()
                ?: 0,
            initialMinute = horaInicio.takeIf { it.isNotBlank() }?.split(":")?.get(1)?.toIntOrNull()
                ?: 0,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, min ->
                horaInicio = "%02d:%02d".format(hour, min)
            }
        )
    }
}

@Composable
fun GradientOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp
) {
    val cs = MaterialTheme.colorScheme
    val borderBrush = if (isError) Brush.horizontalGradient(listOf(cs.error, cs.secondary)) else Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val bgBrush = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(15.dp))
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(15.dp)
            )
            .background(bgBrush)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = if (label.isNotBlank()) ({ Text(label, color = cs.mutedText, fontSize = 17.sp) }) else null,
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
                .padding(horizontal = 8.dp)
                .defaultMinSize(minHeight = 1.dp)
                .background(Color.Transparent),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    height: Dp = 58.dp
) {
    val cs = MaterialTheme.colorScheme
    val borderBrush = if (isError) Brush.horizontalGradient(listOf(cs.error, cs.secondary)) else Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val bgBrush = Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(15.dp))
            .border(
                width = 2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(15.dp)
            )
            .background(bgBrush)
            .clickable { onClick() }
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isError) cs.error else cs.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}



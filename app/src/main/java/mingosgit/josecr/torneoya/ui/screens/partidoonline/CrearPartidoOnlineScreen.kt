package mingosgit.josecr.torneoya.ui.screens.partidoonline

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.partidoonline.CreatePartidoOnlineViewModel

@Composable
fun CrearPartidoOnlineScreen(
    navController: NavController,
    viewModel: CreatePartidoOnlineViewModel
) {
    val context = LocalContext.current

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

    val calendar = remember { java.util.Calendar.getInstance() }
    val scope = rememberCoroutineScope()

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

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(modernBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Crear Partido Online",
                fontSize = 27.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // EQUIPO A
            GradientOutlinedField(
                value = equipoA,
                onValueChange = { equipoA = it },
                label = "Nombre Equipo A",
                isError = mostrarErrores && camposError["equipoA"] == true,
                height = 64.dp
            )
            if (mostrarErrores && camposError["equipoA"] == true) {
                Text("Campo obligatorio", color = Color(0xFFFF7675), fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))
            }

            Spacer(Modifier.height(10.dp))

            // EQUIPO B
            GradientOutlinedField(
                value = equipoB,
                onValueChange = { equipoB = it },
                label = "Nombre Equipo B",
                isError = mostrarErrores && camposError["equipoB"] == true,
                height = 64.dp
            )
            if (mostrarErrores && camposError["equipoB"] == true) {
                Text("Campo obligatorio", color = Color(0xFFFF7675), fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))
            }

            Spacer(Modifier.height(13.dp))

            // FECHA Y HORA
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GradientButton(
                    text = if (fecha.isBlank()) "Seleccionar fecha" else fecha,
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f),
                    isError = mostrarErrores && camposError["fecha"] == true,
                    height = 58.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                GradientButton(
                    text = if (horaInicio.isBlank()) "Seleccionar hora" else horaInicio,
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f),
                    isError = mostrarErrores && camposError["horaInicio"] == true,
                    height = 58.dp
                )
            }
            if (mostrarErrores && (camposError["fecha"] == true || camposError["horaInicio"] == true)) {
                Row(Modifier.fillMaxWidth()) {
                    if (camposError["fecha"] == true)
                        Text(
                            "Falta la fecha",
                            color = Color(0xFFFF7675),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["horaInicio"] == true)
                        Text(
                            "Falta la hora",
                            color = Color(0xFFFF7675),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                }
            }

            Spacer(modifier = Modifier.height(13.dp))

            // ETIQUETAS ARRIBA DE CADA CAMPO NUMÉRICO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Nº de partes",
                    color = Color(0xFFB7B7D1),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(start = 4.dp, bottom = 2.dp)
                )
                Text(
                    "Min/parte",
                    color = Color(0xFFB7B7D1),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(start = 4.dp, bottom = 2.dp)
                )
                Text(
                    "Descanso (min)",
                    color = Color(0xFFB7B7D1),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f).padding(start = 4.dp, bottom = 2.dp)
                )
            }

            // CAMPOS DE NUMEROS
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
                            "Obligatorio o inválido",
                            color = Color(0xFFFF7675),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["tiempoPorParte"] == true)
                        Text(
                            "Obligatorio o inválido",
                            color = Color(0xFFFF7675),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    if (camposError["tiempoDescanso"] == true)
                        Text(
                            "Obligatorio o inválido",
                            color = Color(0xFFFF7675),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CHECKBOX PUBLICO
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF23273D))
                    .border(
                        2.dp,
                        Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            ) {
                Checkbox(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = TorneoYaPalette.blue,
                        uncheckedColor = Color(0xFF6D6D8A),
                        checkmarkColor = Color.White
                    )
                )
                Spacer(Modifier.width(10.dp))
                Text("Partido público (compartible por link/UID)", color = Color(0xFFB7B7D1), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // BOTON CREAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                        )
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
                        "Crear y asignar jugadores",
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 19.sp
                    )
                } else {
                    CircularProgressIndicator(
                        color = TorneoYaPalette.blue,
                        strokeWidth = 2.2.dp,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

// ------------------- COMPONENTES REUTILIZABLES ESTILO TORNEOYA -------------------

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
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(15.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    if (isError) listOf(Color(0xFFFF7675), TorneoYaPalette.violet)
                    else listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(15.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
            )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = if (label.isNotBlank()) ({ Text(label, color = Color(0xFFB7B7D1), fontSize = 17.sp) }) else null,
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                focusedLabelColor = TorneoYaPalette.blue,
                errorLabelColor = Color(0xFFFF7675),
                cursorColor = TorneoYaPalette.blue,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorTextColor = Color.White,
                disabledTextColor = Color(0xFFB7B7D1)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp, bottom = 0.dp, start = 8.dp, end = 8.dp)
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
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(15.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    if (isError) listOf(Color(0xFFFF7675), TorneoYaPalette.violet)
                    else listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                ),
                shape = RoundedCornerShape(15.dp)
            )
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                )
            )
            .clickable { onClick() }
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isError) Color(0xFFFF7675) else TorneoYaPalette.blue,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import java.util.*

@Composable
fun CustomDatePickerDialog(
    show: Boolean,
    initialDate: Calendar = Calendar.getInstance(),
    onDismiss: () -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    if (!show) return

    var year by remember { mutableStateOf(initialDate.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(initialDate.get(Calendar.MONTH) + 1) }
    var day by remember { mutableStateOf(initialDate.get(Calendar.DAY_OF_MONTH)) }
    val monthNames = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )
    var showMonthDropdown by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xA0161925))
            .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF23273D), Color(0xFF191A23))
                    )
                )
                .border(
                    2.dp,
                    Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                    RoundedCornerShape(22.dp)
                )
                .padding(28.dp)
                .widthIn(min = 310.dp, max = 350.dp)
        ) {
            Text(
                "Selecciona una fecha",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            // Día (input), Mes (dropdown), Año (input)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Día", color = Color(0xFFB7B7D1), fontSize = 15.sp)
                    OutlinedTextField(
                        value = day.toString(),
                        onValueChange = { txt ->
                            txt.toIntOrNull()?.let {
                                if (it in 1..31) day = it
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.width(60.dp).height(54.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = TorneoYaPalette.blue,
                            cursorColor = TorneoYaPalette.blue,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mes", color = Color(0xFFB7B7D1), fontSize = 15.sp)
                    Box {
                        Button(
                            onClick = { showMonthDropdown = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF191A23),
                                contentColor = TorneoYaPalette.blue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(90.dp).height(54.dp)
                        ) {
                            Text(monthNames[month - 1], fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false },
                            modifier = Modifier.background(Color(0xFF23273D))
                        ) {
                            monthNames.forEachIndexed { idx, name ->
                                DropdownMenuItem(
                                    text = { Text(name, color = Color.White) },
                                    onClick = {
                                        month = idx + 1
                                        showMonthDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Año", color = Color(0xFFB7B7D1), fontSize = 15.sp)
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = { txt ->
                            txt.toIntOrNull()?.let {
                                if (it in (year - 60)..(year + 60)) year = it
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.width(75.dp).height(54.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = TorneoYaPalette.blue,
                            cursorColor = TorneoYaPalette.blue,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB7B7D1))
                ) { Text("Cancelar") }
                Button(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(year, month - 1, day)
                        focusManager.clearFocus()
                        onDateSelected(cal)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TorneoYaPalette.blue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Aceptar") }
            }
        }
    }
}

@Composable
fun CustomTimePickerDialog(
    show: Boolean,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    if (!show) return

    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }
    val focusManager = LocalFocusManager.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xA0161925))
            .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF23273D), Color(0xFF191A23))
                    )
                )
                .border(
                    2.dp,
                    Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)),
                    RoundedCornerShape(22.dp)
                )
                .padding(28.dp)
                .widthIn(min = 270.dp, max = 350.dp)
        ) {
            Text(
                "Selecciona una hora",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = hour.toString().padStart(2, '0'),
                    onValueChange = { txt ->
                        txt.toIntOrNull()?.let {
                            if (it in 0..23) hour = it
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.width(66.dp).height(54.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = TorneoYaPalette.blue,
                        cursorColor = TorneoYaPalette.blue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                )
                Text(":", color = Color.White, fontSize = 32.sp, modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedTextField(
                    value = minute.toString().padStart(2, '0'),
                    onValueChange = { txt ->
                        txt.toIntOrNull()?.let {
                            if (it in 0..59) minute = it
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.width(66.dp).height(54.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = TorneoYaPalette.blue,
                        cursorColor = TorneoYaPalette.blue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB7B7D1))
                ) { Text("Cancelar") }
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onTimeSelected(hour, minute)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TorneoYaPalette.blue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Aceptar") }
            }
        }
    }
}

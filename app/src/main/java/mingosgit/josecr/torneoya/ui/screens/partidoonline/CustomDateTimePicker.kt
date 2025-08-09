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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.ui.theme.text
import java.util.*

@Composable
fun CustomDatePickerDialog(
    show: Boolean,
    initialDate: Calendar = Calendar.getInstance(),
    onDismiss: () -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    if (!show) return

    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val panelBg = Brush.verticalGradient(listOf(cs.surfaceVariant, cs.surface))

    var year by remember { mutableStateOf(initialDate.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(initialDate.get(Calendar.MONTH) + 1) }
    var day by remember { mutableStateOf(initialDate.get(Calendar.DAY_OF_MONTH)) }
    val monthNames = listOf(
        stringResource(id = R.string.datepicker_month_ene),
        stringResource(id = R.string.datepicker_month_feb),
        stringResource(id = R.string.datepicker_month_mar),
        stringResource(id = R.string.datepicker_month_abr),
        stringResource(id = R.string.datepicker_month_may),
        stringResource(id = R.string.datepicker_month_jun),
        stringResource(id = R.string.datepicker_month_jul),
        stringResource(id = R.string.datepicker_month_ago),
        stringResource(id = R.string.datepicker_month_sep),
        stringResource(id = R.string.datepicker_month_oct),
        stringResource(id = R.string.datepicker_month_nov),
        stringResource(id = R.string.datepicker_month_dic)
    )
    var showMonthDropdown by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(cs.background.copy(alpha = 0.63f))
            .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(22.dp))
                .background(panelBg)
                .border(2.dp, gradientPrimary, RoundedCornerShape(22.dp))
                .padding(28.dp)
                .widthIn(min = 310.dp, max = 350.dp)
        ) {
            Text(
                text = stringResource(id = R.string.datepicker_title),
                fontSize = 22.sp,
                color = cs.text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(id = R.string.datepicker_label_day),
                        color = cs.mutedText,
                        fontSize = 15.sp
                    )
                    OutlinedTextField(
                        value = day.toString(),
                        onValueChange = { txt -> txt.toIntOrNull()?.let { if (it in 1..31) day = it } },
                        singleLine = true,
                        modifier = Modifier.width(60.dp).height(54.dp),
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
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = cs.text
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(id = R.string.datepicker_label_month),
                        color = cs.mutedText,
                        fontSize = 15.sp
                    )
                    Box {
                        Button(
                            onClick = { showMonthDropdown = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.surface,
                                contentColor = cs.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(110.dp).height(54.dp)
                        ) {
                            Text(
                                monthNames[month - 1],
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        DropdownMenu(
                            expanded = showMonthDropdown,
                            onDismissRequest = { showMonthDropdown = false },
                            modifier = Modifier.background(cs.surface)
                        ) {
                            monthNames.forEachIndexed { idx, name ->
                                DropdownMenuItem(
                                    text = { Text(name, color = cs.text) },
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
                    Text(
                        stringResource(id = R.string.datepicker_label_year),
                        color = cs.mutedText,
                        fontSize = 15.sp
                    )
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = { txt ->
                            txt.toIntOrNull()?.let { if (it in (year - 60)..(year + 60)) year = it }
                        },
                        singleLine = true,
                        modifier = Modifier.width(80.dp).height(54.dp),
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
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = cs.text
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
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.mutedText)
                ) { Text(stringResource(id = R.string.gen_cancelar)) }
                Button(
                    onClick = {
                        val cal = Calendar.getInstance().apply { set(year, month - 1, day) }
                        focusManager.clearFocus()
                        onDateSelected(cal)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor = cs.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(stringResource(id = R.string.gen_guardar)) }
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

    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val panelBg = Brush.verticalGradient(listOf(cs.surfaceVariant, cs.surface))

    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    Box(
        Modifier
            .fillMaxSize()
            .background(cs.background.copy(alpha = 0.63f))
            .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(22.dp))
                .background(panelBg)
                .border(2.dp, gradientPrimary, RoundedCornerShape(22.dp))
                .padding(28.dp)
                .widthIn(min = 270.dp, max = 350.dp)
        ) {
            Text(
                text = stringResource(id = R.string.timepicker_title),
                fontSize = 22.sp,
                color = cs.text,
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
                    onValueChange = { txt -> txt.toIntOrNull()?.let { if (it in 0..23) hour = it } },
                    singleLine = true,
                    modifier = Modifier.width(66.dp).height(54.dp),
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
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = cs.text
                    )
                )
                Text(":", color = cs.text, fontSize = 32.sp, modifier = Modifier.padding(horizontal = 8.dp))
                OutlinedTextField(
                    value = minute.toString().padStart(2, '0'),
                    onValueChange = { txt -> txt.toIntOrNull()?.let { if (it in 0..59) minute = it } },
                    singleLine = true,
                    modifier = Modifier.width(66.dp).height(54.dp),
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
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = cs.text
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
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.mutedText)
                ) { Text(stringResource(id = R.string.gen_cancelar)) }
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onTimeSelected(hour, minute)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor = cs.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(stringResource(id = R.string.gen_guardar)) }
            }
        }
    }
}

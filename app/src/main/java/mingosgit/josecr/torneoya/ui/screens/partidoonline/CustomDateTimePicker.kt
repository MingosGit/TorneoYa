package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText
import mingosgit.josecr.torneoya.ui.theme.text
import java.util.Calendar

// ========================== DATE PICKER (AÑOS: ACTUAL..ACTUAL+5) ==========================

@Composable
fun CustomDatePickerDialog(
    show: Boolean,
    initialDate: Calendar = Calendar.getInstance(),
    onDismiss: () -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    if (!show) return

    val cs = MaterialTheme.colorScheme
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val panelBg = Brush.verticalGradient(listOf(cs.surfaceVariant, cs.surface))

    val now = remember { Calendar.getInstance() }
    val currentYear = now.get(Calendar.YEAR)
    val maxYear = currentYear + 5

    var year by remember { mutableIntStateOf(initialDate.get(Calendar.YEAR).coerceIn(currentYear, maxYear)) }
    var month by remember { mutableIntStateOf(initialDate.get(Calendar.MONTH) + 1) } // 1..12
    var day by remember { mutableIntStateOf(initialDate.get(Calendar.DAY_OF_MONTH)) }

    // Menús
    var showYearMenu by remember { mutableStateOf(false) }
    var showMonthMenu by remember { mutableStateOf(false) }
    var showDayMenu by remember { mutableStateOf(false) }

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

    // Días válidos según mes/año
    val maxDay = remember(year, month) { daysInMonth(year, month) }
    LaunchedEffect(year, month) {
        if (day > maxDay) day = maxDay
        if (day < 1) day = 1
    }

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
                .padding(24.dp)
                .widthIn(min = 320.dp, max = 380.dp)
        ) {
            Text(
                text = stringResource(id = R.string.datepicker_title),
                fontSize = 22.sp,
                color = cs.text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Accesos rápidos
            QuickDateChips(
                onToday = {
                    val cal = Calendar.getInstance()
                    year = cal.get(Calendar.YEAR).coerceIn(currentYear, maxYear)
                    month = cal.get(Calendar.MONTH) + 1
                    day = cal.get(Calendar.DAY_OF_MONTH)
                },
                onTomorrow = {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    year = cal.get(Calendar.YEAR).coerceIn(currentYear, maxYear)
                    month = cal.get(Calendar.MONTH) + 1
                    day = cal.get(Calendar.DAY_OF_MONTH)
                },
                onNextWeek = {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_MONTH, 7)
                    year = cal.get(Calendar.YEAR).coerceIn(currentYear, maxYear)
                    month = cal.get(Calendar.MONTH) + 1
                    day = cal.get(Calendar.DAY_OF_MONTH)
                }
            )

            Spacer(Modifier.height(8.dp))

            // Selectores (DÍA / MES / AÑO)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Día
                Box {
                    LabeledButton(
                        label = stringResource(id = R.string.datepicker_label_day),
                        value = day.toString().padStart(2, '0'),
                        onClick = { showDayMenu = true }
                    )
                    DropdownMenu(
                        expanded = showDayMenu,
                        onDismissRequest = { showDayMenu = false }
                    ) {
                        (1..maxDay).forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.toString().padStart(2, '0')) },
                                onClick = {
                                    day = d
                                    showDayMenu = false
                                }
                            )
                        }
                    }
                }

                // Mes
                Box {
                    LabeledButton(
                        label = stringResource(id = R.string.datepicker_label_month),
                        value = monthNames[month - 1],
                        onClick = { showMonthMenu = true },
                        minWidth = 130.dp
                    )
                    DropdownMenu(
                        expanded = showMonthMenu,
                        onDismissRequest = { showMonthMenu = false }
                    ) {
                        monthNames.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    month = idx + 1
                                    showMonthMenu = false
                                }
                            )
                        }
                    }
                }

                // Año (solo ACTUAL..ACTUAL+5)
                Box {
                    LabeledButton(
                        label = stringResource(id = R.string.datepicker_label_year),
                        value = year.toString(),
                        onClick = { showYearMenu = true },
                        minWidth = 92.dp
                    )
                    DropdownMenu(
                        expanded = showYearMenu,
                        onDismissRequest = { showYearMenu = false }
                    ) {
                        for (y in currentYear..maxYear) {
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = {
                                    year = y
                                    showYearMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.mutedText)
                ) { Text(stringResource(id = R.string.gen_cancelar)) }

                Button(
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month - 1)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
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
private fun QuickDateChips(
    onToday: () -> Unit,
    onTomorrow: () -> Unit,
    onNextWeek: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val chipShape = RoundedCornerShape(12.dp)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = chipShape,
            color = TorneoYaPalette.violet.copy(alpha = 0.08f),
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(36.dp)
                .clip(chipShape)
        ) {
            Box(
                modifier = Modifier
                    .clickableNoRipple { onToday() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.datepicker_quick_today), color = cs.text, fontSize = 14.sp)
            }
        }
        Surface(
            shape = chipShape,
            color = TorneoYaPalette.blue.copy(alpha = 0.08f),
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(36.dp)
                .clip(chipShape)
        ) {
            Box(
                modifier = Modifier
                    .clickableNoRipple { onTomorrow() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.datepicker_quick_tomorrow), color = cs.text, fontSize = 14.sp)
            }
        }
        Surface(
            shape = chipShape,
            color = cs.surfaceVariant,
            tonalElevation = 0.dp,
            modifier = Modifier
                .height(36.dp)
                .clip(chipShape)
        ) {
            Box(
                modifier = Modifier
                    .clickableNoRipple { onNextWeek() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.datepicker_quick_nextweek), color = cs.text, fontSize = 14.sp)
            }
        }
    }
}

private fun daysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))
}

// Clickable sin ripple para chips
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = { onClick() })
    })

// Botón con etiqueta arriba + valor destacado
@Composable
private fun LabeledButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    minWidth: Dp = 92.dp
) {
    val cs = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = cs.mutedText, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = cs.surface,
                contentColor = cs.primary
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(54.dp)
                .widthIn(min = minWidth)
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = cs.text)
        }
    }
}

// ========================== TIME PICKER (DESPLEGABLES) ==========================

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
    val gradientPrimary = Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    val panelBg = Brush.verticalGradient(listOf(cs.surfaceVariant, cs.surface))

    var hour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    var showHourMenu by remember { mutableStateOf(false) }
    var showMinuteMenu by remember { mutableStateOf(false) }

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
                .padding(24.dp)
                .widthIn(min = 300.dp, max = 360.dp)
        ) {
            Text(
                text = stringResource(id = R.string.timepicker_title),
                fontSize = 22.sp,
                color = cs.text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Button(
                        onClick = { showHourMenu = true },
                        modifier = Modifier.width(90.dp).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.surface)
                    ) {
                        Text(hour.toString().padStart(2, '0'), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = cs.text)
                    }
                    DropdownMenu(
                        expanded = showHourMenu,
                        onDismissRequest = { showHourMenu = false }
                    ) {
                        (0..23).forEach { h ->
                            DropdownMenuItem(
                                text = { Text(h.toString().padStart(2, '0')) },
                                onClick = {
                                    hour = h
                                    showHourMenu = false
                                }
                            )
                        }
                    }
                }
                Text(":", color = cs.text, fontSize = 32.sp, modifier = Modifier.padding(horizontal = 10.dp))
                Box {
                    Button(
                        onClick = { showMinuteMenu = true },
                        modifier = Modifier.width(90.dp).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.surface)
                    ) {
                        Text(minute.toString().padStart(2, '0'), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = cs.text)
                    }
                    DropdownMenu(
                        expanded = showMinuteMenu,
                        onDismissRequest = { showMinuteMenu = false }
                    ) {
                        (0..59).forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.toString().padStart(2, '0')) },
                                onClick = {
                                    minute = m
                                    showMinuteMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = cs.mutedText)
                ) { Text(stringResource(id = R.string.gen_cancelar)) }

                Button(
                    onClick = {
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

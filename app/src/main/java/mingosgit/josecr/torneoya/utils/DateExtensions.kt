package mingosgit.josecr.torneoya.utils

import java.text.SimpleDateFormat
import java.util.*

fun Long.formatAsDateString(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

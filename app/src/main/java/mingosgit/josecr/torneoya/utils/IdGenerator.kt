package mingosgit.josecr.torneoya.utils

import java.text.SimpleDateFormat
import java.util.*

object IdGenerator {
    fun generarId(fecha: String, hora: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        val date = sdf.parse("$fecha $hora:00.000")
        return date?.time ?: System.currentTimeMillis()
    }
}

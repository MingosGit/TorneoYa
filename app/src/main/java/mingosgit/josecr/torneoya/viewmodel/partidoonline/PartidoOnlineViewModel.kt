package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class PartidoConNombresOnline(
    val uid: String,
    val nombreEquipoA: String,
    val nombreEquipoB: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String
)

class PartidoOnlineViewModel(
    private val partidoRepo: PartidoFirebaseRepository,
    private val equipoRepo: PartidoFirebaseRepository,
    private val usuarioUid: String
) : ViewModel() {
    private val _partidos = MutableStateFlow<List<PartidoFirebase>>(emptyList())
    val partidos: StateFlow<List<PartidoFirebase>> = _partidos
    private val _cargandoPartidos = MutableStateFlow(true)
    val cargandoPartidos: StateFlow<Boolean> = _cargandoPartidos
    private val _partidosConNombres = MutableStateFlow<List<PartidoConNombresOnline>>(emptyList())
    val partidosConNombres: StateFlow<List<PartidoConNombresOnline>> = _partidosConNombres

    fun cargarPartidos() {
        viewModelScope.launch {
            _partidos.value = partidoRepo.listarPartidosPorUsuario(usuarioUid)
        }
    }

    fun cargarPartidosConNombres() {
        viewModelScope.launch {
            _cargandoPartidos.value = true
            val partidos = partidoRepo.listarPartidosPorUsuario(usuarioUid)
            val equipos = mutableMapOf<String, EquipoFirebase>()
            val equipoUids = partidos.flatMap { listOf(it.equipoAId, it.equipoBId) }.distinct()
            equipoUids.forEach { uid ->
                if (uid.isNotBlank()) {
                    val eq = equipoRepo.obtenerEquipo(uid)
                    if (eq != null) equipos[uid] = eq
                }
            }

            val partidosNombres = partidos.map { partido ->
                val equipoA = equipos[partido.equipoAId]
                val equipoB = equipos[partido.equipoBId]
                PartidoConNombresOnline(
                    uid = partido.uid,
                    nombreEquipoA = equipoA?.nombre ?: "Equipo A",
                    nombreEquipoB = equipoB?.nombre ?: "Equipo B",
                    fecha = partido.fecha,
                    horaInicio = partido.horaInicio,
                    horaFin = calcularHoraFin(
                        partido.horaInicio,
                        partido.numeroPartes,
                        partido.tiempoPorParte,
                        partido.tiempoDescanso
                    )
                )
            }
            _partidosConNombres.value = partidosNombres
            _cargandoPartidos.value = false
        }
    }

    suspend fun buscarPartidoPorUid(uid: String): PartidoConNombresOnline? {
        val partido = partidoRepo.obtenerPartido(uid) ?: return null

        // Permite buscar SIEMPRE que esté en PREVIA, seas creador O NO TENGAS acceso (para poder añadirte)
        if (partido.estado != "PREVIA") return null

        // Si ya tienes acceso no pasa nada, pero NO bloquees si no lo tienes, porque justo quieres añadirte
        val equipoA = partido.equipoAId.takeIf { it.isNotBlank() }?.let { equipoRepo.obtenerEquipo(it) }
        val equipoB = partido.equipoBId.takeIf { it.isNotBlank() }?.let { equipoRepo.obtenerEquipo(it) }
        return PartidoConNombresOnline(
            uid = partido.uid,
            nombreEquipoA = equipoA?.nombre ?: "Equipo A",
            nombreEquipoB = equipoB?.nombre ?: "Equipo B",
            fecha = partido.fecha,
            horaInicio = partido.horaInicio,
            horaFin = calcularHoraFin(
                partido.horaInicio,
                partido.numeroPartes,
                partido.tiempoPorParte,
                partido.tiempoDescanso
            )
        )
    }

    fun agregarPartidoALista(partido: PartidoConNombresOnline) {
        viewModelScope.launch {
            partidoRepo.agregarUsuarioAAcceso(partido.uid, usuarioUid)
            cargarPartidosConNombres()
        }
    }

    fun agregarUsuarioAAccesoPartido(partidoUid: String) {
        viewModelScope.launch {
            partidoRepo.agregarUsuarioAAcceso(partidoUid, usuarioUid)
        }
    }

    private fun calcularHoraFin(
        horaInicio: String,
        numeroPartes: Int,
        tiempoPorParte: Int,
        tiempoDescanso: Int
    ): String {
        return try {
            val partesTotal = (numeroPartes * tiempoPorParte) + ((numeroPartes - 1) * tiempoDescanso)
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val inicio = LocalTime.parse(horaInicio, formatter)
            val fin = inicio.plusMinutes(partesTotal.toLong())
            fin.format(formatter)
        } catch (e: Exception) {
            ""
        }
    }

    fun agregarPartido(partido: PartidoFirebase) {
        viewModelScope.launch {
            partidoRepo.crearPartido(partido)
            cargarPartidosConNombres()
        }
    }

    fun eliminarPartido(uid: String) {
        viewModelScope.launch {
            partidoRepo.borrarPartido(uid)
            cargarPartidosConNombres()
        }
    }

    fun duplicarPartido(uid: String) {
        viewModelScope.launch {
            val partido = partidoRepo.obtenerPartido(uid)
            partido?.let {
                val nuevoPartido = it.copy(
                    uid = "",
                    creadorUid = usuarioUid,
                    usuariosConAcceso = listOf(usuarioUid)
                )
                partidoRepo.crearPartido(nuevoPartido)
            }
            cargarPartidosConNombres()
        }
    }
}

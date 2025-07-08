package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository

class CreatePartidoOnlineViewModel(
    private val partidoFirebaseRepository: PartidoFirebaseRepository,
    private val userUid: String // <<--- PASA EL UID AQUÍ!
) : ViewModel() {

    fun crearPartidoOnline(
        equipoA: String,
        equipoB: String,
        fecha: String,
        horaInicio: String,
        numeroPartes: Int,
        tiempoPorParte: Int,
        tiempoDescanso: Int,
        numeroJugadores: Int,
        isPublic: Boolean,
        onFinish: (String, String, String) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            val equipoAId = partidoFirebaseRepository.crearEquipo(EquipoFirebase(nombre = equipoA))
            val equipoBId = partidoFirebaseRepository.crearEquipo(EquipoFirebase(nombre = equipoB))
            val partido = PartidoFirebase(
                fecha = fecha,
                horaInicio = horaInicio,
                numeroPartes = numeroPartes,
                tiempoPorParte = tiempoPorParte,
                tiempoDescanso = tiempoDescanso,
                equipoAId = equipoAId,
                equipoBId = equipoBId,
                numeroJugadores = numeroJugadores,
                estado = "PREVIA",
                golesEquipoA = 0,
                golesEquipoB = 0,
                jugadoresEquipoA = emptyList(),
                jugadoresEquipoB = emptyList(),
                nombresManualEquipoA = emptyList(),
                nombresManualEquipoB = emptyList(),
                creadorUid = userUid, // AQUÍ SE PONE EL CREADOR
                isPublic = isPublic,
                usuariosConAcceso = listOf(userUid) // EL CREADOR TIENE ACCESO
            )
            val partidoUid = partidoFirebaseRepository.crearPartidoConRetornoUid(partido)
            onFinish(partidoUid, equipoAId, equipoBId)
        }
    }
}

package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository

class CreatePartidoOnlineViewModel(
    private val partidoFirebaseRepository: PartidoFirebaseRepository,
    private val userUid: String
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
                creadorUid = userUid,  // <- ESTE CAMPO SE USA Y SE GUARDA SIEMPRE
                isPublic = isPublic,
                usuariosConAcceso = listOf(userUid)  // <- ESTE CAMPO SE USA Y SE GUARDA SIEMPRE
            )
            val partidoUid = partidoFirebaseRepository.crearPartidoConRetornoUid(partido)
            onFinish(partidoUid, equipoAId, equipoBId)
        }
    }
}

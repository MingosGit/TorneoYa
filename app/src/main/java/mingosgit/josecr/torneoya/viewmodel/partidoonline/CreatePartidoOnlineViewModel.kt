package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.EquipoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebase
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository

class CreatePartidoOnlineViewModel(
    private val partidoFirebaseRepository: PartidoFirebaseRepository
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
            // 1. Crear equipos en Firestore (guarda y recupera sus UIDs)
            val equipoAId = partidoFirebaseRepository.crearEquipo(EquipoFirebase(nombre = equipoA))
            val equipoBId = partidoFirebaseRepository.crearEquipo(EquipoFirebase(nombre = equipoB))
            // 2. Crear partido en Firestore
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
                creadorUid = "", // Añade el UID correcto si tienes autenticación
                isPublic = isPublic
            )
            val partidoUid = partidoFirebaseRepository.crearPartidoConRetornoUid(partido)
            onFinish(partidoUid, equipoAId, equipoBId)
        }
    }
}

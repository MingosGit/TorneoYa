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

    // Método para crear un partido online con dos equipos, sus parámetros y guardarlo en Firebase
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
            // Se crean los equipos en Firebase y se obtienen sus IDs
            val equipoAId = partidoFirebaseRepository.crearEquipo(EquipoFirebase(nombre = equipoA))
            val equipoBId = partidoFirebaseRepository.crearEquipo(EquipoFirebase(nombre = equipoB))

            // Se construye el objeto PartidoFirebase con todos los datos necesarios
            val partido = PartidoFirebase(
                fecha = fecha,
                horaInicio = horaInicio,
                numeroPartes = numeroPartes,
                tiempoPorParte = tiempoPorParte,
                tiempoDescanso = tiempoDescanso,
                equipoAId = equipoAId,
                equipoBId = equipoBId,
                numeroJugadores = numeroJugadores,
                estado = "PREVIA", // Estado inicial del partido
                golesEquipoA = 0,
                golesEquipoB = 0,
                jugadoresEquipoA = emptyList(),
                jugadoresEquipoB = emptyList(),
                nombresManualEquipoA = emptyList(),
                nombresManualEquipoB = emptyList(),
                creadorUid = userUid, // Se guarda el creador
                isPublic = isPublic, // Define si el partido es público o no
                usuariosConAcceso = listOf(userUid) // Se asigna acceso al creador
            )

            // Se guarda el partido en Firebase y se obtiene su UID
            val partidoUid = partidoFirebaseRepository.crearPartidoConRetornoUid(partido)

            // Se ejecuta el callback con los identificadores creados
            onFinish(partidoUid, equipoAId, equipoBId)
        }
    }
}

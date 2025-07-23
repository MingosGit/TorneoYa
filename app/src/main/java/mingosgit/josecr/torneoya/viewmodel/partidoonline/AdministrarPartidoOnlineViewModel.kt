package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdministrarPartidoOnlineViewModel(
    private val partidoUid: String,
    private val repo: PartidoFirebaseRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _partido = MutableStateFlow<PartidoFirebase?>(null)
    val partido: StateFlow<PartidoFirebase?> = _partido.asStateFlow()

    private val _equipoA = MutableStateFlow<EquipoFirebase?>(null)
    val equipoA: StateFlow<EquipoFirebase?> = _equipoA.asStateFlow()

    private val _equipoB = MutableStateFlow<EquipoFirebase?>(null)
    val equipoB: StateFlow<EquipoFirebase?> = _equipoB.asStateFlow()

    private val _jugadoresA = MutableStateFlow<List<JugadorFirebase>>(emptyList())
    val jugadoresA: StateFlow<List<JugadorFirebase>> = _jugadoresA.asStateFlow()

    private val _jugadoresB = MutableStateFlow<List<JugadorFirebase>>(emptyList())
    val jugadoresB: StateFlow<List<JugadorFirebase>> = _jugadoresB.asStateFlow()

    private val _goles = MutableStateFlow<List<GoleadorFirebase>>(emptyList())
    val goles: StateFlow<List<GoleadorFirebase>> = _goles.asStateFlow()

    // Para edición de nombre de equipo
    private val _nombreEquipoAEditable = MutableStateFlow("")
    val nombreEquipoAEditable: StateFlow<String> = _nombreEquipoAEditable.asStateFlow()
    private val _nombreEquipoBEditable = MutableStateFlow("")
    val nombreEquipoBEditable: StateFlow<String> = _nombreEquipoBEditable.asStateFlow()

    fun setNombreEquipoAEditable(valor: String) {
        _nombreEquipoAEditable.value = valor
    }

    fun setNombreEquipoBEditable(valor: String) {
        _nombreEquipoBEditable.value = valor
    }

    fun recargarTodo() {
        viewModelScope.launch {
            _loading.value = true
            val p = repo.obtenerPartido(partidoUid)
            _partido.value = p
            _equipoA.value = p?.equipoAId?.let { repo.obtenerEquipo(it) }
            _equipoB.value = p?.equipoBId?.let { repo.obtenerEquipo(it) }
            val allJugadores = repo.obtenerJugadores()
            _jugadoresA.value = allJugadores.filter { p?.jugadoresEquipoA?.contains(it.uid) == true }
            _jugadoresB.value = allJugadores.filter { p?.jugadoresEquipoB?.contains(it.uid) == true }
            _goles.value = obtenerGoleadoresPartido(partidoUid)
            _nombreEquipoAEditable.value = _equipoA.value?.nombre ?: ""
            _nombreEquipoBEditable.value = _equipoB.value?.nombre ?: ""
            _loading.value = false
        }
    }

    private suspend fun obtenerGoleadoresPartido(partidoUid: String): List<GoleadorFirebase> {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val res = db.collection("goleadores")
            .whereEqualTo("partidoUid", partidoUid)
            .get().await()
        return res.documents.mapNotNull { doc ->
            val g = doc.toObject(GoleadorFirebase::class.java)
            g?.copy(uid = doc.id)
        }
    }

    fun agregarGol(
        equipoUid: String,
        jugadorUid: String,
        minuto: Int?,
        asistenciaUid: String?
    ) {
        viewModelScope.launch {
            val partido = _partido.value ?: return@launch
            val db = FirebaseFirestore.getInstance()
            val golMap = hashMapOf(
                "partidoUid" to partido.uid,
                "equipoUid" to equipoUid,
                "jugadorUid" to jugadorUid,
                "minuto" to minuto,
                "asistenciaJugadorUid" to asistenciaUid
            )
            val doc = db.collection("goleadores").document()
            golMap["uid"] = doc.id
            doc.set(golMap).await()

            actualizarMarcadorPartido()
            recargarTodo()
        }
    }

    fun borrarGol(gol: GoleadorFirebase) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            db.collection("goleadores").document(gol.uid).delete().await()
            actualizarMarcadorPartido()
            recargarTodo()
        }
    }

    private suspend fun actualizarMarcadorPartido() {
        val partido = _partido.value ?: return
        val db = FirebaseFirestore.getInstance()
        val golesA = db.collection("goleadores")
            .whereEqualTo("partidoUid", partido.uid)
            .whereEqualTo("equipoUid", partido.equipoAId)
            .get().await().size()
        val golesB = db.collection("goleadores")
            .whereEqualTo("partidoUid", partido.uid)
            .whereEqualTo("equipoUid", partido.equipoBId)
            .get().await().size()
        db.collection("partidos").document(partido.uid)
            .update(
                mapOf(
                    "golesEquipoA" to golesA,
                    "golesEquipoB" to golesB
                )
            ).await()
    }

    fun actualizarNombreEquipoA() {
        val equipo = _equipoA.value ?: return
        val nuevoNombre = _nombreEquipoAEditable.value
        viewModelScope.launch {
            if (nuevoNombre.isNotBlank()) {
                FirebaseFirestore.getInstance().collection("equipos")
                    .document(equipo.uid)
                    .update("nombre", nuevoNombre)
                    .await()
                recargarTodo()
            }
        }
    }

    fun actualizarNombreEquipoB() {
        val equipo = _equipoB.value ?: return
        val nuevoNombre = _nombreEquipoBEditable.value
        viewModelScope.launch {
            if (nuevoNombre.isNotBlank()) {
                FirebaseFirestore.getInstance().collection("equipos")
                    .document(equipo.uid)
                    .update("nombre", nuevoNombre)
                    .await()
                recargarTodo()
            }
        }
    }
}

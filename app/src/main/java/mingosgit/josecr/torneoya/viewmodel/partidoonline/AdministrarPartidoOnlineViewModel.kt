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

    private val _jugadoresManualA = MutableStateFlow<List<JugadorFirebase>>(emptyList())
    val jugadoresManualA: StateFlow<List<JugadorFirebase>> = _jugadoresManualA.asStateFlow()

    private val _jugadoresManualB = MutableStateFlow<List<JugadorFirebase>>(emptyList())
    val jugadoresManualB: StateFlow<List<JugadorFirebase>> = _jugadoresManualB.asStateFlow()

    private val _goles = MutableStateFlow<List<GoleadorFirebase>>(emptyList())
    val goles: StateFlow<List<GoleadorFirebase>> = _goles.asStateFlow()

    private val _nombreEquipoAEditable = MutableStateFlow("")
    val nombreEquipoAEditable: StateFlow<String> = _nombreEquipoAEditable.asStateFlow()
    private val _nombreEquipoBEditable = MutableStateFlow("")
    val nombreEquipoBEditable: StateFlow<String> = _nombreEquipoBEditable.asStateFlow()

    private val _fechaEditable = MutableStateFlow("")
    val fechaEditable: StateFlow<String> = _fechaEditable.asStateFlow()
    private val _horaEditable = MutableStateFlow("")
    val horaEditable: StateFlow<String> = _horaEditable.asStateFlow()
    private val _numeroPartesEditable = MutableStateFlow(2)
    val numeroPartesEditable: StateFlow<Int> = _numeroPartesEditable.asStateFlow()
    private val _tiempoPorParteEditable = MutableStateFlow(25)
    val tiempoPorParteEditable: StateFlow<Int> = _tiempoPorParteEditable.asStateFlow()
    private val _tiempoDescansoEditable = MutableStateFlow(5)
    val tiempoDescansoEditable: StateFlow<Int> = _tiempoDescansoEditable.asStateFlow()

    fun setNombreEquipoAEditable(valor: String) { _nombreEquipoAEditable.value = valor }
    fun setNombreEquipoBEditable(valor: String) { _nombreEquipoBEditable.value = valor }
    fun setFechaEditable(valor: String) { _fechaEditable.value = valor }
    fun setHoraEditable(valor: String) { _horaEditable.value = valor }
    fun setNumeroPartesEditable(valor: Int) { _numeroPartesEditable.value = valor }
    fun setTiempoPorParteEditable(valor: Int) { _tiempoPorParteEditable.value = valor }
    fun setTiempoDescansoEditable(valor: Int) { _tiempoDescansoEditable.value = valor }

    fun recargarTodo() {
        viewModelScope.launch {
            _loading.value = true
            val p = repo.obtenerPartido(partidoUid)
            _partido.value = p
            _equipoA.value = p?.equipoAId?.let { repo.obtenerEquipo(it) }
            _equipoB.value = p?.equipoBId?.let { repo.obtenerEquipo(it) }
            _jugadoresA.value = p?.jugadoresEquipoA?.let { getJugadoresByUidList(it) } ?: emptyList()
            _jugadoresB.value = p?.jugadoresEquipoB?.let { getJugadoresByUidList(it) } ?: emptyList()
            _jugadoresManualA.value = p?.nombresManualEquipoA?.map { nombre ->
                JugadorFirebase(uid = "", nombre = nombre, email = "", avatarUrl = null)
            } ?: emptyList()
            _jugadoresManualB.value = p?.nombresManualEquipoB?.map { nombre ->
                JugadorFirebase(uid = "", nombre = nombre, email = "", avatarUrl = null)
            } ?: emptyList()
            _goles.value = obtenerGoleadoresPartido(partidoUid)
            _nombreEquipoAEditable.value = _equipoA.value?.nombre ?: ""
            _nombreEquipoBEditable.value = _equipoB.value?.nombre ?: ""
            _fechaEditable.value = p?.fecha ?: ""
            _horaEditable.value = p?.horaInicio ?: ""
            _numeroPartesEditable.value = p?.numeroPartes ?: 2
            _tiempoPorParteEditable.value = p?.tiempoPorParte ?: 25
            _tiempoDescansoEditable.value = p?.tiempoDescanso ?: 5
            _loading.value = false
        }
    }

    private suspend fun getJugadoresByUidList(uids: List<String>): List<JugadorFirebase> {
        val db = FirebaseFirestore.getInstance()
        val jugadores = mutableListOf<JugadorFirebase>()
        for (uid in uids.map { it.trim() }) {
            val snapJugador = db.collection("jugadores").document(uid).get().await()
            val jugador = snapJugador.toObject(JugadorFirebase::class.java)?.copy(uid = uid)
            if (jugador != null && jugador.nombre.isNotBlank()) {
                jugadores.add(jugador)
                continue
            }
            val snapUsuario = db.collection("usuarios").document(uid).get().await()
            val nombreUsuario = snapUsuario.getString("nombreUsuario")
            val email = snapUsuario.getString("email") ?: ""
            val avatarUrl = snapUsuario.getString("avatarUrl")
            if (!nombreUsuario.isNullOrBlank()) {
                jugadores.add(
                    JugadorFirebase(
                        uid = uid,
                        nombre = nombreUsuario,
                        email = email,
                        avatarUrl = avatarUrl
                    )
                )
            }
        }
        return jugadores
    }

    private suspend fun obtenerGoleadoresPartido(partidoUid: String): List<GoleadorFirebase> {
        val db = FirebaseFirestore.getInstance()
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
        asistenciaUid: String?,
        jugadorNombreManual: String? = null,
        asistenciaNombreManual: String? = null
    ) {
        viewModelScope.launch {
            val partido = _partido.value ?: return@launch
            val db = FirebaseFirestore.getInstance()
            val golMap = hashMapOf(
                "partidoUid" to partido.uid,
                "equipoUid" to equipoUid,
                "jugadorUid" to jugadorUid,
                "minuto" to minuto,
                "asistenciaJugadorUid" to asistenciaUid,
                "jugadorNombreManual" to jugadorNombreManual,
                "asistenciaNombreManual" to asistenciaNombreManual
            )
            val doc = db.collection("goleadores").document()
            golMap["uid"] = doc.id
            doc.set(golMap).await()
            actualizarMarcadorPartido()
            recargarTodo()
        }
    }

    fun agregarGolManual(
        equipoUid: String,
        nombreJugadorManual: String,
        minuto: Int?,
        nombreAsistenteManual: String?
    ) {
        viewModelScope.launch {
            val partido = _partido.value ?: return@launch
            val db = FirebaseFirestore.getInstance()
            val golMap = hashMapOf(
                "partidoUid" to partido.uid,
                "equipoUid" to equipoUid,
                "jugadorUid" to "",
                "jugadorManual" to nombreJugadorManual,
                "minuto" to minuto,
                "asistenciaJugadorUid" to "",
                "asistenciaManual" to (nombreAsistenteManual ?: "")
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

    fun actualizarDatosPartido() {
        val fecha = _fechaEditable.value.trim()
        val hora = _horaEditable.value.trim()
        val numPartes = _numeroPartesEditable.value
        val tiempoPorParte = _tiempoPorParteEditable.value
        val tiempoDescanso = _tiempoDescansoEditable.value

        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("partidos")
                .document(partidoUid)
                .update(
                    mapOf(
                        "fecha" to fecha,
                        "horaInicio" to hora,
                        "numeroPartes" to numPartes,
                        "tiempoPorParte" to tiempoPorParte,
                        "tiempoDescanso" to tiempoDescanso
                    )
                ).await()
            recargarTodo()
        }
    }
}

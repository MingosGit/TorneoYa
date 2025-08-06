package mingosgit.josecr.torneoya.viewmodel.usuario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.repository.UsuarioAuthRepository

class MiCuentaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val partidoRepo = PartidoFirebaseRepository()
    private val usuarioAuthRepo = UsuarioAuthRepository()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario

    private val _confirmarCerrarSesion = MutableStateFlow(false)
    val confirmarCerrarSesion: StateFlow<Boolean> = _confirmarCerrarSesion

    private val _confirmarEliminarCuenta = MutableStateFlow(false)
    val confirmarEliminarCuenta: StateFlow<Boolean> = _confirmarEliminarCuenta

    private val _errorCambioNombre = MutableStateFlow<String?>(null)
    val errorCambioNombre: StateFlow<String?> = _errorCambioNombre

    private val _cambioNombreExitoso = MutableStateFlow(false)
    val cambioNombreExitoso: StateFlow<Boolean> = _cambioNombreExitoso

    // ----------- NUEVO: PASSWORD RESET ---------------
    private val _showMensajeReset = MutableStateFlow(false)
    val showMensajeReset: StateFlow<Boolean> = _showMensajeReset

    private val _resetTimer = MutableStateFlow(0)
    val resetTimer: StateFlow<Int> = _resetTimer

    private var timerJob: Job? = null

    fun cargarDatos() {
        val user = auth.currentUser ?: return
        _email.value = user.email ?: ""
        viewModelScope.launch {
            val snap = firestore.collection("usuarios").document(user.uid).get().await()
            _nombreUsuario.value = snap.getString("nombreUsuario") ?: ""
        }
    }

    fun cambiarNombreUsuario(nuevoNombre: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            if (!usuarioAuthRepo.isNombreUsuarioDisponible(nuevoNombre)) {
                _errorCambioNombre.value = "El nombre de usuario ya está en uso"
                _cambioNombreExitoso.value = false
                return@launch
            }
            firestore.collection("usuarios").document(uid)
                .update("nombreUsuario", nuevoNombre).await()
            _nombreUsuario.value = nuevoNombre
            _errorCambioNombre.value = null
            _cambioNombreExitoso.value = true
        }
    }

    fun resetErrorCambioNombre() {
        _errorCambioNombre.value = null
    }

    fun resetCambioNombreExitoso() {
        _cambioNombreExitoso.value = false
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun confirmarCerrarSesionDialog(show: Boolean) {
        _confirmarCerrarSesion.value = show
    }

    fun confirmarEliminarCuentaDialog(show: Boolean) {
        _confirmarEliminarCuenta.value = show
    }

    fun eliminarCuentaYDatos() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            try {
                // 1. Eliminar de amigos de otros usuarios
                val usuariosSnapshot = firestore.collection("usuarios").get().await()
                for (usuarioDoc in usuariosSnapshot.documents) {
                    firestore.collection("usuarios").document(usuarioDoc.id)
                        .collection("amigos").whereEqualTo("uid", uid)
                        .get().await().documents.forEach {
                            firestore.collection("usuarios").document(usuarioDoc.id)
                                .collection("amigos").document(it.id).delete().await()
                        }
                }

                // 2. Eliminar de equipos
                val equiposSnapshot = firestore.collection("equipos").get().await()
                for (equipoDoc in equiposSnapshot.documents) {
                    firestore.collection("equipos").document(equipoDoc.id)
                        .collection("jugadores").whereEqualTo("uid", uid)
                        .get().await().documents.forEach {
                            firestore.collection("equipos").document(equipoDoc.id)
                                .collection("jugadores").document(it.id).delete().await()
                        }
                }

                // 3. Eliminar de arrays de partidos y referencias como admin, acceso, etc
                val partidosSnapshot = firestore.collection("partidos").get().await()
                for (partidoDoc in partidosSnapshot.documents) {
                    val partidoRef = firestore.collection("partidos").document(partidoDoc.id)
                    val datos = partidoDoc.data ?: continue
                    val jugadoresA = (datos["jugadoresEquipoA"] as? List<*>)?.filter { it != uid } ?: emptyList<String>()
                    val jugadoresB = (datos["jugadoresEquipoB"] as? List<*>)?.filter { it != uid } ?: emptyList<String>()
                    val administradores = (datos["administradores"] as? List<*>)?.filter { it != uid } ?: emptyList<String>()
                    val usuariosConAcceso = (datos["usuariosConAcceso"] as? List<*>)?.filter { it != uid } ?: emptyList<String>()
                    val creadorUid = datos["creadorUid"] as? String

                    val updates = mutableMapOf<String, Any>(
                        "jugadoresEquipoA" to jugadoresA,
                        "jugadoresEquipoB" to jugadoresB,
                        "administradores" to administradores,
                        "usuariosConAcceso" to usuariosConAcceso
                    )
                    if (creadorUid == uid) {
                        updates["creadorUid"] = ""
                    }
                    partidoRef.update(updates).await()
                }

                // 4. BORRAR PARTIDOS DONDE EL USUARIO ES CREADOR (Y TODOS SUS DATOS RELACIONADOS)
                val partidosCreados = firestore.collection("partidos")
                    .whereEqualTo("creadorUid", uid)
                    .get().await()

                for (partidoDoc in partidosCreados.documents) {
                    val partidoUid = partidoDoc.id

                    // Borra comentarios del partido
                    firestore.collection("comentarios")
                        .whereEqualTo("partidoUid", partidoUid)
                        .get().await().documents.forEach {
                            firestore.collection("comentarios").document(it.id).delete().await()
                        }

                    // Borra encuestas del partido
                    firestore.collection("encuestas")
                        .whereEqualTo("partidoUid", partidoUid)
                        .get().await().documents.forEach {
                            firestore.collection("encuestas").document(it.id).delete().await()
                        }

                    // Borra votos de encuestas del partido
                    firestore.collection("encuesta_votos")
                        .whereEqualTo("partidoUid", partidoUid)
                        .get().await().documents.forEach {
                            firestore.collection("encuesta_votos").document(it.id).delete().await()
                        }

                    // Borra goles del partido
                    firestore.collection("goleadores")
                        .whereEqualTo("partidoUid", partidoUid)
                        .get().await().documents.forEach {
                            firestore.collection("goleadores").document(it.id).delete().await()
                        }

                    // Borra eventos del partido
                    firestore.collection("eventos")
                        .whereEqualTo("partidoUid", partidoUid)
                        .get().await().documents.forEach {
                            firestore.collection("eventos").document(it.id).delete().await()
                        }

                    // Finalmente, borra el partido
                    firestore.collection("partidos").document(partidoUid).delete().await()
                }

                // 5. Eliminar goles y eventos del usuario
                firestore.collection("goleadores")
                    .whereEqualTo("jugadorUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("goleadores").document(it.id).delete().await()
                    }
                firestore.collection("goleadores")
                    .whereEqualTo("asistenciaJugadorUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("goleadores").document(it.id).delete().await()
                    }
                firestore.collection("eventos")
                    .whereEqualTo("jugadorUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("eventos").document(it.id).delete().await()
                    }
                firestore.collection("eventos")
                    .whereEqualTo("asistenteUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("eventos").document(it.id).delete().await()
                    }

                // 6. Eliminar notificaciones
                firestore.collection("notificaciones")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("notificaciones").document(it.id).delete().await()
                    }

                // 7. Eliminar encuestas creadas por el usuario
                firestore.collection("encuestas")
                    .whereEqualTo("creadorUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("encuestas").document(it.id).delete().await()
                    }

                // 8. Eliminar comentarios del usuario
                firestore.collection("comentarios")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("comentarios").document(it.id).delete().await()
                    }

                // 9. Eliminar votos del usuario
                firestore.collection("comentario_votos")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("comentario_votos").document(it.id).delete().await()
                    }

                // 10. Eliminar votos de encuestas del usuario
                firestore.collection("encuesta_votos")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        firestore.collection("encuesta_votos").document(it.id).delete().await()
                    }

                // 11. Eliminar usuario de la colección usuarios
                firestore.collection("usuarios").document(uid).delete().await()

                // 12. Eliminar usuario de FirebaseAuth
                user.delete().await()

            } catch (e: Exception) {
                // Manejo de error simple (log, puedes adaptar a tu app)
            }
        }
    }

    // --------------- RESTABLECER CONTRASEÑA -------------------
    fun enviarCorreoResetPassword() {
        val correo = _email.value
        if (correo.isBlank() || _resetTimer.value > 0) return

        viewModelScope.launch {
            usuarioAuthRepo.enviarCorreoRestablecerPassword(correo)
            _showMensajeReset.value = true
            startResetTimer()
        }
    }

    private fun startResetTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _resetTimer.value = 60
            while (_resetTimer.value > 0) {
                delay(1000L)
                _resetTimer.value = _resetTimer.value - 1
            }
            _showMensajeReset.value = false
        }
    }
}

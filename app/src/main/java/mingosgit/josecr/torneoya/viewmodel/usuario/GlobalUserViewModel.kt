package mingosgit.josecr.torneoya.viewmodel.usuario

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GlobalUserViewModel(app: Application) : AndroidViewModel(app) {

    private val _nombreUsuarioOnline = MutableStateFlow<String?>(null)
    val nombreUsuarioOnline: StateFlow<String?> = _nombreUsuarioOnline

    private val _sesionOnlineActiva = MutableStateFlow(false)
    val sesionOnlineActiva: StateFlow<Boolean> = _sesionOnlineActiva

    private val _goles = MutableStateFlow<Int?>(null)
    val goles: StateFlow<Int?> = _goles

    private val _asistencias = MutableStateFlow<Int?>(null)
    val asistencias: StateFlow<Int?> = _asistencias

    private val _partidosJugados = MutableStateFlow<Int?>(null)
    val partidosJugados: StateFlow<Int?> = _partidosJugados

    private val _promedioGoles = MutableStateFlow<Double?>(null)
    val promedioGoles: StateFlow<Double?> = _promedioGoles

    private val _avatar = MutableStateFlow<Int?>(null)
    val avatar: StateFlow<Int?> = _avatar

    fun setNombreUsuarioOnline(nombre: String) {
        _nombreUsuarioOnline.value = nombre
    }

    fun cargarNombreUsuarioOnlineSiSesionActiva() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.isEmailVerified) {
            _sesionOnlineActiva.value = true
            viewModelScope.launch {
                val db = FirebaseFirestore.getInstance()
                val usuarioSnap = db.collection("usuarios").document(user.uid).get().await()
                val nombreUsuario = usuarioSnap.getString("nombreUsuario")
                _nombreUsuarioOnline.value = nombreUsuario
                _avatar.value = usuarioSnap.getLong("avatar")?.toInt()
                cargarEstadisticasUsuario(user.uid)
            }
        } else {
            _nombreUsuarioOnline.value = null
            _sesionOnlineActiva.value = false
            _goles.value = null
            _asistencias.value = null
            _partidosJugados.value = null
            _promedioGoles.value = null
            _avatar.value = null
        }
    }

    fun cargarEstadisticasUsuario(uid: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            // Goles
            val golesSnapshot = db.collection("goleadores")
                .whereEqualTo("jugadorUid", uid)
                .get().await()
            val golesCount = golesSnapshot.size()
            // Asistencias
            val asistenciasSnapshot = db.collection("goleadores")
                .whereEqualTo("asistenciaJugadorUid", uid)
                .get().await()
            val asistenciasCount = asistenciasSnapshot.size()
            // Partidos jugados: leer el contador del usuario
            val usuarioSnap = db.collection("usuarios").document(uid).get().await()
            val partidosJugados = usuarioSnap.getLong("partidosJugados")?.toInt() ?: 0
            val promedio = if (partidosJugados > 0) golesCount.toDouble() / partidosJugados.toDouble() else 0.0

            _goles.value = golesCount
            _asistencias.value = asistenciasCount
            _partidosJugados.value = partidosJugados
            _promedioGoles.value = promedio
        }
    }

    fun cambiarAvatarEnFirebase(avatar: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("usuarios").document(uid)
                    .update("avatar", avatar)
                    .await()
                _avatar.value = avatar
            } catch (e: Exception) {
                // Maneja el error si lo necesitas
            }
        }
    }

    fun cerrarSesionOnline() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            _nombreUsuarioOnline.value = null
            _sesionOnlineActiva.value = false
            _goles.value = null
            _asistencias.value = null
            _partidosJugados.value = null
            _promedioGoles.value = null
            _avatar.value = null
            delay(200)
            reiniciarApp()
        }
    }

    internal fun reiniciarApp() {
        val context = getApplication<Application>().applicationContext
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    fun eliminarCuentaYDatosDelUsuario(
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                // Eliminar usuario de colección "usuarios"
                db.collection("usuarios").document(uid).delete().await()

                // Eliminar de amigos de otros usuarios
                val usuariosSnapshot = db.collection("usuarios").get().await()
                for (usuarioDoc in usuariosSnapshot.documents) {
                    db.collection("usuarios").document(usuarioDoc.id)
                        .collection("amigos").whereEqualTo("uid", uid)
                        .get().await().documents.forEach {
                            db.collection("usuarios").document(usuarioDoc.id)
                                .collection("amigos").document(it.id).delete()
                        }
                }

                // Eliminar comentarios del usuario
                db.collection("comentarios")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        db.collection("comentarios").document(it.id).delete()
                    }

                // Eliminar votos del usuario
                db.collection("comentariosVotos")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        db.collection("comentariosVotos").document(it.id).delete()
                    }

                // Eliminar notificaciones del usuario
                db.collection("notificaciones")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        db.collection("notificaciones").document(it.id).delete()
                    }

                // Eliminar del equipo si existe una colección equipos/jugadores
                val equiposSnapshot = db.collection("equipos").get().await()
                for (equipoDoc in equiposSnapshot.documents) {
                    db.collection("equipos").document(equipoDoc.id)
                        .collection("jugadores").whereEqualTo("uid", uid)
                        .get().await().documents.forEach {
                            db.collection("equipos").document(equipoDoc.id)
                                .collection("jugadores").document(it.id).delete()
                        }
                }

                // Eliminar de partidos: jugadoresEquipoA, jugadoresEquipoB, administradores, usuariosConAcceso
                val partidosSnapshot = db.collection("partidos").get().await()
                for (partidoDoc in partidosSnapshot.documents) {
                    val partidoRef = db.collection("partidos").document(partidoDoc.id)
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

                // Eliminar goles y eventos del usuario
                db.collection("goleadores")
                    .whereEqualTo("jugadorUid", uid)
                    .get().await().documents.forEach {
                        db.collection("goleadores").document(it.id).delete()
                    }
                db.collection("goleadores")
                    .whereEqualTo("asistenciaJugadorUid", uid)
                    .get().await().documents.forEach {
                        db.collection("goleadores").document(it.id).delete()
                    }

                db.collection("eventos")
                    .whereEqualTo("jugadorUid", uid)
                    .get().await().documents.forEach {
                        db.collection("eventos").document(it.id).delete()
                    }
                db.collection("eventos")
                    .whereEqualTo("asistenteUid", uid)
                    .get().await().documents.forEach {
                        db.collection("eventos").document(it.id).delete()
                    }

                // Eliminar encuestas creadas por el usuario
                db.collection("encuestas")
                    .whereEqualTo("creadorUid", uid)
                    .get().await().documents.forEach {
                        db.collection("encuestas").document(it.id).delete()
                    }

                // Eliminar usuario de autenticación
                user.delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSuccess?.invoke()
                        cerrarSesionOnline()
                    } else {
                        onError?.invoke(it.exception ?: Exception("Error al eliminar usuario"))
                    }
                }

            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }
}

package mingosgit.josecr.torneoya.viewmodel.usuario

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.auth.AuthManager
import mingosgit.josecr.torneoya.data.session.SessionStore

// ViewModel global del usuario: expone perfil, sesión y estadísticas
class GlobalUserViewModel(app: Application) : AndroidViewModel(app) {

    // Gestor de autenticación y sesión local
    private val authManager = AuthManager(app)
    private val sessionStore = SessionStore(app.applicationContext)

    // Estado: nombre visible del usuario online (nullable si no hay)
    private val _nombreUsuarioOnline = MutableStateFlow<String?>(null)
    val nombreUsuarioOnline: StateFlow<String?> = _nombreUsuarioOnline.asStateFlow()

    // Estado: si la sesión online/caché está activa
    private val _sesionOnlineActiva = MutableStateFlow(false)
    val sesionOnlineActiva: StateFlow<Boolean> = _sesionOnlineActiva.asStateFlow()

    // Estado: estadísticas básicas
    private val _goles = MutableStateFlow<Int?>(null)
    val goles: StateFlow<Int?> = _goles.asStateFlow()

    private val _asistencias = MutableStateFlow<Int?>(null)
    val asistencias: StateFlow<Int?> = _asistencias.asStateFlow()

    private val _partidosJugados = MutableStateFlow<Int?>(null)
    val partidosJugados: StateFlow<Int?> = _partidosJugados.asStateFlow()

    private val _promedioGoles = MutableStateFlow<Double?>(null)
    val promedioGoles: StateFlow<Double?> = _promedioGoles.asStateFlow()

    // Estado: avatar seleccionado (id de recurso o similar)
    private val _avatar = MutableStateFlow<Int?>(null)
    val avatar: StateFlow<Int?> = _avatar.asStateFlow()

    // Se suscribe al estado de autenticación y actualiza UI; intenta refrescar con red
    init {
        // Observa el estado de autenticación y ajusta banderas sin cerrar sesión por estar offline.
        viewModelScope.launch {
            authManager.state.collect { state ->
                when (state) {
                    is mingosgit.josecr.torneoya.data.auth.AuthState.SignedOut -> {
                        resetEstado()
                    }
                    is mingosgit.josecr.torneoya.data.auth.AuthState.SignedInCached -> {
                        _sesionOnlineActiva.value = true
                        _nombreUsuarioOnline.value = state.session.nombreUsuario.ifBlank { null }
                        _avatar.value = state.session.avatar
                    }
                    is mingosgit.josecr.torneoya.data.auth.AuthState.SignedInOnline -> {
                        _sesionOnlineActiva.value = true
                        _nombreUsuarioOnline.value = state.session.nombreUsuario.ifBlank { null }
                        _avatar.value = state.session.avatar
                        // Con red, refrescamos perfil desde Firestore (sin romper si falla)
                        cargarPerfilYStatsRemoto()
                    }
                    is mingosgit.josecr.torneoya.data.auth.AuthState.SignedInNeedsAttention -> {
                        _sesionOnlineActiva.value = true
                        _nombreUsuarioOnline.value = state.session.nombreUsuario.ifBlank { null }
                        _avatar.value = state.session.avatar
                        // Podrías mostrar banner en UI, aquí no cerramos sesión.
                    }
                }
            }
        }
    }

    // Cambia el nombre mostrado y actualiza la caché local
    fun setNombreUsuarioOnline(nombre: String) {
        _nombreUsuarioOnline.value = nombre
        viewModelScope.launch { authManager.updateProfileCache(nombreUsuario = nombre, avatar = _avatar.value) }
    }

    // Dispara un refresco remoto de perfil/estadísticas si hay red
    fun cargarNombreUsuarioOnlineSiSesionActiva() {
        // Mantener compatibilidad con la llamada existente: ahora solo dispara refresh remoto si hay red.
        viewModelScope.launch { cargarPerfilYStatsRemoto() }
    }

    // Lee perfil y estadísticas del usuario en Firestore y actualiza estados
    private suspend fun cargarPerfilYStatsRemoto() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        try {
            val usuarioSnap = db.collection("usuarios").document(user.uid).get().await()
            val nombreUsuario = usuarioSnap.getString("nombreUsuario")
            val avatar = usuarioSnap.getLong("avatar")?.toInt()
            _nombreUsuarioOnline.value = nombreUsuario
            _avatar.value = avatar
            authManager.updateProfileCache(nombreUsuario = nombreUsuario, avatar = avatar)

            // Stats: cuenta goles, asistencias y calcula promedio por partido
            val golesSnapshot = db.collection("goleadores")
                .whereEqualTo("jugadorUid", user.uid)
                .get().await()
            val asistenciasSnapshot = db.collection("goleadores")
                .whereEqualTo("asistenciaJugadorUid", user.uid)
                .get().await()
            val partidosJugados = usuarioSnap.getLong("partidosJugados")?.toInt() ?: 0
            val golesCount = golesSnapshot.size()
            val asistenciasCount = asistenciasSnapshot.size()
            val promedio = if (partidosJugados > 0) golesCount.toDouble() / partidosJugados else 0.0

            _goles.value = golesCount
            _asistencias.value = asistenciasCount
            _partidosJugados.value = partidosJugados
            _promedioGoles.value = promedio
        } catch (_: Exception) {
            // No romper por offline.
        }
    }

    // Limpia todo el estado del usuario en memoria
    private fun resetEstado() {
        _nombreUsuarioOnline.value = null
        _sesionOnlineActiva.value = false
        _goles.value = null
        _asistencias.value = null
        _partidosJugados.value = null
        _promedioGoles.value = null
        _avatar.value = null
    }

    // Actualiza avatar en Firestore y sincroniza caché
    fun cambiarAvatarEnFirebase(avatar: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                db.collection("usuarios").document(uid).update("avatar", avatar).await()
                _avatar.value = avatar
                authManager.updateProfileCache(nombreUsuario = _nombreUsuarioOnline.value, avatar = avatar)
            } catch (_: Exception) {
            }
        }
    }

    // Cierra sesión solo en local y reinicia la app
    fun cerrarSesionOnline() {
        viewModelScope.launch {
            authManager.signOutLocalOnly()
            reiniciarApp()
        }
    }

    // Reinicia el proceso lanzando la actividad principal y saliendo
    internal fun reiniciarApp() {
        val context = getApplication<Application>().applicationContext
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    // Borra la cuenta y todos sus datos relacionados en Firestore; cierra sesión local al terminar
    fun eliminarCuentaYDatosDelUsuario(
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        // Conserva tu implementación original aquí si la usas. No cambia el comportamiento offline.
        // Mantengo llamado al cierre local tras borrar, usando authManager.
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                // Perfil del usuario
                db.collection("usuarios").document(uid).delete().await()

                // Quita al usuario de la lista de amigos de otros
                val usuariosSnapshot = db.collection("usuarios").get().await()
                for (usuarioDoc in usuariosSnapshot.documents) {
                    db.collection("usuarios").document(usuarioDoc.id)
                        .collection("amigos").whereEqualTo("uid", uid)
                        .get().await().documents.forEach {
                            db.collection("usuarios").document(usuarioDoc.id)
                                .collection("amigos").document(it.id).delete()
                        }
                }

                // Comentarios y votos
                db.collection("comentarios")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        db.collection("comentarios").document(it.id).delete()
                    }

                db.collection("comentariosVotos")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        db.collection("comentariosVotos").document(it.id).delete()
                    }

                // Notificaciones
                db.collection("notificaciones")
                    .whereEqualTo("usuarioUid", uid)
                    .get().await().documents.forEach {
                        db.collection("notificaciones").document(it.id).delete()
                    }

                // Eliminar del roster de equipos
                val equiposSnapshot = db.collection("equipos").get().await()
                for (equipoDoc in equiposSnapshot.documents) {
                    db.collection("equipos").document(equipoDoc.id)
                        .collection("jugadores").whereEqualTo("uid", uid)
                        .get().await().documents.forEach {
                            db.collection("equipos").document(equipoDoc.id)
                                .collection("jugadores").document(it.id).delete()
                        }
                }

                // Limpieza de partidos: listas y creador
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

                // Goleadores y eventos relacionados
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

                // Encuestas creadas
                db.collection("encuestas")
                    .whereEqualTo("creadorUid", uid)
                    .get().await().documents.forEach {
                        db.collection("encuestas").document(it.id).delete()
                    }

                // Elimina cuenta de FirebaseAuth y cierra sesión local
                user.delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSuccess?.invoke()
                        viewModelScope.launch { authManager.signOutLocalOnly() }
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

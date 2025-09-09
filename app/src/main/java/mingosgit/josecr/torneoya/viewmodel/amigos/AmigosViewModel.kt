package mingosgit.josecr.torneoya.viewmodel.amigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mingosgit.josecr.torneoya.data.firebase.AmigoFirebaseEntity
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity
import mingosgit.josecr.torneoya.repository.AmigosRepository

// DTO de amigo con posible avatar cargado desde Firestore
data class AmigoConAvatar(
    val uid: String,
    val nombreUsuario: String,
    val avatar: Int?
)

// DTO de usuario (solicitud) con posible avatar
data class UsuarioConAvatar(
    val uid: String,
    val nombreUsuario: String,
    val avatar: Int?
)

// ViewModel que gestiona amigos y solicitudes
class AmigosViewModel(
    private val repo: AmigosRepository = AmigosRepository(),                 // Repositorio de amigos
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()              // Auth para obtener mi uid
) : ViewModel() {
    // Estado: lista de amigos con avatar
    private val _amigos = MutableStateFlow<List<AmigoConAvatar>>(emptyList())
    val amigos: StateFlow<List<AmigoConAvatar>> = _amigos

    // Estado: lista de solicitudes con avatar
    private val _solicitudes = MutableStateFlow<List<UsuarioConAvatar>>(emptyList())
    val solicitudes: StateFlow<List<UsuarioConAvatar>> = _solicitudes

    // Estado: mensaje informativo para la UI
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // Acceso rápido a mi uid actual (cadena vacía si no hay sesión)
    val miUid: String get() = auth.currentUser?.uid.orEmpty()

    init {
        cargarAmigosYSolicitudes()                                           // Carga inicial de datos
    }

    // Carga amigos y solicitudes desde repo y enriquece con avatar/nombre desde Firestore
    fun cargarAmigosYSolicitudes() {
        val uid = miUid
        if (uid.isBlank()) return                                            // Si no hay uid, no hace nada
        viewModelScope.launch {
            // Amigos
            val amigosRaw = repo.getAmigos(uid)                              // Pide lista básica al repo
            val db = FirebaseFirestore.getInstance()
            val amigosConAvatar = amigosRaw.map { amigo ->
                var avatar: Int? = null
                var nombre = amigo.nombreUsuario
                try {
                    val snap = db.collection("usuarios").document(amigo.uid).get().await()
                    avatar = snap.getLong("avatar")?.toInt()                 // Lee avatar (si existe)
                    nombre = snap.getString("nombreUsuario") ?: nombre       // Refresca nombre (si existe)
                } catch (_: Exception) { }
                AmigoConAvatar(
                    uid = amigo.uid,
                    nombreUsuario = nombre,
                    avatar = avatar
                )
            }
            _amigos.value = amigosConAvatar                                  // Publica en el estado

            // Solicitudes
            val solicitudesRaw = repo.getSolicitudes(uid)                    // Pide solicitudes al repo
            val solicitudesConAvatar = solicitudesRaw.map { solicitud ->
                var avatar: Int? = null
                var nombre = solicitud.nombreUsuario
                try {
                    val snap = db.collection("usuarios").document(solicitud.uid).get().await()
                    avatar = snap.getLong("avatar")?.toInt()                 // Lee avatar (si existe)
                    nombre = snap.getString("nombreUsuario") ?: nombre       // Refresca nombre (si existe)
                } catch (_: Exception) { }
                UsuarioConAvatar(
                    uid = solicitud.uid,
                    nombreUsuario = nombre,
                    avatar = avatar
                )
            }
            _solicitudes.value = solicitudesConAvatar                        // Publica en el estado
        }
    }

    // Acepta una solicitud y recarga listas; además escribe mensaje de confirmación
    fun aceptarSolicitud(usuario: UsuarioFirebaseEntity) {
        val uid = miUid
        viewModelScope.launch {
            repo.aceptarSolicitud(uid, usuario)
            _mensaje.value = "¡Ahora sois amigos!"
            cargarAmigosYSolicitudes()
        }
    }

    // Rechaza una solicitud por uid y recarga; escribe mensaje informativo
    fun rechazarSolicitud(amigoUid: String) {
        val uid = miUid
        viewModelScope.launch {
            repo.rechazarSolicitud(uid, amigoUid)
            _mensaje.value = "Solicitud rechazada"
            cargarAmigosYSolicitudes()
        }
    }

    // Elimina un amigo por uid y recarga; escribe mensaje informativo
    fun eliminarAmigo(amigoUid: String) {
        val uid = miUid
        viewModelScope.launch {
            repo.eliminarAmigo(uid, amigoUid)
            _mensaje.value = "Amigo eliminado"
            cargarAmigosYSolicitudes()
        }
    }

    // Factory para crear el ViewModel sin parámetros externos
    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        // Crea una instancia del ViewModel para el provider
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AmigosViewModel() as T
        }
    }
}

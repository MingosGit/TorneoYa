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

data class AmigoConAvatar(
    val uid: String,
    val nombreUsuario: String,
    val avatar: Int?
)

data class UsuarioConAvatar(
    val uid: String,
    val nombreUsuario: String,
    val avatar: Int?
)

class AmigosViewModel(
    private val repo: AmigosRepository = AmigosRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _amigos = MutableStateFlow<List<AmigoConAvatar>>(emptyList())
    val amigos: StateFlow<List<AmigoConAvatar>> = _amigos

    private val _solicitudes = MutableStateFlow<List<UsuarioConAvatar>>(emptyList())
    val solicitudes: StateFlow<List<UsuarioConAvatar>> = _solicitudes

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    val miUid: String get() = auth.currentUser?.uid.orEmpty()

    init {
        cargarAmigosYSolicitudes()
    }

    fun cargarAmigosYSolicitudes() {
        val uid = miUid
        if (uid.isBlank()) return
        viewModelScope.launch {
            // Amigos
            val amigosRaw = repo.getAmigos(uid)
            val db = FirebaseFirestore.getInstance()
            val amigosConAvatar = amigosRaw.map { amigo ->
                var avatar: Int? = null
                var nombre = amigo.nombreUsuario
                try {
                    val snap = db.collection("usuarios").document(amigo.uid).get().await()
                    avatar = snap.getLong("avatar")?.toInt()
                    nombre = snap.getString("nombreUsuario") ?: nombre
                } catch (_: Exception) { }
                AmigoConAvatar(
                    uid = amigo.uid,
                    nombreUsuario = nombre,
                    avatar = avatar
                )
            }
            _amigos.value = amigosConAvatar

            // Solicitudes: añadir avatar
            val solicitudesRaw = repo.getSolicitudes(uid)
            val solicitudesConAvatar = solicitudesRaw.map { solicitud ->
                var avatar: Int? = null
                var nombre = solicitud.nombreUsuario
                try {
                    val snap = db.collection("usuarios").document(solicitud.uid).get().await()
                    avatar = snap.getLong("avatar")?.toInt()
                    nombre = snap.getString("nombreUsuario") ?: nombre
                } catch (_: Exception) { }
                UsuarioConAvatar(
                    uid = solicitud.uid,
                    nombreUsuario = nombre,
                    avatar = avatar
                )
            }
            _solicitudes.value = solicitudesConAvatar
        }
    }
    fun aceptarSolicitud(usuario: UsuarioFirebaseEntity) {
        val uid = miUid
        viewModelScope.launch {
            repo.aceptarSolicitud(uid, usuario)
            _mensaje.value = "¡Ahora sois amigos!"
            cargarAmigosYSolicitudes()
        }
    }

    fun rechazarSolicitud(amigoUid: String) {
        val uid = miUid
        viewModelScope.launch {
            repo.rechazarSolicitud(uid, amigoUid)
            _mensaje.value = "Solicitud rechazada"
            cargarAmigosYSolicitudes()
        }
    }

    fun eliminarAmigo(amigoUid: String) {
        val uid = miUid
        viewModelScope.launch {
            repo.eliminarAmigo(uid, amigoUid)
            _mensaje.value = "Amigo eliminado"
            cargarAmigosYSolicitudes()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AmigosViewModel() as T
        }
    }
}

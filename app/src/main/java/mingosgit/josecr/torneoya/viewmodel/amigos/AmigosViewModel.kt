package mingosgit.josecr.torneoya.viewmodel.amigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.entities.AmigoFirebaseEntity
import mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity
import mingosgit.josecr.torneoya.repository.AmigosRepository

class AmigosViewModel(
    private val repo: AmigosRepository = AmigosRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _amigos = MutableStateFlow<List<AmigoFirebaseEntity>>(emptyList())
    val amigos: StateFlow<List<AmigoFirebaseEntity>> = _amigos

    private val _solicitudes = MutableStateFlow<List<UsuarioFirebaseEntity>>(emptyList())
    val solicitudes: StateFlow<List<UsuarioFirebaseEntity>> = _solicitudes

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
            _amigos.value = repo.getAmigos(uid)
            _solicitudes.value = repo.getSolicitudes(uid)
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

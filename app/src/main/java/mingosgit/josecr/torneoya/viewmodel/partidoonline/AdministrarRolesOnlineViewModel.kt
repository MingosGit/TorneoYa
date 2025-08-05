package mingosgit.josecr.torneoya.viewmodel.partidoonline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.data.firebase.PartidoFirebaseRepository
import mingosgit.josecr.torneoya.data.firebase.UsuarioFirebaseEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UsuarioConRolUi(
    val uid: String,
    val nombre: String,
    val avatar: Int?, // CAMBIO: avatar en vez de avatarUrl
    val esAdmin: Boolean
)

class AdministrarRolesOnlineViewModel(
    private val partidoUid: String,
    private val repo: PartidoFirebaseRepository
) : ViewModel() {

    private val _usuariosConAcceso = MutableStateFlow<List<UsuarioConRolUi>>(emptyList())
    val usuariosConAcceso: StateFlow<List<UsuarioConRolUi>> = _usuariosConAcceso

    private val _administradores = MutableStateFlow<List<UsuarioConRolUi>>(emptyList())
    val administradores: StateFlow<List<UsuarioConRolUi>> = _administradores

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            val partido = repo.obtenerPartido(partidoUid)
            val accesoUids = partido?.usuariosConAcceso ?: emptyList()
            val adminUids = partido?.administradores ?: emptyList()

            val usuariosConAcceso = accesoUids
                .filter { it.isNotBlank() && !adminUids.contains(it) }
                .map { uid -> getUsuarioInfo(uid, false) }

            val administradores = adminUids
                .filter { it.isNotBlank() }
                .map { uid -> getUsuarioInfo(uid, true) }

            _usuariosConAcceso.value = usuariosConAcceso.mapNotNull { it }
            _administradores.value = administradores.mapNotNull { it }
        }
    }

    private suspend fun getUsuarioInfo(uid: String, esAdmin: Boolean): UsuarioConRolUi? {
        val snap = FirebaseFirestore.getInstance().collection("usuarios").document(uid).get().await()
        val entity = snap.toObject(UsuarioFirebaseEntity::class.java) ?: return null
        return UsuarioConRolUi(uid, entity.nombreUsuario, entity.avatar, esAdmin)
    }

    fun quitarUsuarioDeAcceso(uid: String) {
        viewModelScope.launch {
            repo.quitarUsuarioDeAcceso(partidoUid, uid)
            cargarUsuarios()
        }
    }

    fun darRolAdministrador(uid: String) {
        viewModelScope.launch {
            repo.agregarAdministrador(partidoUid, uid)
            cargarUsuarios()
        }
    }

    fun quitarRolAdministrador(uid: String) {
        viewModelScope.launch {
            repo.eliminarAdministrador(partidoUid, uid)
            cargarUsuarios()
        }
    }

    fun eliminarUsuarioCompletamente(uid: String) {
        viewModelScope.launch {
            repo.eliminarAdministrador(partidoUid, uid) // por si es admin
            repo.quitarUsuarioDeAcceso(partidoUid, uid)
            cargarUsuarios()
        }
    }
}

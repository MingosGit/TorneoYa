package mingosgit.josecr.torneoya.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class AgregarJugadoresViewModel(jugadores: List<String>) : ViewModel() {
    // mutableStateListOf es observable por Compose
    var nombres = mutableStateListOf<String>().apply { addAll(jugadores) }
        private set

    fun agregar(nombre: String) {
        nombres.add(nombre)
    }

    fun borrar(idx: Int) {
        nombres.removeAt(idx)
    }

    fun getJugadores(): List<String> = nombres.toList()
}

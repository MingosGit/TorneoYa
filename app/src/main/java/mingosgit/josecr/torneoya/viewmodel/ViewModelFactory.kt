package mingosgit.josecr.torneoya.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mingosgit.josecr.torneoya.repository.EquiposRepository
import mingosgit.josecr.torneoya.repository.PartidosRepository
import mingosgit.josecr.torneoya.repository.TorneosRepository
import mingosgit.josecr.torneoya.repository.IntegrantesRepository

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = DatabaseProvider.getDatabase(context)
        val torneosRepo = TorneosRepository(db.torneosDao())
        val partidosRepo = PartidosRepository(db.partidosDao())
        val equiposRepo = EquiposRepository(db.equiposDao())
        val integrantesRepo = IntegrantesRepository(db.integrantesDao())

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(torneosRepo, partidosRepo, equiposRepo) as T
            }
            modelClass.isAssignableFrom(TorneoViewModel::class.java) -> {
                TorneoViewModel(torneosRepo) as T
            }
            modelClass.isAssignableFrom(PartidoViewModel::class.java) -> {
                PartidoViewModel(partidosRepo, equiposRepo, integrantesRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

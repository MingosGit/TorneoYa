package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.usuario.AdministrarPartidosViewModel
import mingosgit.josecr.torneoya.repository.EquipoRepository
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.runBlocking
import mingosgit.josecr.torneoya.data.database.AppDatabase

@Composable
fun PartidosListaBusquedaScreen(
    viewModel: AdministrarPartidosViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val equipoDao = db.equipoDao()
    val partidos by viewModel.partidos.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // Cache de nombres de equipos para no hacer consultas repetidas en recomposiciones
    val equipoNombres = remember { mutableStateMapOf<Long, String>() }

    fun getNombreEquipo(equipoId: Long): String {
        if (equipoId == -1L) return "Equipo desconocido"
        equipoNombres[equipoId]?.let { return it }
        val nombre = runBlocking { equipoDao.getNombreById(equipoId) ?: "Equipo #$equipoId" }
        equipoNombres[equipoId] = nombre
        return nombre
    }

    LaunchedEffect(Unit) {
        viewModel.cargarPartidos()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.setBusqueda(it.text)
            },
            label = { Text("Buscar por fecha o ID") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
        Divider()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(partidos) { partido ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable {
                            navController.navigate("administrar_partido_goles/${partido.id}")
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("ID: ${partido.id}")
                            Text("Fecha: ${partido.fecha}")
                            // Mostramos los nombres de los dos equipos
                            Text(
                                "Equipos: ${getNombreEquipo(partido.equipoAId)} vs ${getNombreEquipo(partido.equipoBId)}"
                            )
                            Text("Goles: ${partido.golesEquipoA} - ${partido.golesEquipoB}")
                        }
                    }
                }
            }
        }
    }
}

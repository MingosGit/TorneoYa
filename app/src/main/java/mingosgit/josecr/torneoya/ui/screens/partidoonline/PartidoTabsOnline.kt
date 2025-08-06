package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineUiState
import mingosgit.josecr.torneoya.viewmodel.partidoonline.VisualizarPartidoOnlineViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R

@Composable
fun PartidoTabsOnline(
    uiState: VisualizarPartidoOnlineUiState,
    vm: VisualizarPartidoOnlineViewModel,
    usuarioUid: String,
    partidoUid: String,
    golesReloadKey: Int = 0
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf(
        stringResource(id = R.string.ponlinetabs_jugadores),
        stringResource(id = R.string.ponlinetabs_eventos),
        stringResource(id = R.string.ponlinetabs_comentarios),
        stringResource(id = R.string.ponlinetabs_encuestas)
    )
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var reloadEventos by remember { mutableStateOf(0) }

    val customIndicator = @Composable { tabPositions: List<TabPosition> ->
        TabRowDefaults.Indicator(
            modifier = Modifier
                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                .height(5.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(TorneoYaPalette.blue)
        )
    }

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color(0xFF23273D),
        edgePadding = 0.dp,
        indicator = { tabPositions -> customIndicator(tabPositions) },
        divider = {}
    ) {
        tabTitles.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val tabModifier = Modifier
                .padding(vertical = 3.dp, horizontal = 3.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF23273D))

            Tab(
                selected = isSelected,
                onClick = {
                    isLoading = true
                    selectedTabIndex = index
                    scope.launch {
                        when (index) {
                            0 -> vm.cargarDatos(usuarioUid)
                            1 -> { reloadEventos++ }
                            2, 3 -> vm.cargarComentariosEncuestas(usuarioUid)
                        }
                        kotlinx.coroutines.delay(400)
                        isLoading = false
                    }
                },
                modifier = tabModifier,
                selectedContentColor = TorneoYaPalette.blue,
                unselectedContentColor = Color(0xFFB7B7D1),
                text = {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TorneoYaPalette.blue else Color(0xFFB7B7D1),
                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
                    )
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))

    if (isLoading) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = TorneoYaPalette.blue) }
    } else {
        when (selectedTabIndex) {
            0 -> PartidoTabJugadoresOnline(uiState)
            1 -> PartidoTabEventosOnline(partidoUid, uiState, reloadKey = reloadEventos + golesReloadKey)
            2 -> PartidoTabComentariosOnline(vm, usuarioUid)
            3 -> PartidoTabEncuestasOnline(vm, usuarioUid)
        }
    }
}

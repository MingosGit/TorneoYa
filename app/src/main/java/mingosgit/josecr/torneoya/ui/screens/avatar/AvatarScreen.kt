package mingosgit.josecr.torneoya.ui.screens.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarScreen(
    navController: NavController,            // Navegación para cerrar/volver
    globalUserViewModel: GlobalUserViewModel // VM global: lee/escribe avatar del usuario
) {
    val totalAvatares = 21                               // Cantidad total de avatares disponibles
    val avatarList = listOf(0) + (1..totalAvatares)      // Lista con placeholder (0) + avatares 1..N
    val context = LocalContext.current

    val avatarActual by globalUserViewModel.avatar.collectAsState() // Avatar actual desde el VM
    var selectedAvatar by remember { mutableStateOf(avatarActual ?: 1) } // Selección local en la UI
    var guardando by remember { mutableStateOf(false) }               // Flag de guardado en curso

    val cs = MaterialTheme.colorScheme
    val gradientBorder = Brush.horizontalGradient(listOf(cs.primary, cs.secondary)) // Borde degradado común
    val modernBackground = TorneoYaPalette.backgroundGradient                       // Fondo degradado

    val scope = rememberCoroutineScope() // Alcance para lanzar corrutinas

    // Estructura base con barra superior
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.AvatSC_title),
                        color = cs.onBackground,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    // Botón circular para cerrar (volver atrás)
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = gradientBorder,
                                shape = CircleShape
                            )
                            .background(cs.surfaceVariant)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = stringResource(id = R.string.gen_cerrar),
                            tint = cs.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = cs.onSurface
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        // Contenido principal: preview del avatar + rejilla de selección + acciones
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Preview grande del avatar seleccionado
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(142.dp)
                    .clip(CircleShape)
                    .border(
                        width = 5.dp,
                        brush = gradientBorder,
                        shape = CircleShape
                    )
                    .background(
                        Brush.radialGradient(
                            colors = listOf(cs.surfaceVariant, cs.background),
                            radius = 210f
                        )
                    )
            ) {
                val avatarRes = remember(selectedAvatar) {
                    if (selectedAvatar == 0) {
                        context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)
                    } else {
                        context.resources.getIdentifier("avatar_${selectedAvatar}", "drawable", context.packageName)
                    }
                }
                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = stringResource(id = R.string.AvatSC_selected_avatar_desc),
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Título de sección de selección
            Text(
                text = stringResource(id = R.string.AvatSC_select_avatar),
                color = cs.onBackground,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rejilla de avatares: placeholder + 1..N
            Box(
                Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4), // 4 columnas
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    userScrollEnabled = true,
                ) {
                    items(avatarList, key = { it }) { avatarNum ->
                        val isPlaceholder = avatarNum == 0
                        val avatarRes = remember(avatarNum) {
                            if (isPlaceholder) {
                                context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)
                            } else {
                                context.resources.getIdentifier("avatar_$avatarNum", "drawable", context.packageName)
                            }
                        }
                        // Cada celda: imagen circular con borde resaltado si está seleccionada
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(7.dp)
                        ) {
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = if (isPlaceholder)
                                    stringResource(id = R.string.AvatSC_empty_avatar_desc)
                                else
                                    stringResource(id = R.string.AvatSC_avatar_desc, avatarNum),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selectedAvatar == avatarNum) 3.5.dp else 2.dp,
                                        brush = if (selectedAvatar == avatarNum)
                                            gradientBorder
                                        else
                                            Brush.horizontalGradient(listOf(cs.outline, cs.surfaceVariant)),
                                        shape = CircleShape
                                    )
                                    .background(
                                        if (selectedAvatar == avatarNum)
                                            Brush.radialGradient(listOf(cs.primary.copy(alpha = 0.15f), Color.Transparent), radius = 55f)
                                        else
                                            Brush.radialGradient(listOf(cs.surfaceVariant.copy(alpha = 0.15f), Color.Transparent), radius = 55f)
                                    )
                                    .clickable {
                                        if (selectedAvatar != avatarNum) selectedAvatar = avatarNum // Cambia selección
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Botones de acción: cancelar / guardar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(19.dp)
            ) {
                // Cancelar: vuelve sin guardar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = gradientBorder,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(cs.surfaceVariant)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.gen_cancelar),
                        color = cs.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Guardar: envía el avatar seleccionado al VM y cierra
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = gradientBorder,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(Color.Transparent)
                        .clickable(
                            enabled = !guardando
                        ) {
                            guardando = true
                            scope.launch(Dispatchers.IO) {
                                globalUserViewModel.cambiarAvatarEnFirebase(selectedAvatar) // Persiste en backend
                            }
                            navController.popBackStack() // Cierra pantalla
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (guardando) stringResource(id = R.string.gen_guardando) else stringResource(id = R.string.gen_guardar),
                        color = cs.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

// Archivo: AvatarScreen.kt
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
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    val totalAvatares = 21
    val avatarList = listOf(0) + (1..totalAvatares)  // 0 será el placeholder
    val context = LocalContext.current

    val avatarActual by globalUserViewModel.avatar.collectAsState()
    var selectedAvatar by remember { mutableStateOf(avatarActual ?: 1) }
    var guardando by remember { mutableStateOf(false) }

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.AvatSC_title),
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                                ),
                                shape = CircleShape
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                )
                            )
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = stringResource(id = R.string.gen_cerrar),
                            tint = Color(0xFF8F5CFF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = modernBackground)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            // AVATAR GRANDE SELECCIONADO
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(142.dp)
                    .clip(CircleShape)
                    .border(
                        width = 5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                        ),
                        shape = CircleShape
                    )
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF23273D), Color(0xFF1B1D29)),
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
            Text(
                text = stringResource(id = R.string.AvatSC_select_avatar),
                color = Color(0xFFF7F7FF),
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // GRID DE AVATARES
            Box(
                Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
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
                                            Brush.horizontalGradient(listOf(TorneoYaPalette.blue, TorneoYaPalette.violet))
                                        else
                                            Brush.horizontalGradient(listOf(Color(0xFF3E4160), Color(0xFF20243B))),
                                        shape = CircleShape
                                    )
                                    .background(
                                        if (selectedAvatar == avatarNum)
                                            Brush.radialGradient(listOf(Color(0x221968FF), Color.Transparent), radius = 55f)
                                        else
                                            Brush.radialGradient(listOf(Color(0x2223243D), Color.Transparent), radius = 55f)
                                    )
                                    .clickable {
                                        if (selectedAvatar != avatarNum) selectedAvatar = avatarNum
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(19.dp)
            ) {
                // CANCELAR BUTTON
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                            )
                        )
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.gen_cancelar),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // GUARDAR BUTTON SOLO BORDE, SIN FONDO
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(TorneoYaPalette.blue, TorneoYaPalette.violet)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(Color.Transparent)
                        .clickable(
                            enabled = !guardando
                        ) {
                            guardando = true
                            scope.launch(Dispatchers.IO) {
                                globalUserViewModel.cambiarAvatarEnFirebase(selectedAvatar)
                            }
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (guardando) stringResource(id = R.string.gen_guardando) else stringResource(id = R.string.gen_guardar),
                        color = TorneoYaPalette.blue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

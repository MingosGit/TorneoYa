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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.viewmodel.usuario.GlobalUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarScreen(
    navController: NavController,
    globalUserViewModel: GlobalUserViewModel
) {
    val totalAvatares = 21
    val avatarList = (1..totalAvatares).toList()
    val context = LocalContext.current

    val avatarActual by globalUserViewModel.avatar.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elige tu avatar", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1D29)
                )
            )
        },
        containerColor = Color(0xFF1B1D29)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B1D29))
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Toca un avatar para seleccionarlo",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            ) {
                items(avatarList) { avatarNum ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(
                                id = context.resources.getIdentifier(
                                    "avatar_$avatarNum",
                                    "drawable",
                                    context.packageName
                                )
                            ),
                            contentDescription = "Avatar $avatarNum",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (avatarActual == avatarNum) 4.dp else 2.dp,
                                    color = if (avatarActual == avatarNum) Color(0xFF8F5CFF) else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    globalUserViewModel.cambiarAvatarEnFirebase(avatarNum)
                                    navController.popBackStack()
                                }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

package mingosgit.josecr.torneoya.ui.screens.partidoonline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.viewmodel.partidoonline.PartidoOnlineViewModel
import mingosgit.josecr.torneoya.ui.theme.mutedText

@Composable
// Pantalla de entrada a "Partidos online": si no hay usuario, muestra CTA de login/registro; si hay, delega al listado.
fun PartidoOnlineScreen(
    navController: NavController,
    partidoViewModel: PartidoOnlineViewModel
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val cs = MaterialTheme.colorScheme

    if (currentUser == null) {
        // Fondo degradado suave para el estado sin sesión.
        val modernBackground = Brush.verticalGradient(
            0.0f to cs.background,
            0.28f to cs.surface,
            0.58f to cs.surfaceVariant,
            1.0f to cs.background
        )

        // Vista para usuarios no autenticados con botones a login/registro.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = modernBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono decorativo.
                Surface(
                    shape = CircleShape,
                    color = cs.primary.copy(alpha = 0.13f),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(85.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Logo",
                        tint = cs.primary,
                        modifier = Modifier.padding(22.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                // Título y subtítulo explicativos.
                Text(
                    text = stringResource(R.string.ponline_acceso_partidos_online),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    color = cs.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ponline_inicia_sesion_o_crea_cuenta),
                    fontSize = 16.sp,
                    color = cs.mutedText,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(32.dp))

                // Botón: navegar a pantalla de login.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(cs.primary, cs.secondary)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(cs.surfaceVariant, cs.surface)
                            )
                        )
                        .clickable { navController.navigate("login") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.ponline_boton_iniciar_sesion),
                        color = cs.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(11.dp))

                // Botón: navegar a pantalla de registro.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                listOf(cs.primary, cs.secondary)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(cs.surfaceVariant, cs.surface)
                            )
                        )
                        .clickable { navController.navigate("register") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.ponline_boton_crear_cuenta),
                        color = cs.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(26.dp))
                // Nota informativa sobre cuenta local/ajustes.
                Text(
                    text = stringResource(R.string.ponline_cuenta_local_ajustes),
                    fontSize = 14.sp,
                    color = cs.mutedText,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    lineHeight = 19.sp
                )
            }
        }
    } else {
        // Usuario autenticado: muestra el contenido principal (listado y acciones).
        PartidoOnlineScreenContent(
            navController = navController,
            partidoViewModel = partidoViewModel
        )
    }
}

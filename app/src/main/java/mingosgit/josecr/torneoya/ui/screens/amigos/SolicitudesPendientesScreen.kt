package mingosgit.josecr.torneoya.ui.screens.amigos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.viewmodel.amigos.AmigosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesPendientesScreen(
    navController: NavController,
    amigosViewModel: AmigosViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AmigosViewModel.Factory()
    )
) {
    val solicitudes by amigosViewModel.solicitudes.collectAsState()
    val cs = MaterialTheme.colorScheme
    val gradientBorder = remember(cs.primary, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    }
    val modernBackground = TorneoYaPalette.backgroundGradient

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GradientBorderedIconButton(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.gen_volver),
                            onClick = { navController.popBackStack() },
                            gradient = gradientBorder,
                            iconTint = cs.onSurface,
                            background = cs.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.solpensc_titulo),
                            color = cs.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 23.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = cs.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                if (solicitudes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(26.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = cs.primary.copy(alpha = 0.13f),
                            shadowElevation = 0.dp,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = cs.primary,
                                modifier = Modifier.padding(22.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = stringResource(id = R.string.solpensc_no_solicitudes_titulo),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = cs.onSurfaceVariant
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            text = stringResource(id = R.string.solpensc_no_solicitudes_desc),
                            fontSize = 15.sp,
                            color = cs.secondary,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(horizontal = 15.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        items(solicitudes) { solicitud ->
                            SolicitudItem(
                                uid = solicitud.uid,
                                nombreUsuario = solicitud.nombreUsuario,
                                avatar = solicitud.avatar,
                                onAceptar = {
                                    amigosViewModel.aceptarSolicitud(
                                        mingosgit.josecr.torneoya.data.entities.UsuarioFirebaseEntity(
                                            uid = solicitud.uid,
                                            nombreUsuario = solicitud.nombreUsuario
                                        )
                                    )
                                },
                                onRechazar = { amigosViewModel.rechazarSolicitud(solicitud.uid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradientBorderedIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    gradient: Brush,
    size: androidx.compose.ui.unit.Dp = 38.dp,
    iconSize: androidx.compose.ui.unit.Dp = 21.dp,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    background: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = 2.5.dp,
                brush = gradient,
                shape = CircleShape
            )
            .background(background, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun SolicitudItem(
    uid: String,
    nombreUsuario: String,
    avatar: Int?,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val gradientBorder = remember(cs.primary, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    }
    val gradientAccept = remember(cs.tertiary, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.tertiary, cs.secondary))
    }
    val gradientReject = remember(cs.error, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.error, cs.secondary))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .border(
                width = 2.dp,
                brush = gradientBorder,
                shape = RoundedCornerShape(17.dp)
            )
            .background(cs.surfaceVariant),
        color = Color.Transparent,
        shadowElevation = 3.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val avatarNum = avatar ?: 0
            val avatarResId = remember(avatarNum) {
                if (avatarNum > 0)
                    context.resources.getIdentifier("avatar_$avatarNum", "drawable", context.packageName)
                else
                    context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(cs.tertiary, cs.primary)
                        ),
                        shape = CircleShape
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(cs.surfaceVariant, cs.surface)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatarResId != 0) {
                    Icon(
                        painter = painterResource(id = avatarResId),
                        contentDescription = stringResource(id = R.string.gen_avatar_desc),
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        nombreUsuario.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = cs.onSurface,
                        fontSize = 22.sp
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(
                Modifier
                    .weight(1f)
            ) {
                Text(
                    nombreUsuario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = cs.onSurface
                )
            }
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = onAceptar,
                modifier = Modifier
                    .size(32.dp)
                    .border(
                        2.dp,
                        gradientAccept,
                        shape = CircleShape
                    )
                    .background(cs.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.gen_iniciar_sesion),
                    tint = cs.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(25.dp))
            IconButton(
                onClick = onRechazar,
                modifier = Modifier
                    .size(32.dp)
                    .border(
                        2.dp,
                        gradientReject,
                        shape = CircleShape
                    )
                    .background(cs.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.gen_eliminar_amigo),
                    tint = cs.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

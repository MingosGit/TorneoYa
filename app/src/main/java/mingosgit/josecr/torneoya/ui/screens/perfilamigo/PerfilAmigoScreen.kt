package mingosgit.josecr.torneoya.ui.screens.perfilamigo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.R

@Composable
fun PerfilAmigoScreen(
    navController: NavController,
    amigoUid: String,
    viewModel: PerfilAmigoViewModel = viewModel(factory = PerfilAmigoViewModel.Factory(amigoUid))
) {
    val state by viewModel.state.collectAsState()
    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet
    val accent = TorneoYaPalette.accent
    val mutedText = TorneoYaPalette.mutedText

    val modernBackground = Brush.verticalGradient(
        0.0f to Color(0xFF1B1D29),
        0.28f to Color(0xFF212442),
        0.58f to Color(0xFF191A23),
        1.0f to Color(0xFF14151B)
    )

    val context = LocalContext.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val textBack = stringResource(id = R.string.gen_volver)
    val textProfileFriend = stringResource(id = R.string.ponfilamigo_title_profile) // define este en strings.xml
    val textUidCopied = stringResource(id = R.string.gen_uid_copiado)
    val textGoals = stringResource(id = R.string.ponfilamigo_label_goals)  // define en strings.xml
    val textAssists = stringResource(id = R.string.ponfilamigo_label_assists)  // define en strings.xml
    val textAverage = stringResource(id = R.string.ponfilamigo_label_average)  // define en strings.xml
    val textMatchesPlayed = stringResource(id = R.string.ponfilamigo_label_matches_played) // define en strings.xml

    Box(
        Modifier
            .fillMaxSize()
            .background(modernBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(42.dp)) // Margen superior mayor

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(listOf(blue, violet)),
                                shape = CircleShape
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = textBack,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Botón compartir/copiar UID
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("UID Amigo", state.uid ?: amigoUid)
                            clipboard.setPrimaryClip(clip)
                            showCopiedMessage = true
                            coroutineScope.launch {
                                delay(1400)
                                showCopiedMessage = false
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.horizontalGradient(listOf(blue, violet)),
                                shape = CircleShape
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF23273D), Color(0xFF1C1D25))
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(id = R.string.gen_copiar_uid),
                            tint = Color.White,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }

            Text(
                textProfileFriend,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, violet)),
                        shape = CircleShape
                    )
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF23273D), Color(0xFF1C1D25)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                val avatarRes = if (state.avatar != null)
                    context.resources.getIdentifier("avatar_${state.avatar}", "drawable", context.packageName)
                else
                    context.resources.getIdentifier("avatar_placeholder", "drawable", context.packageName)

                if (avatarRes != 0) {
                    Icon(
                        painter = painterResource(id = avatarRes),
                        contentDescription = stringResource(id = R.string.gen_avatar_desc),
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text("👤", fontSize = 54.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                state.nombreUsuario ?: stringResource(id = R.string.gen_amigos),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                "UID: ${state.uid}",
                color = mutedText,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(listOf(blue, violet)),
                        shape = RoundedCornerShape(17.dp)
                    )
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xFF23273D))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(textGoals, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                state.goles?.toString() ?: "-",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(textAssists, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                state.asistencias?.toString() ?: "-",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(textAverage, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (state.promedioGoles != null) String.format("%.2f", state.promedioGoles) else "-",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    if (state.partidosJugados != null) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$textMatchesPlayed: ${state.partidosJugados}",
                                color = mutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
        if (showCopiedMessage) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 38.dp)
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xEE24243A),
                    shape = RoundedCornerShape(13.dp),
                    shadowElevation = 7.dp
                ) {
                    Text(
                        textUidCopied,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

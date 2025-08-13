package mingosgit.josecr.torneoya.ui.screens.usuario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette
import mingosgit.josecr.torneoya.ui.theme.mutedText

@Composable
fun ConfirmarCorreoScreen(
    navController: NavController,
    correoElectronico: String,
    onVerificado: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val modernBackground = TorneoYaPalette.backgroundGradient
    val gradientPrimarySecondary = remember(cs.primary, cs.secondary) {
        Brush.horizontalGradient(listOf(cs.primary, cs.secondary))
    }
    val context = LocalContext.current

    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var infoMsg by remember { mutableStateOf<String?>(null) } // <--- mensaje informativo (no rojo)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = modernBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono centrado
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, gradientPrimarySecondary, CircleShape)
                    .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = stringResource(id = R.string.gen_email_desc),
                    tint = cs.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(id = R.string.confirmar_correo_titulo),
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.mutedText,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(2.dp, gradientPrimarySecondary, RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface))),
                color = Color.Transparent,
                shadowElevation = 0.dp,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    Modifier
                        .padding(vertical = 18.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.confirmar_correo_mensaje, correoElectronico),
                        color = MaterialTheme.colorScheme.mutedText,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    // Mensaje de error (rojo)
                    AnimatedVisibility(visible = errorMsg != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(
                            text = errorMsg.orEmpty(),
                            color = cs.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Mensaje informativo (NO rojo)
                    AnimatedVisibility(visible = infoMsg != null, enter = fadeIn(), exit = fadeOut()) {
                        Text(
                            text = infoMsg.orEmpty(),
                            color = cs.primary, // color amigable, no rojo
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Botón "Ya verificado"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, gradientPrimarySecondary, RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                            .clickable(enabled = !loading) {
                                loading = true
                                errorMsg = null
                                infoMsg = null
                                val auth = FirebaseAuth.getInstance()
                                val user = auth.currentUser
                                if (user != null) {
                                    user.reload()
                                        .addOnCompleteListener { reloadTask ->
                                            if (reloadTask.isSuccessful) {
                                                if (user.isEmailVerified) {
                                                    onVerificado()
                                                } else {
                                                    errorMsg = context.getString(R.string.confirmar_correo_no_verificado)
                                                }
                                            } else {
                                                errorMsg = reloadTask.exception?.localizedMessage
                                                    ?: context.getString(R.string.confirmar_correo_error_comprobar)
                                            }
                                            loading = false
                                        }
                                } else {
                                    errorMsg = context.getString(R.string.confirmar_correo_sesion_expirada)
                                    loading = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(strokeWidth = 2.2.dp, modifier = Modifier.size(22.dp), color = cs.primary)
                        } else {
                            Text(
                                text = stringResource(id = R.string.confirmar_correo_btn_verificado),
                                color = MaterialTheme.colorScheme.mutedText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Botón "Reenviar"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, gradientPrimarySecondary, RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(cs.surfaceVariant, cs.surface)))
                            .clickable {
                                val auth = FirebaseAuth.getInstance()
                                auth.currentUser?.sendEmailVerification()
                                errorMsg = null
                                infoMsg = context.getString(R.string.confirmar_correo_reenviado) +
                                        " · Revisa tu carpeta de SPAM."
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.confirmar_correo_btn_reenviar),
                            color = MaterialTheme.colorScheme.mutedText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

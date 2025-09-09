package mingosgit.josecr.torneoya.ui.screens.usuario.ajustes

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Pantalla de selección de idioma: lista de opciones, guarda en SharedPreferences y recrea la actividad
fun IdiomaScreen(
    navController: NavController,          // Navegación para volver atrás
    onLanguageChanged: () -> Unit          // Callback para notificar cambio de idioma al host
) {
    // Forma de las tarjetas de opción
    val cardShape = RoundedCornerShape(17.dp)
    // Contexto de la actividad
    val context = LocalContext.current
    // Idioma actual del sistema guardado en estado
    val currentLocale = remember { mutableStateOf(Locale.getDefault().language) }

    // Textos visibles de los idiomas
    val idiomas = listOf(
        stringResource(id = R.string.idioma_espanol),
        stringResource(id = R.string.idioma_catala),
        stringResource(id = R.string.idioma_english)
    )

    // Códigos ISO correspondientes a cada idioma
    val languageCodes = listOf("es", "ca", "en")

    // Recursos de banderas para cada idioma
    val banderas = listOf(
        R.drawable.flag_es,
        R.drawable.flag_cat,
        R.drawable.flag_uk
    )

    // Colores y degradados de tema
    val blue = TorneoYaPalette.blue
    val violet = TorneoYaPalette.violet
    val backgroundGradient = TorneoYaPalette.backgroundGradient
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            // Barra superior con botón de volver y título
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón circular con borde degradado que navega atrás
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                                    shape = CircleShape
                                )
                                .background(
                                    Brush.horizontalGradient(listOf(surfaceColor, surfaceVariant))
                                )
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "ATRAS",
                                tint = violet,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(15.dp))
                        // Título "Idioma"
                        Text(
                            text = stringResource(id = R.string.ajustes_idioma),
                            color = onBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 27.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        // Fondo con degradado de la pantalla
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(innerPadding)
        ) {
            // Lista vertical de opciones de idioma
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(23.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Itera por cada idioma mostrando bandera + tarjeta clicable
                idiomas.forEachIndexed { index, idioma ->
                    val seleccionado = currentLocale.value == languageCodes[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar circular con la bandera del idioma
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                                    shape = CircleShape
                                )
                                .background(
                                    Brush.horizontalGradient(listOf(surfaceColor, surfaceVariant))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = banderas[index]),
                                contentDescription = "Bandera $idioma",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(Modifier.width(13.dp))
                        // Opción de idioma: tarjeta con borde degradado y click para seleccionar
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(cardShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                                    shape = cardShape
                                )
                                .background(
                                    Brush.horizontalGradient(listOf(surfaceColor, surfaceVariant))
                                )
                                .clickable {
                                    // Guarda el código de idioma en preferencias
                                    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                                    sharedPref.edit().putString("app_language", languageCodes[index]).apply()

                                    // Actualiza estado local y notifica al host
                                    currentLocale.value = languageCodes[index]
                                    onLanguageChanged()
                                    // Recrea la actividad para aplicar strings/recursos
                                    (context as? Activity)?.recreate()
                                },
                            color = Color.Transparent,
                            shadowElevation = 0.dp
                        ) {
                            // Contenido de la tarjeta: nombre del idioma + check si está seleccionado
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 22.dp, horizontal = 17.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Nombre del idioma
                                    Text(
                                        text = idioma,
                                        fontSize = 17.sp,
                                        color = onBackground,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Indicador de seleccionado (círculo con check)
                                    if (seleccionado) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .border(
                                                    width = 2.dp,
                                                    brush = Brush.horizontalGradient(listOf(blue, violet)),
                                                    shape = CircleShape
                                                )
                                                .background(Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seleccionado",
                                                tint = blue,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

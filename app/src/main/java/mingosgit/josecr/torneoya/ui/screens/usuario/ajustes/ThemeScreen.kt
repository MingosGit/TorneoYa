package mingosgit.josecr.torneoya.ui.screens.usuario.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mingosgit.josecr.torneoya.R
import mingosgit.josecr.torneoya.ui.theme.TorneoYaPalette

// Forma base de las tarjetas de opciones
private val cardShape = RoundedCornerShape(16.dp)

/** themeMode: 0=Sistema, 1=Claro, 2=Oscuro */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Pantalla de Tema: muestra 3 opciones (sistema/claro/oscuro) y notifica cambios
fun ThemeScreen(
    navController: NavController,          // Control de navegación para volver
    currentThemeDark: Boolean,             // Indica si el tema actual es oscuro (no se usa aquí, pero disponible)
    currentMode: Int,                      // Modo actual seleccionado
    onThemeChange: (Int) -> Unit           // Callback al seleccionar un modo
) {
    // Estado local guardable del modo seleccionado
    var selectedMode by rememberSaveable { mutableStateOf(currentMode) }
    // Sincroniza cuando cambia desde fuera
    LaunchedEffect(currentMode) { selectedMode = currentMode }

    val colorScheme = MaterialTheme.colorScheme
    val modernBackground = TorneoYaPalette.backgroundGradient

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
                        // Botón de volver con borde en degradado
                        GradientBorderedIconButton(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            onClick = { navController.popBackStack() },
                            gradient = Brush.horizontalGradient(
                                listOf(colorScheme.primary, colorScheme.secondary)
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.ajustes_tema_app),
                            color = colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        // Fondo con gradiente y lista de opciones
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(modernBackground)
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 14.dp,
                    end = 14.dp
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Opción: seguir sistema
                ThemeOptionCard(
                    title = stringResource(id = R.string.theme_system),
                    selected = selectedMode == 0,
                    onClick = {
                        if (selectedMode != 0) {
                            selectedMode = 0
                            onThemeChange(0)
                        }
                    },
                    colorScheme = colorScheme
                )
                // Opción: claro
                ThemeOptionCard(
                    title = stringResource(id = R.string.theme_light),
                    selected = selectedMode == 1,
                    onClick = {
                        if (selectedMode != 1) {
                            selectedMode = 1
                            onThemeChange(1)
                        }
                    },
                    colorScheme = colorScheme
                )
                // Opción: oscuro
                ThemeOptionCard(
                    title = stringResource(id = R.string.theme_dark),
                    selected = selectedMode == 2,
                    onClick = {
                        if (selectedMode != 2) {
                            selectedMode = 2
                            onThemeChange(2)
                        }
                    },
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
// Tarjeta de opción de tema: radio + texto, con borde degradado y click
private fun ThemeOptionCard(
    title: String,               // Texto de la opción
    selected: Boolean,           // Si está seleccionada
    onClick: () -> Unit,         // Acción al pulsar
    colorScheme: ColorScheme     // Paleta para colores
) {
    val leftColor = colorScheme.primary.copy(alpha = 0.8f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, cardShape)
            .clip(cardShape)
            .background(colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(listOf(leftColor, colorScheme.secondary)),
                shape = cardShape
            )
            .clickable { onClick() },
        color = colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // RadioButton que refleja el estado seleccionado
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Etiqueta de la opción
            Text(
                text = title,
                fontSize = 17.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

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

private val cardShape = RoundedCornerShape(16.dp)

/** themeMode: 0=Sistema, 1=Claro, 2=Oscuro */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    navController: NavController,
    currentThemeDark: Boolean,
    currentMode: Int,
    onThemeChange: (Int) -> Unit
) {
    var selectedMode by rememberSaveable { mutableStateOf(currentMode) }

    LaunchedEffect(currentMode) { selectedMode = currentMode }

    val colorScheme = MaterialTheme.colorScheme
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
private fun ThemeOptionCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme
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
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


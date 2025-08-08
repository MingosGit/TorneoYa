package mingosgit.josecr.torneoya.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemeScreen(
    currentThemeDark: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var selectedDark by rememberSaveable { mutableStateOf(currentThemeDark) }

    // Sincroniza el estado local si viene actualizado desde arriba
    LaunchedEffect(currentThemeDark) {
        selectedDark = currentThemeDark
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selecciona Tema",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ThemeOption(
            title = "Claro",
            selected = !selectedDark
        ) {
            if (selectedDark) {
                selectedDark = false
                onThemeChange(false)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ThemeOption(
            title = "Oscuro",
            selected = selectedDark
        ) {
            if (!selectedDark) {
                selectedDark = true
                onThemeChange(true)
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title)
        }
    }
}

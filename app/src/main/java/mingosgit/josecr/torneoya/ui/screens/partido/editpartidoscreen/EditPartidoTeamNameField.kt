package mingosgit.josecr.torneoya.ui.screens.partido.editpartidoscreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditPartidoTeamNameField(
    label: String,
    nombre: String,
    onNombreChange: (String) -> Unit,
    editando: Boolean,
    onEditandoChange: (Boolean) -> Unit,
    onGuardar: () -> Unit
) {
    OutlinedTextField(
        value = nombre,
        onValueChange = onNombreChange,
        label = { Text(label) },
        singleLine = true,
        enabled = editando,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = {
                if (editando) {
                    onGuardar()
                }
                onEditandoChange(!editando)
            }) {
                Icon(
                    imageVector = if (editando) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (editando) "Guardar" else "Editar"
                )
            }
        }
    )
}

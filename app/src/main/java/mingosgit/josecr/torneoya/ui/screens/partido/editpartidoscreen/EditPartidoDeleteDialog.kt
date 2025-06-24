package mingosgit.josecr.torneoya.ui.screens.partido.editpartidoscreen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun EditPartidoDeleteDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete
            ) { Text("Eliminar", color = Color.Red) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Eliminar Partido") },
        text = { Text("¿Seguro que deseas eliminar este partido? Esta acción no se puede deshacer.") }
    )
}

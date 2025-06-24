package mingosgit.josecr.torneoya.ui.screens.usuario

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.io.File
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

@Composable
fun CropImageDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onCropDone: (croppedPath: String) -> Unit
) {
    val context = LocalContext.current

    val originalBitmap = remember(uri) {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    }

    if (originalBitmap == null) {
        onDismiss()
        return
    }

    val cropBoxSize = 240.dp
    val cropBoxPx = cropBoxSize.dpToPx()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Ajusta tu foto") },
        text = {
            Box(
                modifier = Modifier
                    .height(320.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(cropBoxSize)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.Gray)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = originalBitmap
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Recortar foto",
                            modifier = Modifier
                                .size(cropBoxSize * scale)
                                .offset {
                                    IntOffset(
                                        offset.x.roundToInt(),
                                        offset.y.roundToInt()
                                    )
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cropped = cropCircleBitmap(
                        originalBitmap = originalBitmap,
                        scale = scale,
                        offset = offset,
                        cropBoxSizePx = cropBoxPx
                    )
                    val filename = "profile_cropped_${System.currentTimeMillis()}.jpg"
                    val file = File(context.filesDir, filename)
                    file.outputStream().use { out ->
                        cropped.compress(Bitmap.CompressFormat.JPEG, 96, out)
                    }
                    onCropDone(file.absolutePath)
                }
            ) {
                Text("Listo")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun cropCircleBitmap(
    originalBitmap: Bitmap,
    scale: Float,
    offset: Offset,
    cropBoxSizePx: Int
): Bitmap {
    val output = Bitmap.createBitmap(cropBoxSizePx, cropBoxSizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.isAntiAlias = true

    canvas.drawARGB(0, 0, 0, 0)

    val matrix = Matrix()
    val cx = cropBoxSizePx / 2f
    val cy = cropBoxSizePx / 2f

    matrix.postTranslate(-originalBitmap.width / 2f, -originalBitmap.height / 2f)
    matrix.postScale(scale, scale)
    matrix.postTranslate(cx + offset.x, cy + offset.y)

    canvas.save()
    canvas.drawCircle(cx, cy, cropBoxSizePx / 2f, paint)
    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(originalBitmap, matrix, paint)
    paint.xfermode = null
    canvas.restore()

    return output
}

// Extension dp -> px para Compose
private fun Dp.dpToPx(): Int {
    val density = Resources.getSystem().displayMetrics.density
    return (value * density).roundToInt()
}

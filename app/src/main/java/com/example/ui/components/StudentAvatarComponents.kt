package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

object StudentAvatarImageHelper {

    private val bitmapCache = object : LruCache<Int, ImageBitmap>(120) {}

    /**
     * Center-crops the source [Bitmap] to a 1:1 square, scales it to [targetSizePx]x[targetSizePx] (200x200),
     * and compresses it as JPEG until the resulting Base64 string is guaranteed < 28KB (< 30KB limit).
     */
    fun cropAndCompressToBase64(
        source: Bitmap,
        targetSizePx: Int = 200,
        maxBytes: Int = 20_000 // ~20KB raw bytes -> ~27KB Base64 (< 30KB)
    ): String {
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return ""

        val squareEdge = min(width, height)
        val cropX = (width - squareEdge) / 2
        val cropY = (height - squareEdge) / 2

        val cropped = Bitmap.createBitmap(source, cropX, cropY, squareEdge, squareEdge)
        val scaled = if (squareEdge != targetSizePx) {
            Bitmap.createScaledBitmap(cropped, targetSizePx, targetSizePx, true)
        } else {
            cropped
        }

        var quality = 74
        var compressedBytes: ByteArray
        do {
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            compressedBytes = out.toByteArray()
            quality -= 10
        } while (compressedBytes.size > maxBytes && quality >= 24)

        return Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
    }

    fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            var sampleSize = 1
            val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
            while (maxDim / sampleSize > 600) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getCachedBitmap(base64: String): ImageBitmap? {
        if (base64.isBlank()) return null
        return bitmapCache.get(base64.hashCode())
    }

    fun decodeBase64ToImageBitmap(base64: String): ImageBitmap? {
        if (base64.isBlank()) return null
        val cacheKey = base64.hashCode()
        bitmapCache.get(cacheKey)?.let { return it }

        return try {
            val clean = base64.substringAfter("base64,", base64)
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val imageBitmap = bmp.asImageBitmap()
            bitmapCache.put(cacheKey, imageBitmap)
            imageBitmap
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Circular thumbnail for a child.
 * - Shows the child's square-cropped photo if [Student.avatarBase64] is present.
 * - Falls back to [Student.avatarInitials] if no photo is set.
 * - Tapping the circle opens the photo picker / camera dialog for any servant.
 */
@Composable
fun StudentAvatarCircle(
    student: Student,
    blockAccent: Color,
    size: Dp = 42.dp,
    onClick: () -> Unit,
    testTag: String = "student_avatar_${student.id}"
) {
    var imageBitmap by remember(student.avatarBase64) {
        mutableStateOf(StudentAvatarImageHelper.getCachedBitmap(student.avatarBase64))
    }

    LaunchedEffect(student.avatarBase64) {
        if (student.avatarBase64.isBlank()) {
            imageBitmap = null
        } else if (imageBitmap == null) {
            imageBitmap = withContext(Dispatchers.Default) {
                StudentAvatarImageHelper.decodeBase64ToImageBitmap(student.avatarBase64)
            }
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(blockAccent.copy(alpha = 0.25f))
            .border(1.5.dp, blockAccent, CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = "صورة ${student.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = student.avatarInitials,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StudentPhotoPickerDialog(
    student: Student,
    blockAccent: Color,
    onDismiss: () -> Unit,
    onSaveAvatarBase64: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var previewBase64 by remember(student.avatarBase64) { mutableStateOf(student.avatarBase64) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            errorMessage = null
            scope.launch {
                val encoded = withContext(Dispatchers.IO) {
                    val bmp = StudentAvatarImageHelper.decodeBitmapFromUri(context, uri)
                    if (bmp != null) {
                        StudentAvatarImageHelper.cropAndCompressToBase64(bmp)
                    } else {
                        ""
                    }
                }
                isProcessing = false
                if (encoded.isNotBlank()) {
                    previewBase64 = encoded
                } else {
                    errorMessage = "تعذر قراءة الصورة المختارة"
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            isProcessing = true
            errorMessage = null
            scope.launch {
                val encoded = withContext(Dispatchers.Default) {
                    StudentAvatarImageHelper.cropAndCompressToBase64(bitmap)
                }
                isProcessing = false
                if (encoded.isNotBlank()) {
                    previewBase64 = encoded
                } else {
                    errorMessage = "تعذر معالجة الصورة الملتقطة"
                }
            }
        }
    }

    var previewBitmap by remember(previewBase64) {
        mutableStateOf(StudentAvatarImageHelper.getCachedBitmap(previewBase64))
    }
    LaunchedEffect(previewBase64) {
        if (previewBase64.isBlank()) {
            previewBitmap = null
        } else if (previewBitmap == null) {
            previewBitmap = withContext(Dispatchers.Default) {
                StudentAvatarImageHelper.decodeBase64ToImageBitmap(previewBase64)
            }
        }
    }
    val approxKb = remember(previewBase64) {
        if (previewBase64.isBlank()) 0 else (previewBase64.length * 3 / 4) / 1024
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "صورة الطفل: ${student.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circular / Square Preview
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(blockAccent.copy(alpha = 0.22f))
                        .border(2.dp, blockAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val currentPreviewBitmap = previewBitmap
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    } else if (currentPreviewBitmap != null) {
                        Image(
                            bitmap = currentPreviewBitmap,
                            contentDescription = "معاينة صورة ${student.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = student.avatarInitials,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (previewBase64.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "مقصوصة مربعة ٢٠٠×٢٠٠ • مضغوطة (${maxOf(1, approxKb)} KB) • جاهزة للمزامنة أوفلاين",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = "اختر صورة من المعرض أو الكاميرا ليتم قصها مربعة وضغطها تلقائياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pick_student_photo_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("المعرض")
                    }

                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pick_student_photo_camera_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الكاميرا")
                    }
                }

                if (previewBase64.isNotBlank()) {
                    TextButton(
                        onClick = { previewBase64 = "" },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("clear_student_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إزالة الصورة والعودة للحروف")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveAvatarBase64(previewBase64) },
                enabled = !isProcessing && previewBase64 != student.avatarBase64,
                modifier = Modifier.testTag("confirm_save_student_photo_button")
            ) {
                Text("حفظ الصورة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

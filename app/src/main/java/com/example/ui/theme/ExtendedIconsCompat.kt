@file:Suppress("PackageDirectoryMismatch")
package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Lightweight Material Icon vectors to avoid bundling the 42MB material-icons-extended DEX library
val Icons.Filled.AutoFixHigh: ImageVector
    get() = Icons.Default.Star

val Icons.Filled.AddModerator: ImageVector
    get() = Icons.Default.Lock

val Icons.Filled.ArrowDownward: ImageVector
    get() = Icons.Default.KeyboardArrowDown

val Icons.Filled.ArrowUpward: ImageVector
    get() = Icons.Default.KeyboardArrowUp

val Icons.Filled.CalendarMonth: ImageVector
    get() = Icons.Default.DateRange

val Icons.Filled.CameraAlt: ImageVector
    get() = Icons.Default.AccountCircle

val Icons.Filled.CloudDone: ImageVector
    get() = Icons.Default.CheckCircle

val Icons.Filled.CloudOff: ImageVector
    get() = Icons.Default.Warning

val Icons.Filled.ContentCopy: ImageVector
    get() = Icons.Default.Share

val Icons.Filled.EmojiEvents: ImageVector
    get() = Icons.Default.Star

val Icons.Filled.ExpandLess: ImageVector
    get() = Icons.Default.KeyboardArrowUp

val Icons.Filled.ExpandMore: ImageVector
    get() = Icons.Default.KeyboardArrowDown

val Icons.Filled.FileUpload: ImageVector
    get() = Icons.Default.Add

val Icons.Filled.FlashOn: ImageVector
    get() = Icons.Default.Star

val Icons.Filled.GroupAdd: ImageVector
    get() = Icons.Default.Add

val Icons.Filled.Groups: ImageVector
    get() = Icons.Default.Person

val Icons.Filled.History: ImageVector
    get() = Icons.Default.Refresh

val Icons.Filled.Key: ImageVector
    get() = Icons.Default.Lock

val Icons.Filled.LockOpen: ImageVector
    get() = Icons.Default.Lock

val Icons.Filled.NotificationsActive: ImageVector
    get() = Icons.Default.Notifications

val Icons.Filled.PersonAdd: ImageVector
    get() = Icons.Default.Add

val Icons.Filled.PersonOff: ImageVector
    get() = Icons.Default.Close

val Icons.Filled.PersonRemove: ImageVector
    get() = Icons.Default.Delete

val Icons.Filled.PhotoLibrary: ImageVector
    get() = Icons.Default.AccountCircle

val Icons.Filled.School: ImageVector
    get() = Icons.Default.Star

val Icons.Filled.Security: ImageVector
    get() = Icons.Default.Lock

val Icons.Filled.Tune: ImageVector
    get() = Icons.Default.Settings

val Icons.Filled.ViewModule: ImageVector
    get() = Icons.Default.Info

private var _visibilityIcon: ImageVector? = null
val Icons.Filled.Visibility: ImageVector
    get() = _visibilityIcon ?: ImageVector.Builder(
        name = "Filled.Visibility",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 4.5f)
            curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
            curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f)
            reflectiveCurveToRelative(9.27f, -3.11f, 11f, -7.5f)
            curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f)
            close()
            moveTo(12f, 17f)
            curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
            reflectiveCurveToRelative(2.24f, -5f, 5f, -5f)
            reflectiveCurveToRelative(5f, 2.24f, 5f, 5f)
            reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f)
            close()
        }
    }.build().also { _visibilityIcon = it }

private var _visibilityOffIcon: ImageVector? = null
val Icons.Filled.VisibilityOff: ImageVector
    get() = _visibilityOffIcon ?: ImageVector.Builder(
        name = "Filled.VisibilityOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 7f)
            curveToRelative(2.76f, 0f, 5f, 2.24f, 5f, 5f)
            curveToRelative(0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.83f)
            lineToRelative(2.92f, 2.92f)
            curveToRelative(1.51f, -1.26f, 2.7f, -2.89f, 3.43f, -4.75f)
            curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f)
            curveToRelative(-1.4f, 0f, -2.74f, 0.25f, -3.98f, 0.7f)
            lineToRelative(2.16f, 2.16f)
            curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f)
            close()
            moveTo(2f, 4.27f)
            lineToRelative(2.28f, 2.28f)
            lineToRelative(0.46f, 0.46f)
            curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
            curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f)
            curveToRelative(1.55f, 0f, 3.03f, -0.3f, 4.38f, -0.84f)
            lineToRelative(0.42f, 0.42f)
            lineTo(19.73f, 22f)
            lineTo(21f, 20.73f)
            lineTo(3.27f, 3f)
            lineTo(2f, 4.27f)
            close()
        }
    }.build().also { _visibilityOffIcon = it }

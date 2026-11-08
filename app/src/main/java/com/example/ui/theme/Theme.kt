package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.BlockColorPreset
import com.example.data.model.CardType

private val KamDarkColorScheme = darkColorScheme(
    primary = KamPrimaryGold,
    onPrimary = KamOnPrimaryDark,
    primaryContainer = KamPrimaryContainerDark,
    onPrimaryContainer = KamOnPrimaryContainerGold,
    secondary = KamSecondaryTeal,
    onSecondary = KamOnSecondaryDark,
    secondaryContainer = KamSecondaryContainerDark,
    onSecondaryContainer = KamOnSecondaryContainerTeal,
    tertiary = KamTertiarySky,
    background = KamBackgroundDark,
    onBackground = KamTextPrimary,
    surface = KamSurfaceDark,
    onSurface = KamTextPrimary,
    surfaceVariant = KamSurfaceVariantDark,
    onSurfaceVariant = KamTextSecondary,
    outline = KamBorderDark,
    error = KamErrorRed,
    errorContainer = KamErrorContainerDark,
    onErrorContainer = KamOnErrorContainer
)

object KamSemanticColors {
    val onlineIndicator: Color = KamOnlineGreen
    val offlineIndicator: Color = KamOfflineAmber
    val elevatedSurface: Color = KamSurfaceElevatedDark
    val unassignedBlock: Color = BlockUnassigned
    val mutedText: Color = KamTextMuted

    fun blockColor(hex: String): Color {
        return when (BlockColorPreset.fromHex(hex)) {
            BlockColorPreset.BLUE -> BlockBlue
            BlockColorPreset.EMERALD -> BlockEmerald
            BlockColorPreset.VIOLET -> BlockViolet
            BlockColorPreset.AMBER -> BlockAmber
            BlockColorPreset.ROSE -> BlockRose
            BlockColorPreset.CYAN -> BlockCyan
            BlockColorPreset.ORANGE -> BlockOrange
        }
    }

    fun cardColor(cardType: CardType): Color {
        return when (cardType) {
            CardType.ATTENDANCE -> CardAttendanceColor
            CardType.QUIET -> CardQuietColor
            CardType.PARTICIPATION -> CardParticipationColor
            CardType.PRAISE -> CardPraiseColor
            CardType.GOLD -> CardGoldColor
            CardType.MEMORIZATION -> CardMemorizationColor
            CardType.CONFESSION -> CardConfessionColor
        }
    }
}

@Composable
fun KamLiteTheme(
    content: @Composable () -> Unit
) {
    // Enforce RTL from first launch across the entire application
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = KamDarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}

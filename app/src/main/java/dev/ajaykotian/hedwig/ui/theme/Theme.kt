package dev.ajaykotian.hedwig.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * App-specific colors that have no slot in Material's ColorScheme.
 * Read them with `HedwigTheme.colors.xyz`.
 */
@Immutable
data class HedwigColors(
    /** Top bar, tab row and their content. */
    val topBar: Color,
    val onTopBar: Color,
    val selectedTab: Color,
    /** Floating action buttons, send button, unread badges. */
    val accent: Color,
    /** The accent used for text and icons on the background: unread times, section labels, edit icons. */
    val accentText: Color,
    val chatBackground: Color,
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val secondaryText: Color,
    val readTick: Color,
    /** Search fields, the message input bar, grouped settings sections. */
    val panel: Color,
    /** Text fields placed on [panel]. */
    val field: Color,
    /** The selected or open chat in a list. */
    val selectedRow: Color,
    val divider: Color,
)

private val LightHedwigColors = HedwigColors(
    topBar = HedwigPurple,
    onTopBar = Color.White,
    selectedTab = Color.White,
    accent = HedwigPurple,
    accentText = HedwigPurple,
    chatBackground = ChatBackgroundLight,
    outgoingBubble = HedwigPurpleLight,
    incomingBubble = Color.White,
    secondaryText = TextSecondaryLight,
    readTick = ReadTickBlue,
    panel = SurfaceLight,
    field = Color.White,
    selectedRow = SelectedLight,
    divider = DividerLight,
)

private val DarkHedwigColors = HedwigColors(
    topBar = SurfaceHighDark,
    onTopBar = TextDark,
    selectedTab = HedwigLauncherBg,
    accent = HedwigPurple,
    accentText = HedwigLauncherBg, // the purple is too dark for text on a dark background
    chatBackground = ChatBackgroundDark,
    outgoingBubble = HedwigPurpleDark,
    incomingBubble = DarkSlate200,
    secondaryText = TextSecondaryDark,
    readTick = ReadTickBlue,
    panel = SurfaceDark,
    field = FieldDark,
    selectedRow = SelectedDark,
    divider = DividerDark,
)

// Material roles get the same tinted neutrals, so dialogs, menus, text fields and
// dividers match the custom colors above without per-screen styling.
private val LightColorScheme = lightColorScheme(
    primary = HedwigPurple,
    onPrimary = Color.White,
    primaryContainer = HedwigPurpleLight,
    onPrimaryContainer = HedwigPurple,
    secondary = HedwigPurpleLight,
    secondaryContainer = SelectedLight,
    onSecondaryContainer = TextLight,
    background = Color.White,
    onBackground = TextLight,
    surface = Color.White,
    onSurface = TextLight,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceLowLight,
    surfaceContainer = SurfaceLight,
    surfaceContainerHigh = SurfaceHighLight,
    surfaceContainerHighest = SurfaceHighestLight,
    outline = OutlineLight,
    outlineVariant = DividerLight,
)

// The old app never customized dark mode; this keeps the brand purple.
private val DarkColorScheme = darkColorScheme(
    primary = HedwigLauncherBg,
    onPrimary = Color(0xFF3A0080),
    primaryContainer = HedwigPurple,
    onPrimaryContainer = Color.White,
    secondaryContainer = SelectedDark,
    onSecondaryContainer = TextDark,
    background = BackgroundDark,
    onBackground = TextDark,
    surface = BackgroundDark,
    onSurface = TextDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = SurfaceLowestDark,
    surfaceContainerLow = SurfaceLowDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceHighDark,
    surfaceContainerHighest = FieldDark,
    outline = OutlineDark,
    outlineVariant = DividerDark,
)

private val LocalHedwigColors = staticCompositionLocalOf { LightHedwigColors }

@Composable
fun HedwigTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep the hedwig purple instead of wallpaper colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalHedwigColors provides if (darkTheme) DarkHedwigColors else LightHedwigColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

/** Same pattern as `MaterialTheme.colorScheme`: `HedwigTheme.colors.accent`. */
object HedwigTheme {
    val colors: HedwigColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHedwigColors.current
}

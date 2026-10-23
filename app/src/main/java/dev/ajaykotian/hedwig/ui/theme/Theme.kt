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
    val chatBackground: Color,
    val outgoingBubble: Color,
    val incomingBubble: Color,
    val secondaryText: Color,
    val readTick: Color,
)

private val LightHedwigColors = HedwigColors(
    topBar = HedwigPurple,
    onTopBar = Color.White,
    selectedTab = Color.White,
    accent = HedwigPurple,
    chatBackground = ChatBackgroundLight,
    outgoingBubble = HedwigPurpleLight,
    incomingBubble = Color.White,
    secondaryText = Gray200,
    readTick = ReadTickBlue,
)

private val DarkHedwigColors = HedwigColors(
    topBar = DarkSlate200,
    onTopBar = White200,
    selectedTab = HedwigLauncherBg,
    accent = HedwigPurple,
    chatBackground = ChatBackgroundDark,
    outgoingBubble = HedwigPurpleDark,
    incomingBubble = DarkSlate200,
    secondaryText = Gray200,
    readTick = ReadTickBlue,
)

private val LightColorScheme = lightColorScheme(
    primary = HedwigPurple,
    onPrimary = Color.White,
    primaryContainer = HedwigPurpleLight,
    onPrimaryContainer = HedwigPurple,
    secondary = HedwigPurpleLight,
    background = Color.White,
    surface = Color.White,
)

// The old app never customized dark mode; this keeps the brand purple.
private val DarkColorScheme = darkColorScheme(
    primary = HedwigLauncherBg,
    onPrimary = Color(0xFF3A0080),
    primaryContainer = HedwigPurple,
    onPrimaryContainer = Color.White,
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

package dev.ajaykotian.hedwig.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = HedwigPurple,
    onPrimary = Color.White,
    primaryContainer = HedwigPurpleLight,
    onPrimaryContainer = HedwigPurple,
    secondary = HedwigPurpleLight,
    background = Color.White,
    surface = Color.White,
)

// The old app never customized dark mode; this is a suggestion that keeps the brand purple.
private val DarkColorScheme = darkColorScheme(
    primary = HedwigLauncherBg,
    onPrimary = Color(0xFF3A0080),
    primaryContainer = HedwigPurple,
    onPrimaryContainer = Color.White,
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
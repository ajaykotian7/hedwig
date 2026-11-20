package dev.ajaykotian.hedwig.ui.components

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Sets the status bar icon colour for the screen that calls it. Every navigation destination calls this.
 *
 * - [onColoredTopBar] `true`: light icons, for screens under the purple (or dark) top bar.
 * - `false`: icons that follow the theme, for screens drawn on the plain background
 *   (dark icons in light mode, light icons in dark mode).
 *
 * There's deliberately no restore when the screen leaves. During a navigation transition the new
 * screen enters *before* the old one leaves, so restoring a saved value would undo the new screen's
 * choice. That's how Login ended up with white icons on white after a visit to a chat. Instead,
 * whichever screen entered last wins.
 */
@Composable
fun StatusBarIcons(onColoredTopBar: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return // previews have no window

    val lightIcons = onColoredTopBar || isSystemInDarkTheme()
    LaunchedEffect(view, lightIcons) {
        val window = (view.context as Activity).window
        // "Light status bars" means a light bar background, i.e. dark icons.
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !lightIcons
    }
}

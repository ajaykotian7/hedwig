package dev.ajaykotian.hedwig.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light status bar icons while this is in composition, for screens with a coloured top bar.
 * Restores the previous icons when the screen leaves.
 */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return // previews have no window

    DisposableEffect(view) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}
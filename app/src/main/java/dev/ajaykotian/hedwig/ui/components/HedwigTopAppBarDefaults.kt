package dev.ajaykotian.hedwig.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

object HedwigTopAppBarDefaults {
    /** The purple (light) / tinted dark bar used by Home, New chat and Chat. */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun colors(): TopAppBarColors {
        val colors = HedwigTheme.colors
        return TopAppBarDefaults.topAppBarColors(
            containerColor = colors.topBar,
            titleContentColor = colors.onTopBar,
            navigationIconContentColor = colors.onTopBar,
            actionIconContentColor = colors.onTopBar,
        )
    }
}

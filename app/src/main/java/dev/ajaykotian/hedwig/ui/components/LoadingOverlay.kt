package dev.ajaykotian.hedwig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ajaykotian.hedwig.ui.theme.GreyTranslucent
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

/**
 * Compose version of the old `lnr_loading` layout: a translucent grey scrim with a
 * 50dp spinner that also blocks touches on the screen behind it.
 * Place it last inside a Box so it draws on top: `if (isLoading) LoadingOverlay()`.
 */
@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GreyTranslucent)
            // Swallow taps so the content behind can't be used while loading.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(50.dp),
        )
    }
}

@Preview
@Composable
private fun LoadingOverlayPreview() {
    HedwigTheme {
        LoadingOverlay()
    }
}

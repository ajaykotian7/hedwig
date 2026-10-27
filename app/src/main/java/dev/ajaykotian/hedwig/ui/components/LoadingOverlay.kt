package dev.ajaykotian.hedwig.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import dev.ajaykotian.hedwig.ui.theme.GreyTranslucent
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    val focusManager = LocalFocusManager.current

    LaunchedEffect(isLoading) {
        if (isLoading) focusManager.clearFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        content()
        if (isLoading) {
            BackHandler() { }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(GreyTranslucent)
                    // Consume every touch so nothing underneath can be tapped.
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent().changes.forEach { it.consume() }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = HedwigTheme.colors.accent)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingOverlayPreview() {
    HedwigTheme {
        LoadingOverlay(isLoading = true) {
            Text("Screen content underneath")
        }
    }
}

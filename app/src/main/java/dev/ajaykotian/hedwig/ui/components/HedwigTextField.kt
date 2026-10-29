package dev.ajaykotian.hedwig.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

object HedwigTextFieldDefaults {
    val Shape = RoundedCornerShape(16.dp)

    @Composable
    fun colors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = HedwigTheme.colors.panel,
        unfocusedContainerColor = HedwigTheme.colors.panel,
        disabledContainerColor = HedwigTheme.colors.panel,
        focusedBorderColor = HedwigTheme.colors.accent,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        cursorColor = HedwigTheme.colors.accent,
    )
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = HedwigTheme.colors.secondaryText.copy(alpha = if (enabled) 1f else 0.38f),
        modifier = modifier,
    )
}
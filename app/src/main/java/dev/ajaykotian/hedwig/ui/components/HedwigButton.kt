package dev.ajaykotian.hedwig.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

/**
 * Compose version of the old `HedwigTheme.Button` style (btn_border_round.xml):
 * full width, 42dp tall, 4dp corners, primary color, bold uppercase 14sp white text.
 */
@Composable
fun HedwigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            // The old XML kept the same color when disabled; a faded one shows the state.
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HedwigButtonPreview() {
    HedwigTheme {
        HedwigButton(text = "Login", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HedwigButtonDisabledPreview() {
    HedwigTheme {
        HedwigButton(text = "Login", onClick = {}, enabled = false)
    }
}

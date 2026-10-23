package dev.ajaykotian.hedwig.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

/**
 * Compose version of the old `HedwigTheme.EditText` style (bg_edit_text.xml):
 * white surface, 8dp corners, 4dp elevation, min height 48dp, 12dp/4dp padding,
 * plus the close icon from the old theme as a "clear text" button.
 */
@Composable
fun HedwigTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    /** Like android:maxLength: longer input is cut off. */
    maxLength: Int? = null,
    showClearButton: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colors.surface,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(if (maxLength != null) it.take(maxLength) else it) },
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                    if (showClearButton && value.isNotEmpty() && enabled) {
                        IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(40.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close_24dp),
                                contentDescription = "Clear",
                                tint = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HedwigTextFieldPreview() {
    HedwigTheme {
        var mobile by remember { mutableStateOf("9876543210") }
        var otp by remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Enter Mobile Number", fontWeight = FontWeight.Bold)
            HedwigTextField(
                value = mobile,
                onValueChange = { mobile = it },
                maxLength = 10,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            Text("Enter Otp", fontWeight = FontWeight.Bold)
            HedwigTextField(
                value = otp,
                onValueChange = { otp = it },
                placeholder = "6-digit code",
                maxLength = 6,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

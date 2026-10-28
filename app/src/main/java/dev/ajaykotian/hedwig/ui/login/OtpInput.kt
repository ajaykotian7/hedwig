package dev.ajaykotian.hedwig.ui.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
internal fun OtpInput(
    otp: String,
    onOtpChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = LoginUIState.OTP_LENGTH,
    enabled: Boolean = true,
    isError: Boolean = false,
    onDone: () -> Unit = {}
) {
    BasicTextField(
        value = otp,
        onValueChange = onOtpChanged,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier.contentType(ContentType.SmsOtpCode),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else 0.6f),
            ) {
                repeat(length) { index ->
                    OtpBox(
                        digit = otp.getOrNull(index),
                        isActive = enabled && index == otp.length,
                        isError = isError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpBox(digit: Char?, isActive: Boolean, isError: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isActive -> HedwigTheme.colors.accent
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "otpBorder",
    )
    val background = when {
        isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        isActive -> MaterialTheme.colorScheme.background
        else -> HedwigTheme.colors.panel
    }
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(background)
            .border(if (isActive || isError) 2.dp else 1.5.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        when {
            digit != null -> Text(
                text = digit.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )

            isActive -> BlinkingCursor()
        }
    }
}

@Composable
private fun BlinkingCursor() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "cursorAlpha",
    )
    Box(
        Modifier
            .size(width = 2.dp, height = 26.dp)
            .graphicsLayer { alpha = cursorAlpha }
            .background(HedwigTheme.colors.accent),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtpInputPreview() {
    HedwigTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OtpInput(otp = "", onOtpChanged = {})
            OtpInput(otp = "482", onOtpChanged = {})
            OtpInput(otp = "482913", onOtpChanged = {})
            OtpInput(otp = "482910", onOtpChanged = {}, isError = true)
            OtpInput(otp = "482913", onOtpChanged = {}, enabled = false)
        }
    }
}

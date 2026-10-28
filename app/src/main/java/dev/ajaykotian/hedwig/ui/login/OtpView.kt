package dev.ajaykotian.hedwig.ui.login

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajaykotian.hedwig.ui.components.PrimaryButton
import dev.ajaykotian.hedwig.ui.components.rememberErrorShake
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme


@Composable
internal fun ColumnScope.OtpView(uiState: LoginUIState, onOtpChanged: (String) -> Unit, onOtpResend: () -> Unit, onOtpSubmitted: () -> Unit, onEditMobileNumber: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val subtitle = buildAnnotatedString {
        append("Sent by SMS to ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
            append("+91 ${uiState.mobileNumber}")
        }
        if (!uiState.isLoading) {           // the mock hides Edit while verifying
            append("  ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = "edit",
                    styles = TextLinkStyles(SpanStyle(color = HedwigTheme.colors.accentText, fontWeight = FontWeight.Bold)),
                ) { onEditMobileNumber() },
            ) { append("Edit") }
        }
    }
    BackButton(onClick = onEditMobileNumber, enabled = !uiState.isLoading)
    Spacer(Modifier.height(28.dp))
    StepHeader("Enter the code", subtitle)
    Spacer(Modifier.height(36.dp))
    OtpInput(
        otp = uiState.otp,
        onOtpChanged = onOtpChanged,
        enabled = !uiState.isLoading,
        isError = uiState.errorMessage != null,
        onDone = onOtpSubmitted,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
    )
    uiState.errorMessage?.let { ErrorRow(it, Modifier.padding(top = 16.dp)) }
    ResendRow(
        countdown = uiState.resendCountdown,
        enabled = uiState.canResend,
        onOtpResend = onOtpResend,
        modifier = Modifier.padding(top = 8.dp),
    )
    Spacer(Modifier.weight(1f))
    PrimaryButton(
        text = "Verify",
        onClick = onOtpSubmitted,
        enabled = uiState.canSubmitOtp,
        loading = uiState.isLoading,
        loadingText = "Verifying…"
    )
}

@Composable
private fun BackButton(onClick: () -> Unit, enabled: Boolean) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = HedwigTheme.colors.panel,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.size(44.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to phone number")
    }
}

@Composable
private fun OtpInput(
    otp: String,
    onOtpChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = LoginUIState.OTP_LENGTH,
    enabled: Boolean = true,
    isError: Boolean = false,
    onDone: () -> Unit = {}
) {

    val shake = rememberErrorShake(isError)

    BasicTextField(
        value = otp,
        onValueChange = onOtpChanged,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier
            .contentType(ContentType.SmsOtpCode)
            .offset { IntOffset(shake.value.dp.roundToPx(), 0) },
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

@Composable
private fun ErrorRow(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}

private fun formatCountdown(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun ResendRow(
    countdown: Int,
    enabled: Boolean,
    onOtpResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(targetState = countdown > 0, modifier = modifier, label = "resend") { waiting ->
        if (waiting) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = HedwigTheme.colors.secondaryText,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = buildAnnotatedString {
                        append("Resend code in ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFeatureSettings = "tnum",
                            ),
                        ) { append(formatCountdown(countdown)) }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = HedwigTheme.colors.secondaryText,
                )
            }
        } else {
            TextButton(
                onClick = onOtpResend,
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 0.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = HedwigTheme.colors.accentText),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Resend code", fontWeight = FontWeight.Bold)
            }
        }
    }
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

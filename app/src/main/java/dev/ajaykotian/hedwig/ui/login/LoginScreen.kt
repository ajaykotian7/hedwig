package dev.ajaykotian.hedwig.ui.login

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.PrimaryButton
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
fun LoginScreen(viewModel: LoginViewModel = screenViewModel { getLoginViewModel() }, onLoggedIn: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isOtpValidated) {
        if (uiState.isOtpValidated) onLoggedIn()
    }

    val activity = LocalActivity.current

    LoginContent(
        uiState,
        onMobileNumberChanged = viewModel::onMobileNumberChanged,
        onOtpChanged = viewModel::onOtpChanged,
        onMobileSubmitted = { activity?.let(viewModel::generateOtp) },
        resendOtp = { activity?.let(viewModel::resendOtp) },
        validateOtp = viewModel::validateOtp,
        onEditMobileNumber = viewModel::editMobileNumber
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUIState,
    onMobileNumberChanged: (String) -> Unit,
    onOtpChanged: (String) -> Unit,
    onMobileSubmitted: () -> Unit,
    resendOtp: () -> Unit,
    validateOtp: () -> Unit,
    onEditMobileNumber: () -> Unit,
) {

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .padding(24.dp),
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(HedwigTheme.colors.outgoingBubble)
            ) {
                Image(
                    painterResource(R.mipmap.ic_launcher_foreground),
                    null,
                    Modifier
                        .requiredSize(96.dp)
                        .align(Alignment.Center),
                )
            }
            Spacer(Modifier.height(36.dp))
            if (!uiState.isOtpGenerated) {
                PhoneNumberView(
                    uiState = uiState,
                    onValueChanged = onMobileNumberChanged,
                    onMobileSubmitted = onMobileSubmitted
                )
            } else {
                OtpView(
                    uiState,
                    onOtpChanged = onOtpChanged,
                    onOtpResend = resendOtp,
                    onOtpSubmitted = validateOtp,
                    onEditMobileNumber = onEditMobileNumber
                )
            }
        }
    }

}

@Composable
private fun ColumnScope.OtpView(uiState: LoginUIState, onOtpChanged: (String) -> Unit, onOtpResend: () -> Unit, onOtpSubmitted: () -> Unit, onEditMobileNumber: () -> Unit) {
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
    TextButton(onClick = onOtpResend, enabled = uiState.canResend) {
        Text(
            if (uiState.resendCountdown > 0) {
                "Resend OTP in 0:%02d".format(uiState.resendCountdown)
            } else {
                "Resend OTP"
            },
        )
    }
    uiState.errorMessage?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(Modifier.weight(1f))
    PrimaryButton(
        text = "Verify",
        onClick = onOtpSubmitted,
        enabled = uiState.canSubmitOtp,
        loading = uiState.isLoading,
        loadingText = "Verifying…"
    )
}


@Preview(showBackground = true)
@Composable
private fun LoginPhonePreview() {
    HedwigTheme {
        LoginContent(
            uiState = LoginUIState(mobileNumber = "9876587871"),
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoginPhonePreview() {
    HedwigTheme {
        LoginContent(
            uiState = LoginUIState(mobileNumber = "9876587871", errorMessage = "Wrong number"),
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun LoginPhoneErrorPreview() {
    HedwigTheme {
        LoginContent(
            uiState = LoginUIState(mobileNumber = "9876587871", errorMessage = "Wrong number"),
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginOtpPreview() {
    HedwigTheme {
        LoginContent(
            uiState = LoginUIState(
                mobileNumber = "9876543210",
                isOtpGenerated = true,
                otp = "123",
                resendCountdown = 42,
            ),
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginErrorPreview() {
    HedwigTheme {
        LoginContent(
            uiState = LoginUIState(
                mobileNumber = "9876543210",
                isOtpGenerated = true,
                otp = "111111",
                errorMessage = "The code you entered is incorrect.",
            ),
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginLoadingDarkPreview() {
    HedwigTheme {
        LoginContent(
            uiState = LoginUIState(mobileNumber = "9876543210", isOtpGenerated = true, isLoading = true),
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}

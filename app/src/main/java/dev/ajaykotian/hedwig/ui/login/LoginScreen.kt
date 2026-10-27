package dev.ajaykotian.hedwig.ui.login

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.LoadingOverlay
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
    LoadingOverlay(uiState.isLoading) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .safeDrawingPadding()
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "App icon",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )
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
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneNumberView(uiState: LoginUIState, onValueChanged: (String) -> Unit, onMobileSubmitted: () -> Unit) {

    //Code to get textfield into focus
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Text("Enter Mobile Number", style = MaterialTheme.typography.titleLarge, color = HedwigTheme.colors.accent)
    TextField(
        value = uiState.mobileNumber,
        onValueChange = onValueChanged,
        singleLine = true,
        prefix = { Text("+91 ") },
        placeholder = { Text("10-digit mobile number") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onMobileSubmitted() }),
        modifier = Modifier.focusRequester(focusRequester),
    )
    Button(
        onClick = onMobileSubmitted,
        enabled = uiState.canSubmitMobile,
        colors = ButtonDefaults.buttonColors(containerColor = HedwigTheme.colors.accent),
        modifier = Modifier.padding(top = 16.dp),
    ) { Text("Generate OTP") }
}


@Composable
fun OtpView(uiState: LoginUIState, onOtpChanged: (String) -> Unit, onOtpResend: () -> Unit, onOtpSubmitted: () -> Unit, onEditMobileNumber: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Text(
        text = buildAnnotatedString {
            append("Sent to ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("+91 ${uiState.mobileNumber}") }
            append(". ")
            withStyle(SpanStyle(color = HedwigTheme.colors.accentText)) { append("Wrong number?") }
        },
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.clickable(onClick = onEditMobileNumber),
    )

    TextField(
        value = uiState.otp,
        onValueChange = onOtpChanged,
        singleLine = true,
        placeholder = { Text("6-digit code") },
        textStyle = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 8.sp, textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onOtpSubmitted() }),
        modifier = Modifier.focusRequester(focusRequester),
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
    Button(
        onClick = onOtpSubmitted,
        enabled = uiState.canSubmitOtp,
        colors = ButtonDefaults.buttonColors(containerColor = HedwigTheme.colors.accent),
    ) { Text("Verify OTP") }
}


@Preview(showBackground = true)
@Composable
private fun LoginPhonePreview() {
    HedwigTheme {
        LoginContent(
            uiState = LoginUIState(mobileNumber = "98765"),
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
            uiState = LoginUIState(mobileNumber = "9876543210", isLoading = false),
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}

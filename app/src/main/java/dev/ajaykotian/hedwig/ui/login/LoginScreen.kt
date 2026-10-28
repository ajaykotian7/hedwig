package dev.ajaykotian.hedwig.ui.login

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.screenViewModel
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
    BackHandler(enabled = uiState.isOtpGenerated) {
        if (!uiState.isLoading) onEditMobileNumber()
    }
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.isOtpGenerated,
            transitionSpec = {
                val direction = if (targetState) 1 else -1   // forward → content moves left
                (slideInHorizontally { fullWidth -> fullWidth * direction } + fadeIn()) togetherWith
                        (slideOutHorizontally { fullWidth -> -fullWidth * direction } + fadeOut())
            },
            label = "loginStep",
        ) { showOtp ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .padding(24.dp),
            ) {
                if (showOtp) {
                    OtpView(
                        uiState,
                        onOtpChanged = onOtpChanged,
                        onOtpResend = resendOtp,
                        onOtpSubmitted = validateOtp,
                        onEditMobileNumber = onEditMobileNumber
                    )
                } else {
                    PhoneNumberView(
                        uiState = uiState,
                        onValueChanged = onMobileNumberChanged,
                        onMobileSubmitted = onMobileSubmitted
                    )
                }
            }
        }
    }

}

@Composable
private fun LoginPreview(state: LoginUIState) {
    HedwigTheme {
        LoginContent(
            uiState = state,
            onMobileNumberChanged = {},
            onOtpChanged = {},
            onMobileSubmitted = {},
            resendOtp = {},
            validateOtp = {},
            onEditMobileNumber = {},
        )
    }
}

@PhoneLightPreview
@Composable
private fun PhoneTypingPreview() = LoginPreview(LoginUIState(mobileNumber = "98765432"))

@PhoneDarkPreview
@Composable
private fun PhoneEmptyDarkPreview() = LoginPreview(LoginUIState())

@PhoneLightPreview
@Composable
private fun PhoneErrorPreview() =
    LoginPreview(LoginUIState(mobileNumber = "1234567890", errorMessage = "Invalid phone number."))

@PhoneLightPreview
@Composable
private fun PhoneSendingPreview() =
    LoginPreview(LoginUIState(mobileNumber = "9876543210", isLoading = true))

@PhoneLightPreview
@Composable
private fun CodeTypingPreview() = LoginPreview(
    LoginUIState(mobileNumber = "9876543210", isOtpGenerated = true, otp = "482", resendCountdown = 42),
)

@PhoneLightPreview
@Composable
private fun CodeVerifyingPreview() = LoginPreview(
    LoginUIState(mobileNumber = "9876543210", isOtpGenerated = true, otp = "482913", resendCountdown = 37, isLoading = true),
)

@PhoneDarkPreview
@Composable
private fun CodeWrongDarkPreview() = LoginPreview(
    LoginUIState(
        mobileNumber = "9876543210",
        isOtpGenerated = true,
        otp = "482910",
        errorMessage = "That code didn't match. Check the SMS and try again.",
    ),
)
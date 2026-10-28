package dev.ajaykotian.hedwig.ui.login

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

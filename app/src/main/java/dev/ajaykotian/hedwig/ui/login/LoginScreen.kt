package dev.ajaykotian.hedwig.ui.login

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private fun ColumnScope.PhoneNumberView(uiState: LoginUIState, onValueChanged: (String) -> Unit, onMobileSubmitted: () -> Unit) {

    //Code to get textfield into focus
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    StepHeader(
        title = "What's your number?",
        subtitle = "We'll text you a 6-digit code to make sure it's really you.",
    )
    Spacer(Modifier.height(36.dp))
    Text("Mobile number", style = MaterialTheme.typography.labelLarge, color = HedwigTheme.colors.secondaryText)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = uiState.mobileNumber,
        onValueChange = onValueChanged,
        enabled = !uiState.isLoading,
        isError = uiState.errorMessage != null,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
        leadingIcon = { CountryCodeChip() },
        supportingText = {
            when {
                uiState.errorMessage != null -> Text(uiState.errorMessage)
                uiState.mobileNumber.isNotEmpty() -> Text("${uiState.mobileNumber.length} of ${LoginUIState.MOBILE_LENGTH} digits")
            }
        },
        visualTransformation = PhoneNumberSpacing,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = HedwigTheme.colors.panel,
            unfocusedContainerColor = HedwigTheme.colors.panel,
            disabledContainerColor = HedwigTheme.colors.panel,
            focusedBorderColor = HedwigTheme.colors.accent,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = HedwigTheme.colors.accent,
        ),
        singleLine = true,
        placeholder = { Text("10-digit number") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onMobileSubmitted() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
    )
    Spacer(Modifier.weight(1f))
    PrimaryButton(
        text = "Generate OTP",
        onClick = onMobileSubmitted,
        enabled = uiState.canSubmitMobile,
        loading = uiState.isLoading,
        loadingText = "Sending…"
    )
}

@Composable
private fun CountryCodeChip() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(start = 12.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("IN", style = MaterialTheme.typography.labelSmall, color = HedwigTheme.colors.secondaryText)
                Text("+91", style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = HedwigTheme.colors.secondaryText,
                )
            }
        }
        VerticalDivider(
            modifier = Modifier
                .padding(start = 12.dp)
                .height(28.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}


private val PhoneNumberSpacing = VisualTransformation { text ->
    val digits = text.text
    val spaced = if (digits.length > 5) "${digits.take(5)} ${digits.drop(5)}" else digits
    TransformedText(
        AnnotatedString(spaced),
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = if (offset > 5) offset + 1 else offset
            override fun transformedToOriginal(offset: Int) = if (offset > 5) offset - 1 else offset
        },
    )
}

@Composable
private fun StepHeader(
    title: String,
    subtitle: AnnotatedString? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = HedwigTheme.colors.secondaryText)
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String, modifier: Modifier = Modifier) =
    StepHeader(title, AnnotatedString(subtitle), modifier)

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
    TextField(
        value = uiState.otp,
        onValueChange = onOtpChanged,
        enabled = !uiState.isLoading,
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

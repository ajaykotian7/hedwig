package dev.ajaykotian.hedwig.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.ui.components.FieldLabel
import dev.ajaykotian.hedwig.ui.components.HedwigTextFieldDefaults
import dev.ajaykotian.hedwig.ui.components.PrimaryButton
import dev.ajaykotian.hedwig.ui.components.rememberErrorShake
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
internal fun ColumnScope.PhoneNumberView(uiState: LoginUIState, onValueChanged: (String) -> Unit, onMobileSubmitted: () -> Unit) {

    //Code to get textfield into focus
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val isError = uiState.errorMessage != null

    val shake = rememberErrorShake(isError)

    OwlTile()
    Spacer(Modifier.height(28.dp))
    StepHeader(
        title = "What's your number?",
        subtitle = "We'll text you a 6-digit code to make sure it's really you.",
    )
    Spacer(Modifier.height(36.dp))
    FieldLabel("Mobile number")
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = uiState.mobileNumber,
        onValueChange = onValueChanged,
        enabled = !uiState.isLoading,
        isError = isError,
        shape = HedwigTextFieldDefaults.Shape,
        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
        leadingIcon = { CountryCodeChip() },
        supportingText = {
            when {
                isError -> Text(uiState.errorMessage)
                uiState.mobileNumber.isNotEmpty() -> Text("${uiState.mobileNumber.length} of ${LoginUIState.MOBILE_LENGTH} digits")
            }
        },
        visualTransformation = PhoneNumberSpacing,
        colors = HedwigTextFieldDefaults.colors(),
        singleLine = true,
        placeholder = { Text("10-digit number") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onMobileSubmitted() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .offset { IntOffset(shake.value.dp.roundToPx(), 0) },
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
private fun OwlTile() {
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
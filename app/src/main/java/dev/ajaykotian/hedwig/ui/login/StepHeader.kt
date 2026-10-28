package dev.ajaykotian.hedwig.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.ajaykotian.hedwig.ui.base.ComponentPreviews
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
internal fun StepHeader(
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
internal fun StepHeader(title: String, subtitle: String, modifier: Modifier = Modifier) =
    StepHeader(title, AnnotatedString(subtitle), modifier)


@ComponentPreviews
@Composable
private fun StepHeaderPreview() {
    HedwigTheme {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            StepHeader("What's your number?", "We'll text you a 6-digit code to make sure it's really you.")
            StepHeader(
                title = "Enter the code",
                subtitle = buildAnnotatedString {
                    append("Sent by SMS to ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("+91 98765 43210") }
                },
            )
        }
    }
}
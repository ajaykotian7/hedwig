package dev.ajaykotian.hedwig.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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
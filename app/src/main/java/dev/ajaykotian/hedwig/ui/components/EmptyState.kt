package dev.ajaykotian.hedwig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ajaykotian.hedwig.ui.base.ComponentPreviews
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme


/**
 * A centred icon, title and message for a screen or tab with nothing to show yet.
 * Fills whatever space [modifier] gives it and centres its content there.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = HedwigTheme.colors
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(color = colors.selectedRow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(52.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            modifier = Modifier.widthIn(max = 270.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

@ComponentPreviews
@Composable
private fun EmptyStatePreview() {
    HedwigTheme {
        EmptyState(
            icon = Icons.Outlined.Forum,
            title = "No chats yet",
            message = "Tap the new chat button to message anyone on hedwig.",
            modifier = Modifier.height(360.dp),
        )
    }
}
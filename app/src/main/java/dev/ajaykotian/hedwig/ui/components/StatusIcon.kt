package dev.ajaykotian.hedwig.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.ajaykotian.hedwig.ui.chat.MessageStatus
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
fun StatusIcon(status: MessageStatus, dimColor: Color) {
    val (icon, tint, description) = when (status) {
        MessageStatus.Pending -> Triple(Icons.Outlined.Schedule, dimColor, "Sending")
        MessageStatus.Sent -> Triple(Icons.Filled.Done, dimColor, "Sent")
        MessageStatus.Read -> Triple(Icons.Filled.DoneAll, HedwigTheme.colors.readTick, "Read")
    }
    Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(16.dp))
}
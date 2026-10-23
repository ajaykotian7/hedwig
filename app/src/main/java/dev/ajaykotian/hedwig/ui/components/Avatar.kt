package dev.ajaykotian.hedwig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ajaykotian.hedwig.ui.theme.AvatarColors
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme
import kotlin.math.absoluteValue

/**
 * Circle with the user's initials. The background color is derived from [id], so a
 * user keeps the same color everywhere in the app.
 */
@Composable
fun Avatar(
    name: String,
    id: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val initials = initialsOf(name)
    val color = AvatarColors[id.hashCode().absoluteValue % AvatarColors.size]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        if (initials.isEmpty()) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f),
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                // Scale the text with the circle: 48dp avatar -> 18sp text.
                fontSize = (size.value * 0.38f).sp,
            )
        }
    }
}

private fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() && it.first().isLetter() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

@Preview
@Composable
private fun AvatarPreview() {
    HedwigTheme {
        Avatar(name = "Jane Doe", id = "user-1")
    }
}

@Preview
@Composable
private fun AvatarNoNamePreview() {
    HedwigTheme {
        Avatar(name = "", id = "user-2")
    }
}

package dev.ajaykotian.hedwig.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** "+919876543210" -> "+91 98765 43210". Anything else is returned unchanged. */
fun formatPhoneNumber(e164: String): String =
    if (e164.startsWith("+91") && e164.length == 13) {
        "+91 ${e164.substring(3, 8)} ${e164.substring(8)}"
    } else {
        e164
    }


/** "8:02 PM", or "20:02" if the user picked 24-hour time in system settings. */
@Composable
fun rememberTimeFormat(): java.text.DateFormat {
    val context = LocalContext.current
    return remember(context) { android.text.format.DateFormat.getTimeFormat(context) }
}
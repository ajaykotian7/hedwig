package dev.ajaykotian.hedwig.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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


/** The calendar day of this timestamp, in the device's time zone. */
fun Timestamp.toLocalDate(): LocalDate =
    toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

/** "Today", "Yesterday", or a full date like "November 12, 2026" in the user's locale. */
fun formatDayHeader(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
}


/** Chats list time: "8:02 PM" today, "Yesterday", otherwise a short date like "11/12/26". */
@Composable
fun rememberChatListTime(timestamp: Timestamp?): String {
    val timeFormat = rememberTimeFormat()
    return remember(timestamp, timeFormat) {
        if (timestamp == null) return@remember ""
        val today = LocalDate.now()
        when (val date = timestamp.toLocalDate()) {
            today -> timeFormat.format(timestamp.toDate())
            today.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        }
    }
}
package dev.ajaykotian.hedwig.ui.base

/** "+919876543210" -> "+91 98765 43210". Anything else is returned unchanged. */
fun formatPhoneNumber(e164: String): String =
    if (e164.startsWith("+91") && e164.length == 13) {
        "+91 ${e164.substring(3, 8)} ${e164.substring(8)}"
    } else {
        e164
    }
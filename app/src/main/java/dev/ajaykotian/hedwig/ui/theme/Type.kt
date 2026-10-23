package dev.ajaykotian.hedwig.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Same text styles as the WhatsApp clone reference (system font).
val Typography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.W400, fontSize = 17.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.W700, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.W700, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.W500, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.W400, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.W400, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.W400, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.W500, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.W400, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.W500, fontSize = 11.sp, lineHeight = 16.sp),
)

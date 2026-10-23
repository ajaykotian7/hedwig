package dev.ajaykotian.hedwig.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.ajaykotian.hedwig.R

/** Same weights as the old app's res/font/montserrat_family.xml. */
val Montserrat = FontFamily(
    Font(R.font.montserrat_thin, FontWeight.ExtraLight),
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

private val Default = Typography()

/** Material's default text styles, all switched to Montserrat (like android:fontFamily in the old XML theme). */
val Typography = Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = Montserrat),
    displayMedium = Default.displayMedium.copy(fontFamily = Montserrat),
    displaySmall = Default.displaySmall.copy(fontFamily = Montserrat),
    headlineLarge = Default.headlineLarge.copy(fontFamily = Montserrat),
    headlineMedium = Default.headlineMedium.copy(fontFamily = Montserrat),
    headlineSmall = Default.headlineSmall.copy(fontFamily = Montserrat),
    titleLarge = Default.titleLarge.copy(fontFamily = Montserrat),
    titleMedium = Default.titleMedium.copy(fontFamily = Montserrat),
    titleSmall = Default.titleSmall.copy(fontFamily = Montserrat),
    bodyLarge = Default.bodyLarge.copy(fontFamily = Montserrat),
    bodyMedium = Default.bodyMedium.copy(fontFamily = Montserrat),
    bodySmall = Default.bodySmall.copy(fontFamily = Montserrat),
    labelLarge = Default.labelLarge.copy(fontFamily = Montserrat),
    labelMedium = Default.labelMedium.copy(fontFamily = Montserrat),
    labelSmall = Default.labelSmall.copy(fontFamily = Montserrat),
)

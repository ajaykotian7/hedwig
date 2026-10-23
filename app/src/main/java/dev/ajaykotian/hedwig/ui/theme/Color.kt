package dev.ajaykotian.hedwig.ui.theme

import androidx.compose.ui.graphics.Color

// Brand colors from the old hedwig project.
val HedwigPurple = Color(0xFF8A38FF)       // primary_color
val HedwigPurpleLight = Color(0xFFE9DBFD)  // primary_color_light (old colorAccent)
val HedwigLauncherBg = Color(0xFFDCC6FB)   // ic_launcher_background
val GreyTranslucent = Color(0x7A000000)    // grey_translucent (overlays)
val HedwigPurpleDark = Color(0xFF4A2580)   // outgoing bubbles in dark mode

// Neutral colors from the WhatsApp clone reference.
val Gray200 = Color(0xFF88898B)
val White200 = Color(0xFFE0E0E0)
val DarkSlate200 = Color(0xFF232D36)
val ChatBackgroundLight = Color(0xFFECE5DD)
val ChatBackgroundDark = Color(0xFF0B141A)
val ReadTickBlue = Color(0xFF34B7F1)

/** Colors for avatar backgrounds; one is picked per user from their id. */
val AvatarColors = listOf(
    Color(0xFF00A884),
    Color(0xFF53BDEB),
    Color(0xFFFF7A6B),
    Color(0xFFA791FF),
    Color(0xFFFFBC38),
    Color(0xFF02A698),
    Color(0xFFE26AB6),
    Color(0xFF7F66FF),
)

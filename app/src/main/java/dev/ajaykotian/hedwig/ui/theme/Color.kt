package dev.ajaykotian.hedwig.ui.theme

import androidx.compose.ui.graphics.Color

// Brand colors from the old hedwig project.
val HedwigPurple = Color(0xFF8A38FF)       // primary_color
val HedwigPurpleLight = Color(0xFFE9DBFD)  // primary_color_light (old colorAccent)
val HedwigLauncherBg = Color(0xFFDCC6FB)   // ic_launcher_background
val GreyTranslucent = Color(0x7A000000)    // grey_translucent (overlays)
val HedwigPurpleDark = Color(0xFF4A2580)   // outgoing bubbles in dark mode

// Chat colors from the WhatsApp clone reference.
val DarkSlate200 = Color(0xFF232D36)
val ChatBackgroundLight = Color(0xFFECE5DD)
val ChatBackgroundDark = Color(0xFF0B141A)
val ReadTickBlue = Color(0xFF34B7F1)

// Purple-tinted neutrals, shared with hedwig-web (public/css/styles.css).
val TextLight = Color(0xFF1D1B20)
val TextSecondaryLight = Color(0xFF6F6A78)
val SurfaceLight = Color(0xFFF6F2FC)       // panels, search fields, input bar
val SurfaceLowLight = Color(0xFFFAF7FE)
val SurfaceHighLight = Color(0xFFF1EBFA)
val SurfaceHighestLight = Color(0xFFECE5F6)
val SelectedLight = Color(0xFFF0E6FE)      // selected / pressed list row
val DividerLight = Color(0xFFEBE6F0)
val OutlineLight = Color(0xFF7E7887)

val BackgroundDark = Color(0xFF141218)
val TextDark = Color(0xFFE6E0E9)
val TextSecondaryDark = Color(0xFF9C96A4)
val SurfaceLowestDark = Color(0xFF0F0D13)
val SurfaceLowDark = Color(0xFF18161D)
val SurfaceDark = Color(0xFF1D1A22)
val SurfaceHighDark = Color(0xFF241F2A)
val FieldDark = Color(0xFF2A2631)          // text fields on SurfaceDark
val SelectedDark = Color(0xFF2A2236)
val DividerDark = Color(0xFF252229)
val OutlineDark = Color(0xFF8E8898)

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

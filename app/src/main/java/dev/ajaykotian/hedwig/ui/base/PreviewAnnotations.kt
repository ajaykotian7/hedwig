package dev.ajaykotian.hedwig.ui.base

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** A full screen at the mockups' phone size (390×844), light theme. */
@Preview(name = "Light", widthDp = 390, heightDp = 844, showBackground = true)
annotation class PhoneLightPreview

/** A full screen at the mockups' phone size (390×844), dark theme. */
@Preview(name = "Dark", widthDp = 390, heightDp = 844, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class PhoneDarkPreview

/** A component at phone width, rendered once in light and once in dark. */
@Preview(name = "Light", widthDp = 360, showBackground = true)
@Preview(name = "Dark", widthDp = 360, showBackground = true, backgroundColor = 0xFF141218, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ComponentPreviews

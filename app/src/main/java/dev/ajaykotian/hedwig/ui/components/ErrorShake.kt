package dev.ajaykotian.hedwig.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Shakes once and buzzes each time [isError] turns true.
 * Apply with Modifier.offset { IntOffset(shake.value.dp.roundToPx(), 0) }.
 */
@Composable
fun rememberErrorShake(isError: Boolean): Animatable<Float, AnimationVector1D> {
    val shake = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isError) {
        if (isError) {
            // LongPress, not Reject: many devices don't implement the reject effect.
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    -14f at 50
                    14f at 100
                    -10f at 150
                    10f at 200
                    -5f at 250
                    5f at 300
                },
            )
        }
    }
    return shake
}
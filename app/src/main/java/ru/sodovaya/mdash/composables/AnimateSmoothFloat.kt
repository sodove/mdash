package ru.sodovaya.mdash.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import ru.sodovaya.mdash.utils.wrap

@Composable
inline fun animateSmoothFloat(targetValue: Float): String {
    // Create an Animatable to hold the animated float value
    val animatedValue = remember { Animatable(targetValue) }
    
    // Launch a coroutine for animation whenever the target value changes
    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue,
            animationSpec = tween(
                delayMillis = 100,
                durationMillis = 300,
                easing = LinearEasing
            )        )
    }

    // Return the animated value as a State
    return animatedValue.value.wrap(2)
}
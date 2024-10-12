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
    val animatedValue = remember { Animatable(targetValue) }
    
    // Launch a coroutine for animation whenever the target value changes
    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue,
            animationSpec = tween(
                delayMillis = 50,
                durationMillis = 150,
                easing = LinearEasing
            )
        )
    }

    // Return the animated value as a State
    return animatedValue.value.wrap(2)
}
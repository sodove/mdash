package ru.sodovaya.mdash.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Composable
fun PercentageToColor(percentage: Float, highIsBetter: Boolean = true): Color {
    val startColor = if (highIsBetter) Color.Green else Color.Red
    val endColor = if (highIsBetter) Color.Red else Color.Green

    val percentageValue = (percentage / 100f).coerceIn(0f, 1f)

    return lerp(startColor, endColor, percentageValue)
}
package ru.sodovaya.mdash.composables

import androidx.compose.ui.graphics.Color
import kotlin.math.min

fun manipulateColor(color: Color, factor: Float): Color {
    val r = color.red * factor
    val g = color.green * factor
    val b = color.blue * factor
    return Color(
        min(r, 1f),
        min(g, 1f),
        min(b, 1f)
    )
}
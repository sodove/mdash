package ru.sodovaya.mdash.utils

fun convertToPercentage(currentValue: Float, minValue: Float, maxValue: Float): Float {
    if (maxValue - minValue <= 0f) return 0f
    return (((currentValue - minValue) / (maxValue - minValue)) * 100f).coerceIn(0.01f, 100f)
}
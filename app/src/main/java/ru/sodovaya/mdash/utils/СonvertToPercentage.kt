package ru.sodovaya.mdash.utils

fun convertToPercentage(currentValue: Float, minValue: Float, maxValue: Float): Float {
    if (currentValue < minValue) return 0f
    if (currentValue > maxValue) return 100f

    if (maxValue < minValue) return 0f
    if (maxValue - minValue == 0f) return 0f

    return ((currentValue - minValue) / (maxValue - minValue)) * 100f
}
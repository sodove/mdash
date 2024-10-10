package ru.sodovaya.mdash.utils

fun Int.toHumanReadableGear(): String {
    return when (this) {
        0 -> "Eco"
        1 -> "Drive"
        2 -> "Sport"
        else -> "unk"
    }
}
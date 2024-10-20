package ru.sodovaya.mdash.utils

fun String.CapitalizeWords(): String = split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

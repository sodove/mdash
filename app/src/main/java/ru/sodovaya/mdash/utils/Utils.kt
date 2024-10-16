package ru.sodovaya.mdash.utils

import android.annotation.SuppressLint


@SuppressLint("DefaultLocale")
fun Float.wrap(symbolsAfter: Int) = String.format("%.${symbolsAfter}f", this)

fun <T> safely(action: () -> T): T? = runCatching { action.invoke() }.getOrNull()
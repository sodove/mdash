package ru.sodovaya.mdash.composables

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import ru.sodovaya.mdash.ui.screens.ScooterData

val LocalScooterStatus: ProvidableCompositionLocal<ScooterData> =
    staticCompositionLocalOf { ScooterData() }
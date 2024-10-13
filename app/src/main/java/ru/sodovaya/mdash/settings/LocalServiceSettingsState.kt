package ru.sodovaya.mdash.settings

import androidx.compose.runtime.compositionLocalOf

val LocalServiceSettingsState = compositionLocalOf<ServiceSettingsState> {
    error("ServiceSettingsState not provided")
}
package ru.sodovaya.mdash.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.sodovaya.mdash.service.ServiceSettings

@Stable
class ServiceSettingsState(
    initialSettings: ServiceSettings,
    private val preferences: ServiceSettingsPreferences?
) {
    var settings by mutableStateOf(initialSettings)
        private set

    fun updateSettings(newSettings: ServiceSettings) {
        settings = newSettings
        preferences?.saveServiceSettings(newSettings)
    }
}
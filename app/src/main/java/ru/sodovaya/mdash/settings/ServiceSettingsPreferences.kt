package ru.sodovaya.mdash.settings

import android.content.Context
import android.content.SharedPreferences
import ru.sodovaya.mdash.service.ServiceSettings
import ru.sodovaya.mdash.service.WakelockVariant

class ServiceSettingsPreferences(
    private val context: Context,
    private val settingsState: ServiceSettingsState
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ServiceSettingsPrefs", Context.MODE_PRIVATE)

    init {
        val loadedSettings = loadServiceSettings()
        settingsState.updateSettings(loadedSettings)
    }

    fun saveServiceSettings(settings: ServiceSettings) {
        with(sharedPreferences.edit()) {
            putFloat("voltageMin", settings.voltageMin)
            putFloat("voltageMax", settings.voltageMax)
            putFloat("amperageMin", settings.amperageMin)
            putFloat("amperageMax", settings.amperageMax)
            putFloat("powerMin", settings.powerMin)
            putFloat("powerMax", settings.powerMax)
            putFloat("maximumVolumeAt", settings.maximumVolumeAt)
            putFloat("minimalVolume", settings.minimalVolume)
            putBoolean("volumeServiceEnabled", settings.volumeServiceEnabled)
            putString("wakelockVariant", settings.wakelockVariant.toString())
            apply()
        }
    }

    fun loadServiceSettings(): ServiceSettings {
        return ServiceSettings(
            voltageMin = sharedPreferences.getFloat("voltageMin", 39f),
            voltageMax = sharedPreferences.getFloat("voltageMax", 55f),
            amperageMin = sharedPreferences.getFloat("amperageMin", -20f),
            amperageMax = sharedPreferences.getFloat("amperageMax", 40f),
            powerMin = sharedPreferences.getFloat("powerMin", -500f),
            powerMax = sharedPreferences.getFloat("powerMax", 2000f),
            minimalVolume = sharedPreferences.getFloat("minimalVolume", 5f),
            maximumVolumeAt = sharedPreferences.getFloat("maximumVolumeAt", 30f),
            volumeServiceEnabled = sharedPreferences.getBoolean("volumeServiceEnabled", false),
            wakelockVariant = sharedPreferences.getString("wakelockVariant", null)
                ?.let { WakelockVariant.valueOf(it) } ?: WakelockVariant.HIDDEN_ALLOWED_CPU
        )
    }
}
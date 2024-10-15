package ru.sodovaya.mdash.ui.tabs

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.sodovaya.mdash.settings.LocalServiceSettingsState
import ru.sodovaya.mdash.ui.components.SettingSlider
import ru.sodovaya.mdash.ui.interfaces.ScreenTab
import kotlin.math.roundToInt

object SettingsTab : ScreenTab {
    override val tabName = "Settings"

    @Composable
    override fun Content() {
        val settingsState = LocalServiceSettingsState.current
        val settings = settingsState.settings

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .scrollable(rememberScrollState(), Orientation.Vertical),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voltage Min
            item {
                SettingSlider(label = "Voltage Min",
                    value = settings.voltageMin,
                    range = 30f..60f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(settings.copy(voltageMin = newValue))
                    })
            }

            // Voltage Max
            item {
                SettingSlider(label = "Voltage Max",
                    value = settings.voltageMax,
                    range = 30f..80f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(settings.copy(voltageMax = newValue))
                    })
            }

            // Amperage Min
            item {
                SettingSlider(label = "Amperage Min",
                    value = settings.amperageMin,
                    range = -50f..0f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                amperageMin = newValue.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Amperage Max
            item {
                SettingSlider(label = "Amperage Max",
                    value = settings.amperageMax,
                    range = 0f..80f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                amperageMax = newValue.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Temperature Min
            item {
                SettingSlider(label = "Temperature Min",
                    value = settings.temperatureMin,
                    range = -50f..50f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                temperatureMin = newValue.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Temperature Max
            item {
                SettingSlider(label = "Temperature Max",
                    value = settings.temperatureMax,
                    range = 0f..100f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                temperatureMax = newValue.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Power Min
            item {
                SettingSlider(label = "Power Min",
                    value = settings.powerMin,
                    range = -1000f..0f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                powerMin = newValue.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Power Max
            item {
                SettingSlider(label = "Power Max",
                    value = settings.powerMax,
                    range = 0f..3000f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                powerMax = newValue.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Maximum Volume At
            item {
                SettingSlider(label = "Maximum Volume At",
                    value = settings.maximumVolumeAt,
                    range = 0f..100f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                maximumVolumeAt = newValue.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Volume Service Enabled
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Volume Service Enabled")
                    Switch(checked = settings.volumeServiceEnabled, onCheckedChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                volumeServiceEnabled = newValue
                            )
                        )
                    })
                }
            }
        }
    }
}
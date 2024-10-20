package ru.sodovaya.mdash.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.sodovaya.mdash.service.WakelockVariant
import ru.sodovaya.mdash.settings.LocalServiceSettingsState
import ru.sodovaya.mdash.ui.components.SettingRangeSlider
import ru.sodovaya.mdash.ui.components.SettingSlider
import ru.sodovaya.mdash.ui.interfaces.ScreenTab
import ru.sodovaya.mdash.utils.CapitalizeWords
import ru.sodovaya.mdash.utils.wrap
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
            // Voltage
            item {
                SettingRangeSlider(label = "Voltage Range",
                    value = settings.voltageMin .. settings.voltageMax,
                    range = 30f..80f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                voltageMin = newValue.start.wrap(1).toFloat(),
                                voltageMax = newValue.endInclusive.wrap(1).toFloat()
                            )
                        )
                    })
            }

            // Amperage
            item {
                SettingRangeSlider(label = "Amperage Range",
                    value = settings.amperageMin .. settings.amperageMax,
                    range = -50f..80f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                amperageMin = newValue.start.roundToInt().toFloat(),
                                amperageMax = newValue.endInclusive.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Temperature
            item {
                SettingRangeSlider(label = "Temperature Range",
                    value = settings.temperatureMin .. settings.temperatureMax,
                    range = -50f..100f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                temperatureMin = newValue.start.roundToInt().toFloat(),
                                temperatureMax = newValue.endInclusive.roundToInt().toFloat()
                            )
                        )
                    })
            }

            // Power
            item {
                SettingRangeSlider(label = "Power Gauge",
                    value = settings.powerMin .. settings.powerMax,
                    range = -1500f..3000f,
                    onValueChange = { newValue ->
                        settingsState.updateSettings(
                            settings.copy(
                                powerMin = newValue.start.roundToInt().toFloat(),
                                powerMax = newValue.endInclusive.roundToInt().toFloat()
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

            // Wakelock Variant
            item {
                var expanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100))
                            .clickable { expanded = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Wakelock Variant:"
                        )
                        Text(
                            text = settings.wakelockVariant
                                .toString()
                                .replace("_", " ")
                                .CapitalizeWords()
                        )
                    }

                    DropdownMenu(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        WakelockVariant.entries.forEach { variant ->
                            DropdownMenuItem(
                                onClick = {
                                    settingsState.updateSettings(settings.copy(wakelockVariant = variant))
                                    expanded = false
                                },
                                text = {
                                    Text(text = variant.name.replace("_", " ").CapitalizeWords())
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
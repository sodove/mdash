package ru.sodovaya.mdash.ui.tabs

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import ru.sodovaya.mdash.composables.LocalScooterStatus
import ru.sodovaya.mdash.composables.PercentageToColor
import ru.sodovaya.mdash.composables.animateSmoothFloat
import ru.sodovaya.mdash.ui.components.Gauge
import ru.sodovaya.mdash.utils.convertToPercentage

object MainDashboardTab: Screen {
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val configuration = LocalConfiguration.current
        val scooterData = LocalScooterStatus.current
        val width = configuration.screenWidthDp.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Gauge(
                modifier = Modifier.padding(top = 10.dp).size(width * 0.7f),
                percentage = convertToPercentage(
                    currentValue = scooterData.speed.toFloat(),
                    minValue = 0f,
                    maxValue = scooterData.maximumSpeed.toFloat()
                ),
                strokeWidth = 100f,
                text = "${animateSmoothFloat(scooterData.speed.toFloat())}km/h"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .scrollable(rememberScrollState(), Orientation.Vertical)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).offset(y = (-10).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    maxItemsInEachRow = 3
                ) {
                    val voltagePercentage = convertToPercentage(
                        currentValue = scooterData.voltage.toFloat(),
                        minValue = 39f,
                        maxValue = 55f
                    )

                    Gauge(
                        modifier = Modifier.size(width * 0.25f),
                        percentage = voltagePercentage,
                        gaugeColor = PercentageToColor(
                            percentage = voltagePercentage,
                            highIsBetter = false
                        ),
                        text = animateSmoothFloat(scooterData.voltage.toFloat()) + "V",
                        additionalText = "SOC"
                    )

                    Gauge(
                        modifier = Modifier.size(width * 0.25f),
                        percentage = convertToPercentage(
                            currentValue = scooterData.amperage.toFloat(),
                            minValue = -20f,
                            maxValue = 40f
                        ),
                        text = animateSmoothFloat(scooterData.amperage.toFloat()) + "A",
                        additionalText = "AMP"
                    )

                    Gauge(
                        modifier = Modifier.size(width * 0.25f),
                        percentage = convertToPercentage(
                            currentValue = scooterData.temperature.toFloat(),
                            minValue = -10f,
                            maxValue = 100f
                        ),
                        text = animateSmoothFloat(scooterData.temperature.toFloat()) + "°C",
                        additionalText = "TEMP"
                    )
                }

                Box(modifier = Modifier.height(20.dp))

                Text("Battery ${scooterData.battery}")
                Text("Gear: ${scooterData.gear}")
                Text("Maximum Speed: ${scooterData.maximumSpeed}")
                Text("Power: ${scooterData.voltage * scooterData.amperage}")
                Text("Trip: ${scooterData.trip}")
                Text("Total Distance: ${scooterData.totalDist}")
            }
        }
    }
}
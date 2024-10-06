package ru.sodovaya.mdash.ui.tabs

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import ru.sodovaya.mdash.composables.SpaceSomehowPlease
import ru.sodovaya.mdash.ui.components.TubeSpeedometer

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
            TubeSpeedometer(
                modifier = Modifier.aspectRatio(1f).weight(1f).padding(horizontal = width * 0.1f),
                speed = scooterData.speed.toFloat(),
                speedText = { Text("Speed: ${scooterData.speed}") },
                maxSpeed = scooterData.maximumSpeed.toFloat(),
                minSpeed = 0f
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .scrollable(rememberScrollState(), Orientation.Vertical)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.SpaceSomehowPlease,
                    maxItemsInEachRow = 3
                ) {
                    TubeSpeedometer(
                        modifier = Modifier.height(80.dp).weight(1f),
                        barWidth = 15.dp,
                        maxSpeed = 54.6f,
                        minSpeed = 54.6f - 40f,
                        speedText = {
                            Text(
                                text = "SoC: ${scooterData.voltage}",
                                maxLines = 1
                            )
                        },
                        speed = scooterData.voltage.toFloat()
                    )
                    TubeSpeedometer(
                        modifier = Modifier.height(80.dp).weight(1f),
                        barWidth = 15.dp,
                        maxSpeed = 40f,
                        minSpeed = -40f,
                        speedText = {
                            Text(
                                text = "Amp: ${scooterData.amperage}",
                                maxLines = 1
                            )
                        },
                        speed = scooterData.amperage.toFloat()
                    )
                    TubeSpeedometer(
                        modifier = Modifier.height(80.dp).weight(1f),
                        barWidth = 15.dp,
                        maxSpeed = 100f,
                        minSpeed = -10f,
                        speedText = {
                            Text(
                                text = "Temp: ${scooterData.temperature}",
                                maxLines = 1
                            )
                        },
                        speed = scooterData.temperature.toFloat()
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
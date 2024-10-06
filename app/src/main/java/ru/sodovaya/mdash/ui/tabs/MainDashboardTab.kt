package ru.sodovaya.mdash.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import ru.sodovaya.mdash.ui.components.Meter
import ru.sodovaya.mdash.ui.screens.ScooterData

data class MainDashboardTab(
    val scooterData: ScooterData
): Screen {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Meter(
                modifier = Modifier.size(192.dp),
                inputValue = scooterData.speed,
                name = "Speed",
                maximumValue = scooterData.maximumSpeed.toFloat(),
                progressColors = listOf(Color.Green, Color.Red),
                innerGradient = Color.Green
            )
            Text("Battery ${scooterData.battery}")
            Text("Speed: ${scooterData.speed}")
            Text("Gear: ${scooterData.gear}")
            Text("Maximum Speed: ${scooterData.maximumSpeed}")
            Text("Voltage: ${scooterData.voltage}")
            Text("Amperage: ${scooterData.amperage}")
            Text("Power: ${scooterData.voltage * scooterData.amperage}")
            Text("Temperature: ${scooterData.temperature}")
            Text("Trip: ${scooterData.trip}")
            Text("Total Distance: ${scooterData.totalDist}")
        }
    }
}
package ru.sodovaya.mdash.bt

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.sodovaya.mdash.service.BluetoothForegroundService
import ru.sodovaya.mdash.ui.components.Meter

@SuppressLint("MissingPermission")
@Composable
fun MainScreen() {
    var selectedDevice by remember {
        mutableStateOf<BluetoothDevice?>(null)
    }
    BluetoothPermissionsWrapper {
        AnimatedContent(targetState = selectedDevice, label = "Selected device") { device ->
            if (device == null) {
                // Scans for BT devices and handles clicks (see FindDeviceSample)
                FindDevicesScreen {
                    selectedDevice = it
                }
            } else {
                // Once a device is selected show the UI and try to connect device
                ConnectDeviceScreen(device = device) {
                    selectedDevice = null
                }
            }
        }
    }
}

@SuppressLint("InlinedApi")
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ConnectDeviceScreen(device: BluetoothDevice, onClose: () -> Unit) {
    val context = LocalContext.current
    var scooterData by remember { mutableStateOf(ScooterData()) }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val received = intent?.getByteArrayExtra("data")
            received?.let { data ->
                parseScooterData(data)?.let { parsed -> scooterData = parsed }
            }
        }

        private fun parseScooterData(value: ByteArray): ScooterData? {
            return if (value.size != 25) {
                null
            } else {
                if (value[1] == 0x00.toByte()) {
                    val speed =
                        ((value[6].toInt() and 0xFF) shl 8) or (value[7].toInt() and 0xFF)
                    val voltage = (value[10].toInt() shl 8) or (value[11].toInt() and 0xFF)
                    val amperage = (value[12].toInt() shl 8) or (value[13].toInt() and 0xFF)
                    val tripDistance = (value[16].toInt() shl 8) or (value[17].toInt() and 0xFF)
                    val totalDistance =
                        (value[18].toInt() shl 16) or (value[19].toInt() shl 8) or (value[20].toInt() and 0xFF)
                    scooterData.copy(
                        gear = value[4].toInt(),
                        battery = value[5].toInt(),
                        speed = speed / 1000.0,
                        voltage = voltage / 10.0,
                        amperage = amperage / 100.0,
                        temperature = value[14].toInt(),
                        trip = tripDistance / 10.0,
                        totalDist = totalDistance / 10.0
                    )
                } else {
                    scooterData.copy(
                        maximumSpeed = when (scooterData.gear) {
                            0 -> value[4].toInt()
                            1 -> value[5].toInt()
                            2 -> value[6].toInt()
                            else -> 0
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(device) {
        val intent = Intent(context, BluetoothForegroundService::class.java).apply {
            putExtra("device", device)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
            Log.d("Service", "Started service")
        } else {
            context.startService(intent)
            Log.d("Service", "Started service")
        }
        context.registerReceiver(
            receiver,
            IntentFilter("BluetoothData"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_EXPORTED else 0
        ).run { Log.d("Service", "Trying to register receiver: $this") }
    }

    DisposableEffect(Unit) {
        onDispose {
            disposeService(context, receiver)
        }
    }

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

        Button(onClick = { disposeService(context, receiver); onClose.invoke() }) {
            Text(text = "Close")
        }
    }
}

fun disposeService(context: Context, receiver: BroadcastReceiver) {
    try {
        context.unregisterReceiver(receiver)
    } catch (e: IllegalArgumentException) {
        Log.e("MainScreen", "fuck something went 50\\50 wrong")
    }

    val stopIntent = Intent(context, BluetoothForegroundService::class.java)
    context.stopService(stopIntent)
}

data class ScooterData(
    val battery: Int = 0,
    val speed: Double = 0.0,
    val gear: Int = 0,
    val maximumSpeed: Int = 0,
    val voltage: Double = 0.0,
    val amperage: Double = 0.0,
    val temperature: Int = 0,
    val trip: Double = 0.0,
    val totalDist: Double = 0.0
)

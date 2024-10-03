package ru.sodovaya.mdash.bt

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import ru.sodovaya.mdash.bt.Utils.READ_UUID
import ru.sodovaya.mdash.bt.Utils.SEND_UUID
import ru.sodovaya.mdash.bt.Utils.SERVICE_UUID
import ru.sodovaya.mdash.ui.components.Meter
import java.util.UUID

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
    val scope = rememberCoroutineScope()

    // Keeps track of the last connection state with the device
    var state by remember(device) {
        mutableStateOf<DeviceConnectionState?>(null)
    }

    // This effect will handle the connection and notify when the state changes
    BLEConnectEffect(device = device) {
        // update our state to recompose the UI
        state = it
    }

    // Timer effect to call sendNoise every 500 ms
    LaunchedEffect(state?.gatt, state?.characteristicWrite) {
        if (state?.gatt != null && state?.characteristicWrite != null) {
            while (true) {
                delay(500)  // 500 milliseconds delay (0.5 seconds)
                sendNoise(state?.gatt!!, state?.characteristicWrite!!)
            }
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

        state?.scooterData?.let {
            Meter(
                modifier = Modifier.size(192.dp),
                inputValue = it.speed,
                name = "Speed",
                maximumValue = it.maximumSpeed.toFloat(),
                progressColors = listOf(Color.Green, Color.Red),
                innerGradient = Color.Green
            )
            Text("Battery ${it.battery}")
            Text("speed: ${it.speed}")
            Text("gear: ${it.gear}")
            Text("maximumSpeed: ${it.maximumSpeed}")
            Text("voltage: ${it.voltage}")
            Text("amperage: ${it.amperage}")
            Text("power: ${it.voltage * it.amperage}")
            Text("temperature: ${it.temperature}")
            Text("trip: ${it.trip}")
            Text("totalDist: ${it.totalDist}")
        }
        Button(onClick = onClose) {
            Text(text = "Close")
        }
    }
}

@SuppressLint("MissingPermission")
private fun sendNoise(
    gatt: BluetoothGatt,
    characteristic: BluetoothGattCharacteristic,
) {
    val data = byteArrayOf(0xFF.toByte())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        gatt.writeCharacteristic(
            characteristic,
            data,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
        )
    } else {
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        @Suppress("DEPRECATION")
        characteristic.value = data
        @Suppress("DEPRECATION")
        gatt.writeCharacteristic(characteristic)
    }
}

internal fun Int.toConnectionStateString() = when (this) {
    BluetoothProfile.STATE_CONNECTED -> "Connected"
    BluetoothProfile.STATE_CONNECTING -> "Connecting"
    BluetoothProfile.STATE_DISCONNECTED -> "Disconnected"
    BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting"
    else -> "N/A"
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

private data class DeviceConnectionState(
    val gatt: BluetoothGatt?,
    val connectionState: Int,
    val characteristicRead: BluetoothGattCharacteristic? = null,
    val characteristicWrite: BluetoothGattCharacteristic? = null,
    val services: List<BluetoothGattService> = emptyList(),
    val messageSent: Boolean = false,
    val messageReceived: String = "",
    val scooterData: ScooterData = ScooterData()
) {
    companion object {
        val None = DeviceConnectionState(gatt = null, connectionState = -1)
    }
}

@SuppressLint("InlinedApi")
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
private fun BLEConnectEffect(
    device: BluetoothDevice,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onStateChange: (DeviceConnectionState) -> Unit,
) {
    val context = LocalContext.current
    val currentOnStateChange by rememberUpdatedState(onStateChange)

    // Keep the current connection state
    var state by remember {
        mutableStateOf(DeviceConnectionState.None)
    }

    DisposableEffect(lifecycleOwner, device) {
        // This callback will notify us when things change in the GATT connection so we can update
        // our state
        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt,
                status: Int,
                newState: Int,
            ) {
                super.onConnectionStateChange(gatt, status, newState)
                state = state.copy(gatt = gatt, connectionState = newState)
                gatt.discoverServices()

                if (status != BluetoothGatt.GATT_SUCCESS) {
                    val msg = "An error happened: $status"
                    state = state.copy(messageReceived = msg)
                    Log.e("BLEConnectEffect", msg)
                }

                currentOnStateChange(state)
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                state = state.copy(gatt = gatt)
                currentOnStateChange(state)
            }

            @Suppress("DEPRECATION")
            private fun enableNotifications() {
                val characteristic = state.gatt?.getService(SERVICE_UUID)?.getCharacteristic(READ_UUID)
                characteristic?.let {
                    state.gatt?.setCharacteristicNotification(it, true)

                    // Some devices require a descriptor to enable notifications
                    val descriptor = it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor?.let { desc ->
                        desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        state.gatt?.writeDescriptor(desc)
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                state = state.copy(services = gatt.services)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val characteristicRead = gatt.getService(SERVICE_UUID)?.getCharacteristic(READ_UUID)
                    val characteristicWrite = gatt.getService(SERVICE_UUID)?.getCharacteristic(SEND_UUID)
                    if (characteristicWrite != null && characteristicRead != null) {
                        state = state.copy(characteristicWrite = characteristicWrite, characteristicRead = characteristicRead)
                        enableNotifications()
                    }
                }
                currentOnStateChange(state)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int,
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                state = state.copy(messageSent = status == BluetoothGatt.GATT_SUCCESS)
                currentOnStateChange(state)
            }

            @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                doOnRead(characteristic.value)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int,
            ) {
                super.onCharacteristicRead(gatt, characteristic, value, status)
                doOnRead(value)
            }

            @Deprecated("Deprecated in Java")
            @Suppress("DEPRECATION")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                Log.d("onCharacteristicChanged", "Characteristic changed")
                if (characteristic.uuid == READ_UUID) {
                    val data = characteristic.value
                    doOnRead(data)
                }
            }

            private fun doOnRead(value: ByteArray) {
                val output = StringBuilder()
                output.append("\n")

                // Ensure the length of the byte array matches expected values
                if (value.size != 25) {
                    output.append("Invalid data length. Expected 25 bytes, but got ${value.size}\n")
                } else {
                    if (value[1] == 0x00.toByte()) {
                        val speed = ((value[6].toInt() and 0xFF) shl 8) or (value[7].toInt() and 0xFF)
                        val voltage = (value[10].toInt() shl 8) or (value[11].toInt() and 0xFF)
                        val amperage = (value[12].toInt() shl 8) or (value[13].toInt() and 0xFF)
                        val tripDistance = (value[16].toInt() shl 8) or (value[17].toInt() and 0xFF)
                        val totalDistance =
                            (value[18].toInt() shl 16) or (value[19].toInt() shl 8) or (value[20].toInt() and 0xFF)
                        state = state.copy(
                            scooterData = state.scooterData.copy(
                                gear = value[4].toInt(),
                                battery = value[5].toInt(),
                                speed = speed / 1000.0,
                                voltage = voltage / 10.0,
                                amperage = amperage / 100.0,
                                temperature = value[14].toInt(),
                                trip = tripDistance / 10.0,
                                totalDist = totalDistance / 10.0
                            )
                        )
                    } else {
                        output.append("Speed for 1st gear: %d km/h\n".format(value[4].toInt()))
                        output.append("Speed for 2nd gear: %d km/h\n".format(value[5].toInt()))
                        output.append("Speed for 3rd gear: %d km/h\n".format(value[6].toInt()))
                        state = state.copy(
                            scooterData = state.scooterData.copy(
                                maximumSpeed = when (state.scooterData.gear) {
                                    0 -> value[4].toInt()
                                    1 -> value[5].toInt()
                                    2 -> value[6].toInt()
                                    else -> 0
                                }
                            )
                        )
                    }
                    // Update the state
                    currentOnStateChange(state)
                }


            }
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (state.gatt != null) {
                    // If we previously had a GATT connection let's reestablish it
                    state.gatt?.connect()
                } else {
                    // Otherwise create a new GATT connection
                    state = state.copy(gatt = device.connectGatt(context, false, callback))
                }
                state.gatt?.discoverServices()
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                state.gatt?.disconnect()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer and close the connection
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            state.gatt?.close()
        }
    }
}

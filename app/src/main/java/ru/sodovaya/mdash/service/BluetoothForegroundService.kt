package ru.sodovaya.mdash.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import ru.sodovaya.mdash.R
import ru.sodovaya.mdash.settings.ServiceSettingsPreferences
import ru.sodovaya.mdash.settings.ServiceSettingsState
import ru.sodovaya.mdash.utils.ParseScooterData
import ru.sodovaya.mdash.utils.READ_UUID
import ru.sodovaya.mdash.utils.SEND_UUID
import ru.sodovaya.mdash.utils.SERVICE_UUID
import ru.sodovaya.mdash.utils.convertToPercentage
import ru.sodovaya.mdash.utils.midway
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.timer
import kotlin.math.roundToInt

class BluetoothForegroundService : Service() {
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var wakeLock: PowerManager.WakeLock? = null

    private var gatt: BluetoothGatt? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null
    private var characteristicRead: BluetoothGattCharacteristic? = null
    private var isAlive = false
    private var scooterData = ScooterData()
    private var settings = ServiceSettings()
    private var noiseTimer: Timer? = null
    private var volumeTimer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val settingsState = ServiceSettingsState(ServiceSettings(), null)
        settings = ServiceSettingsPreferences(this, settingsState).loadServiceSettings()

        val deviceAddress: String? = intent?.getStringExtra("device")

        val wakelockLevel = when (settings.wakelockVariant) {
            WakelockVariant.KEEP_SCREEN_ON -> PowerManager.PARTIAL_WAKE_LOCK
            WakelockVariant.HIDDEN_ALLOWED_CPU -> PowerManager.PARTIAL_WAKE_LOCK
            WakelockVariant.DISABLED -> 0
        }

        volumeThreadWorker()

        if (wakelockLevel != 0) {
            wakeLock?.release()
            wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(wakelockLevel, "EndlessService::lock").apply {
                        acquire(5 * 60 * 1000L /*5 minutes*/)
                    }
                }
        }

        if (deviceAddress != null) {
            isAlive = true
            connect(deviceAddress)
        }
        startForeground(
            /* id = */ 1,
            /* notification = */ createNotification(),
        )
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "BLUETOOTH_SERVICE_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                /* id = */ notificationChannelId,
                /* name = */ "MDash Service",
                /* importance = */ NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "MDash Service"
            notificationChannel.enableVibration(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(
            /* context = */ this,
            /* channelId = */ notificationChannelId
        ).setContentTitle("MDash Status")
            .setContentText("Foreground service is active.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        gatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                scooterData = scooterData.copy(
                    deviceName = gatt.device.name.midway(),
                    isConnected = newState.toConnectionStateString()
                )
                val intent = Intent("BluetoothData")
                intent.putExtra("data", scooterData)
                sendBroadcast(intent)

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (isAlive.not())
                        onDestroy()
                    else
                        connect(device.address)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                characteristicWrite = gatt.getService(SERVICE_UUID)?.getCharacteristic(SEND_UUID)
                characteristicRead = gatt.getService(SERVICE_UUID)?.getCharacteristic(READ_UUID)

                if (characteristicWrite == null || characteristicRead == null) {
                    scooterData = scooterData.copy(isConnected = "Services failed")
                    val dataIntent = Intent("BluetoothData")
                    dataIntent.putExtra("data", scooterData)
                    sendBroadcast(dataIntent)
                    onDestroy()
                    return
                }

                enableNotifications(characteristicRead!!)

                noiseTimer?.cancel()
                noiseTimer = timer(period = 500) {
                    sendNoise(gatt, characteristicWrite!!)
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, data: ByteArray
            ) {
                if (characteristic.uuid == READ_UUID) {
                    scooterData = scooterData.copy(isConnected = "Got info")
                    ParseScooterData(scooterData = scooterData, value = data)?.let {
                        wakeLock?.acquire(5*60*1000L)
                        scooterData = it
                    }
                    val dataIntent = Intent("BluetoothData")
                    dataIntent.putExtra("data", scooterData)
                    sendBroadcast(dataIntent)
                }
            }

            private fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
                val intent = Intent("BluetoothData")
                intent.putExtra("connection", "Updating info")
                sendBroadcast(intent)
                if (gatt != null) {
                    gatt!!.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt!!.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt!!.writeDescriptor(descriptor)
                    }
                }
            }
        })

        gatt?.requestMtu(2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        isAlive = false
        try {
            noiseTimer?.cancel()
            volumeTimer?.cancel()
            gatt?.close()
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: Exception) {
            Log.d("TAG", "Service stopped without being started: ${e.message}")
        }
    }

    private fun sendNoise(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {
        val data = byteArrayOf(0xa5.toByte(), 0x02.toByte(), 0xfd.toByte(), 0x5a.toByte())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            )
        } else {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.value = data
            gatt.writeCharacteristic(characteristic)
        }
    }

    private fun volumeThreadWorker() {
        val audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(STREAM_MUSIC)

        volumeTimer?.cancel()
        if (settings.volumeServiceEnabled) {
            volumeTimer = timer(period = 150) {
                val minimalVolume = settings.minimalVolume
                val maximumVolumeAt = settings.maximumVolumeAt
                val speed = scooterData.speed
                val calculatedVolume = run {
                    (convertToPercentage(
                        currentValue = speed.toFloat(),
                        minValue = 0f,
                        maxValue = maximumVolumeAt
                    ) / 100 * maxVolume
                    ).coerceIn(minimalVolume, maxVolume.toFloat())
                }

                audioManager.setStreamVolume(STREAM_MUSIC, calculatedVolume.roundToInt(), 0)
            }
        }
    }

    private fun connect(deviceAddress: String) {
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        device?.address?.let { Log.d("BFGS", it) }
        device?.let { connectToDevice(it) }
    }

    private fun Int.toConnectionStateString() = when (this) {
        BluetoothProfile.STATE_CONNECTED -> "Connected"
        BluetoothProfile.STATE_CONNECTING -> "Connecting"
        BluetoothProfile.STATE_DISCONNECTED -> "Disconnected"
        BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting"
        else -> "N/A"
    }
}
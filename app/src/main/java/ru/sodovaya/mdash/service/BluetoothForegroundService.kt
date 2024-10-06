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
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ru.sodovaya.mdash.R
import ru.sodovaya.mdash.utils.READ_UUID
import ru.sodovaya.mdash.utils.SEND_UUID
import ru.sodovaya.mdash.utils.SERVICE_UUID
import java.util.UUID
import kotlin.concurrent.timer

class BluetoothForegroundService : Service() {
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var gatt: BluetoothGatt? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null
    private var characteristicRead: BluetoothGattCharacteristic? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BFGS", "Service started")
        val deviceAddress: String? = intent?.getStringExtra("device")
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        device?.address?.let { Log.d("BFGS", it) }
        device?.let { connectToDevice(it) }

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
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                characteristicWrite = gatt.getService(SERVICE_UUID)?.getCharacteristic(SEND_UUID)
                characteristicRead = gatt.getService(SERVICE_UUID)?.getCharacteristic(READ_UUID)

                enableNotifications(characteristicRead!!)
                timer(period = 500) {
                    sendNoise(gatt, characteristicWrite!!)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == READ_UUID) {
                    val data = characteristic.value
                    val intent = Intent("BluetoothData")
                    intent.putExtra("data", data)
                    sendBroadcast(intent)
                }
            }

            @Suppress("DEPRECATION")
            private fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
                if (gatt != null) {
                    gatt!!.setCharacteristicNotification(characteristic, true)
                    val descriptor =
                        characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt!!.writeDescriptor(descriptor)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        gatt?.close()
    }

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
}
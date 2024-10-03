package ru.sodovaya.mdash

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Parcelable
import java.util.UUID

object Utils {
    val SERVICE_UUID = UUID.fromString("0000f1f0-0000-1000-8000-00805f9b34fb")
    val READ_UUID = UUID.fromString("0000f1f2-0000-1000-8000-00805f9b34fb")
    val SEND_UUID = UUID.fromString("0000f1f1-0000-1000-8000-00805f9b34fb")

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }
}
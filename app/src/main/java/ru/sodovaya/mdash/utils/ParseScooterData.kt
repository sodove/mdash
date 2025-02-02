@file:OptIn(ExperimentalStdlibApi::class)

package ru.sodovaya.mdash.utils

import android.util.Log
import ru.sodovaya.mdash.service.ScooterData

fun ParseScooterData(scooterData: ScooterData = ScooterData(), value: ByteArray): ScooterData? {
    Log.d("ParseScooterData", "Received new bytearray: ${value.toHexString(HexFormat.Default)}")

    val updatedCache = (scooterData.partialDataCache ?: byteArrayOf()) + value

    return if (updatedCache.size >= 25) {
        val fullData = updatedCache.take(25).toByteArray()

        parseFullData(scooterData, fullData)?.copy(
            partialDataCache = null
        )
    } else {
        scooterData.copy(partialDataCache = updatedCache)
    }
}

private fun parseFullData(scooterData: ScooterData, value: ByteArray): ScooterData? {
    if (value.size != 25) return null

    return if (value[1] == 0x00.toByte()) {
        val speed = ((value[6].toInt() and 0xFF) shl 8) or (value[7].toInt() and 0xFF)
        val voltage = (value[10].toInt() shl 8) or (value[11].toInt() and 0xFF)
        val amperage = if (value[12].toInt() < 128) {
            ((value[12].toInt() shl 8) or (value[13].toInt() and 0xFF)) / 64.0
        } else {
            (65535 - ((value[12].toInt() shl 8) or (value[13].toInt() and 0xFF))) * -1 / 64.0
        }
        val tripDistance = (value[16].toInt() shl 8) or (value[17].toInt() and 0xFF)
        val totalDistance = (value[18].toInt() shl 16) or (value[19].toInt() shl 8) or (value[20].toInt() and 0xFF)

        scooterData.copy(
            gear = value[4].toInt(),
            battery = value[5].toInt(),
            speed = speed / 1000.0,
            voltage = voltage / 10.0,
            amperage = amperage,
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
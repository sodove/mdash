package ru.sodovaya.mdash.service

import java.io.Serializable

data class ScooterData(
    val isConnected: String = "Not connected",
    val battery: Int = 0,
    val speed: Double = 0.0,
    val gear: Int = 0,
    val maximumSpeed: Int = 60,
    val voltage: Double = 48.0,
    val amperage: Double = 0.0,
    val temperature: Int = 0,
    val trip: Double = 0.0,
    val totalDist: Double = 0.0,
    val partialDataCache: ByteArray? = null
): Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScooterData

        if (isConnected != other.isConnected) return false
        if (battery != other.battery) return false
        if (speed != other.speed) return false
        if (gear != other.gear) return false
        if (maximumSpeed != other.maximumSpeed) return false
        if (voltage != other.voltage) return false
        if (amperage != other.amperage) return false
        if (temperature != other.temperature) return false
        if (trip != other.trip) return false
        if (totalDist != other.totalDist) return false
        if (partialDataCache != null) {
            if (other.partialDataCache == null) return false
            if (!partialDataCache.contentEquals(other.partialDataCache)) return false
        } else if (other.partialDataCache != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isConnected.hashCode()
        result = 31 * result + battery
        result = 31 * result + speed.hashCode()
        result = 31 * result + gear
        result = 31 * result + maximumSpeed
        result = 31 * result + voltage.hashCode()
        result = 31 * result + amperage.hashCode()
        result = 31 * result + temperature
        result = 31 * result + trip.hashCode()
        result = 31 * result + totalDist.hashCode()
        result = 31 * result + (partialDataCache?.contentHashCode() ?: 0)
        return result
    }
}

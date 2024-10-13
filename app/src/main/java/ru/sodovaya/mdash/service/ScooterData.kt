package ru.sodovaya.mdash.service

import java.io.Serializable

data class ScooterData(
    val isConnected: String = "Not connected",
    val battery: Int = 0,
    val speed: Double = 0.0,
    val gear: Int = 0,
    val maximumSpeed: Int = 0,
    val voltage: Double = 48.0,
    val amperage: Double = 0.0,
    val temperature: Int = 0,
    val trip: Double = 0.0,
    val totalDist: Double = 0.0
): Serializable

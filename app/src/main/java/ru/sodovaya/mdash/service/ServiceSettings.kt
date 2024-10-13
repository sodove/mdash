package ru.sodovaya.mdash.service

data class ServiceSettings(
    val voltageMin: Float = 39f,
    val voltageMax: Float = 55f,
    val amperageMin: Float = -20f,
    val amperageMax: Float = 40f,
    val temperatureMin: Float = -10f,
    val temperatureMax: Float = 90f,
    val powerMin: Float = -500f,
    val powerMax: Float = 2000f,
    val maximumVolumeAt: Float = 30f,
    val volumeServiceEnabled: Boolean = false
)

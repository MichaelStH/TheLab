package com.riders.thelab.core.data.local.model.weather

import com.riders.thelab.core.data.remote.dto.weather.Temperature
import java.io.Serializable

@kotlinx.serialization.Serializable
data class TemperatureModel(
    val temperature: Double = 0.0,
    val realFeels: Double = 0.0,
    val day: Double = 0.0,
    val min: Double = 0.0,
    val max: Double = 0.0,
    val night: Double = 0.0,
    val evening: Double = 0.0,
    val morning: Double = 0.0
) : Serializable

fun Temperature.toModel(): TemperatureModel = TemperatureModel(
    day = this.day,
    min = this.min,
    max = this.max,
    night = this.night,
    evening = this.evening,
    morning = this.morning,
)

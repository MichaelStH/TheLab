package com.riders.thelab.core.data.remote.dto.weather

import com.riders.thelab.core.data.local.model.weather.TemperatureModel
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Temperature(
    @SerialName("day")
    val day: Double = 0.0,

    @SerialName("min")
    val min: Double = 0.0,

    @SerialName("max")
    val max: Double = 0.0,

    @SerialName("night")
    val night: Double = 0.0,

    @SerialName("eve")
    val evening: Double = 0.0,

    @SerialName("morn")
    val morning: Double = 0.0
) : Serializable
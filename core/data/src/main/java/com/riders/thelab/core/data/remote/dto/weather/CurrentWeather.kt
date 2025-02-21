package com.riders.thelab.core.data.remote.dto.weather

import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class CurrentWeather(
    @SerialName("dt")
    val dateTimeUTC: Long = 0L,

    @SerialName("sunrise")
    val sunrise: Long = 0L,

    @SerialName("sunset")
    val sunset: Long = 0L,

    @SerialName("moonrise")
    val moonrise: Long = 0L,

    @SerialName("moonset")
    val moonset: Long = 0L,

    @SerialName("moon_phase")
    val moonPhase: Double = 0.0,

    @SerialName("temp")
    val temperature: Double = 0.0,

    @SerialName("feels_like")
    val feelsLike: Double = 0.0,

    @SerialName("pressure")
    val pressure: Int = 0,

    @SerialName("humidity")
    val humidity: Int = 0,

    @SerialName("dew_point")
    val dewPoint: Double = 0.0,

    // UV index
    @SerialName("uvi")
    val uvIndex: Double = 0.0,

    @SerialName("clouds")
    val clouds: Int = 0,

    // Average visibility, metres
    @SerialName("visibility")
    val visibility: Int = 0,

    @SerialName("wind_speed")
    val windSpeed: Double = 0.0,

    @SerialName("wind_deg")
    val windDegree: Int = 0,

    @SerialName("wind_gust")
    val windGust: Double = 0.0,

    @SerialName("weather")
    val weather: List<Weather>? = null,

    @SerialName("rain")
    val rain: Rain? = null,

    @SerialName("snow")
    val snow: Snow? = null,

    // Probability of precipitation
    @SerialName("pop")
    val pop: Double = 0.0
) : Serializable {
    constructor() : this(
        0,
        0,
        0,
        0,
        0,
        0.0,
        0.0,
        0.0,
        0,
        0,
        0.0,
        0.0,
        0,
        0,
        0.0,
        0,
        0.0,
        null,
        null,
        null,
        0.0
    )
}

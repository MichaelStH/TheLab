package com.riders.thelab.core.data.local.model.weather

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.riders.thelab.core.data.remote.dto.weather.FeelsLike
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import java.io.Serializable

@Stable
@Immutable
@kotlinx.serialization.Serializable
data class WeatherModel(
    val city: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String? = null,
    val mainWeather: String? = null,
    val description: String? = null,
    val weatherIconUrl: String? = null,
    val dateTimeUTC: Long = 0L,

    val sunrise: Long = 0L,
    val sunset: Long = 0L,
    val moonrise: Long = 0L,
    val moonset: Long = 0L,
    val moonPhase: Double = 0.0,

    val temperature: TemperatureModel? = null,
    val feelsLike: FeelsLike? = null,

    val pressure: Int = 0,
    val humidity: Int = 0,

    val dewPoint: Double? = null,
    val uvIndex: Double? = null,

    val clouds: Int = 0,
    // Average visibility, metres
    val visibility: Int = 0,

    val windSpeed: Double? = null,
    val windDegree: Int = 0,
    val windGust: Double? = null,

    val hourlyWeather: List<WeatherModel>? = null,
    val dailyWeather: List<WeatherModel>? = null,

    val rain: Double? = null,
    val snow: Double? = null,
    var probabilityOfPrecipitation: Double? = null
) : Serializable

fun OneCallWeatherResponse.toModel(): WeatherModel {
    val hourlies = this.hourlyWeather?.map { hourlyWeatherItem ->
        WeatherModel(
            mainWeather = hourlyWeatherItem.weather?.get(0)?.main!!,
            description = hourlyWeatherItem.weather?.get(0)?.description!!,
            weatherIconUrl = hourlyWeatherItem.weather?.get(0)?.icon!!
        )
    }

    val dailies = this.dailyWeather?.map { dailyWeatherItem ->
        WeatherModel(
            mainWeather = dailyWeatherItem.weather[0].main,
            description = dailyWeatherItem.weather[0].description,
            weatherIconUrl = dailyWeatherItem.weather[0].icon
        )
    }

    return WeatherModel(
        latitude = this.latitude,
        longitude = this.longitude,
        dateTimeUTC = this.currentWeather?.dateTimeUTC!!,
        temperature = TemperatureModel(temperature = this.currentWeather?.temperature!!),
        hourlyWeather = hourlies,
        dailyWeather = dailies
    )
}

package com.riders.thelab.core.data.local.model.weather

import android.location.Address
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.riders.thelab.core.data.remote.dto.weather.CurrentWeather
import com.riders.thelab.core.data.remote.dto.weather.DailyWeather
import com.riders.thelab.core.data.remote.dto.weather.FeelsLike
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import kotlinx.serialization.Contextual
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
    var weatherIconUrl: String? = null,

    val dateTimeUTC: Long = 0L,
    val timezone: String? = null,
    val timezoneOffset: Int = 0,

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

    val windSpeed: Double = 0.0,
    val windDegree: Int = 0,
    val windGust: Double = 0.0,

    val hourlyWeather: List<WeatherModel>? = null,
    val dailyWeather: List<WeatherModel>? = null,

    // Rain
    val rain: Double? = 0.0,
    val rainLastHour: Double? = null,
    val rainLastThreeHour: Double? = null,

    // Snow
    val snow: Double? = 0.0,
    val snowLastHour: Double? = null,
    val snowLastThreeHour: Double? = null,

    var probabilityOfPrecipitation: Double? = null
) : Serializable {

    var sunriseAsString: String? = null
    var sunsetAsString: String? = null

    @Contextual
    var address: Address? = null
}

fun CurrentWeather.toModel(): WeatherModel = WeatherModel(
    // Current Weather
    dateTimeUTC = this.dateTimeUTC,
    sunrise = this.sunrise,
    sunset = this.sunset,
    moonrise = this.moonrise,
    moonset = this.moonset,
    moonPhase = this.moonPhase,
    temperature = TemperatureModel(temperature = this.temperature),
    feelsLike = FeelsLike(day = this.feelsLike),
    pressure = this.pressure,
    humidity = this.humidity,
    dewPoint = this.dewPoint,
    uvIndex = this.uvIndex,
    clouds = this.clouds,
    visibility = this.visibility,
    windSpeed = this.windSpeed,
    windDegree = this.windDegree,
    windGust = this.windGust,
    mainWeather = this.weather?.get(0)?.main!!,
    description = this.weather[0].description,
    weatherIconUrl = this.weather[0].icon,
    rainLastHour = this.rain?.lastHour,
    rainLastThreeHour = this.rain?.lastThreeHour,
    snowLastHour = this.snow?.lastHour,
    snowLastThreeHour = this.snow?.lastThreeHour,
    probabilityOfPrecipitation = this.pop
)

fun DailyWeather.toModel(): WeatherModel = WeatherModel(
    // Current Weather
    dateTimeUTC = this.dateTimeUTC,
    sunrise = this.sunrise,
    sunset = this.sunset,
    moonrise = this.moonrise,
    moonset = this.moonset,
    moonPhase = this.moonPhase,
    temperature = this.temperature.toModel(),
    feelsLike = this.feelsLike,
    pressure = this.pressure,
    humidity = this.humidity,
    dewPoint = this.dewPoint,
    uvIndex = this.uvIndex,
    clouds = this.clouds,
    visibility = this.visibility,
    windSpeed = this.windSpeed,
    windDegree = this.windDegree,
    windGust = this.windGust,
    mainWeather = this.weather[0].main,
    description = this.weather[0].description,
    weatherIconUrl = this.weather[0].icon,
    rain = this.rain,
    snow = this.snow,
    probabilityOfPrecipitation = this.pop
)

fun OneCallWeatherResponse.toModel(): WeatherModel {
    val hourlies = this.hourlyWeather?.map { hourlyWeatherItem ->
        hourlyWeatherItem.toModel()
    }

    val dailies = this.dailyWeather?.map { dailyWeatherItem ->
        dailyWeatherItem.toModel()
    }

    val currentWeather: WeatherModel = WeatherModel(
        latitude = this.latitude,
        longitude = this.longitude,
        timezone = this.timezone,
        timezoneOffset = this.timezoneOffset,
        // Current Weather
        dateTimeUTC = this.currentWeather?.dateTimeUTC!!,
        sunrise = this.currentWeather.sunrise,
        sunset = this.currentWeather.sunset,
        moonrise = this.currentWeather.moonrise,
        moonset = this.currentWeather.moonset,
        moonPhase = this.currentWeather.moonPhase,
        temperature = TemperatureModel(temperature = this.currentWeather.temperature),
        feelsLike = FeelsLike(day = this.currentWeather.feelsLike),
        pressure = this.currentWeather.pressure,
        humidity = this.currentWeather.humidity,
        dewPoint = this.currentWeather.dewPoint,
        uvIndex = this.currentWeather.uvIndex,
        clouds = this.currentWeather.clouds,
        visibility = this.currentWeather.visibility,
        windSpeed = this.currentWeather.windSpeed,
        windDegree = this.currentWeather.windDegree,
        windGust = this.currentWeather.windGust,
        mainWeather = this.currentWeather.weather?.get(0)?.main!!,
        description = this.currentWeather.weather[0].description,
        weatherIconUrl = this.currentWeather.weather[0].icon,
        hourlyWeather = hourlies,
        dailyWeather = dailies,
        rainLastHour = this.currentWeather.rain?.lastHour,
        rainLastThreeHour = this.currentWeather.rain?.lastThreeHour,
        snowLastHour = this.currentWeather.snow?.lastHour,
        snowLastThreeHour = this.currentWeather.snow?.lastThreeHour,
        probabilityOfPrecipitation = this.currentWeather.pop
    )

    return currentWeather
}

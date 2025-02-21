package com.riders.thelab.core.data.local.model.weather

import com.riders.thelab.core.common.utils.DateTimeUtils
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import java.io.Serializable

@kotlinx.serialization.Serializable
data class WeatherWidgetModel(
    val temperature: TemperatureModel,
    var icon: String,
    val description: String,
    val forecast: List<ForecastWeatherWidgetModel>
) : Serializable {

    override fun toString(): String {
        return "temperature: $temperature, icon: $icon, description: $description, forecast: size:${forecast.size}, content: $forecast"
    }
}

fun OneCallWeatherResponse.toWidgetModel(): WeatherWidgetModel? = this.currentWeather?.run {
    var minStoredTemperature: Double? = null
    var maxStoredTemperature: Double? = null

    this@toWidgetModel.hourlyWeather?.let {

        minStoredTemperature = it[0].temperature
        maxStoredTemperature = it[0].temperature

        it.forEach { temp ->
            if (minStoredTemperature!! >= temp.temperature) {
                minStoredTemperature = temp.temperature
            }
        }

        it.forEach { temp ->
            if (temp.temperature >= maxStoredTemperature!!) {
                maxStoredTemperature = temp.temperature
            }
        }
    }

    val temperature =
        TemperatureModel(
            temperature = this.temperature,
            realFeels = this.feelsLike,
            min = minStoredTemperature ?: 0.0,
            max = maxStoredTemperature ?: 0.0
        )

    val description: String? = this.weather?.get(0)?.description
    val icon: String? = this.weather?.get(0)?.icon

    val dailyWeather: List<ForecastWeatherWidgetModel>? = this@toWidgetModel.dailyWeather
        ?.run {
            this.map {
                ForecastWeatherWidgetModel(
                    day = DateTimeUtils.getDayFromTime(it.dateTimeUTC),
                    temperature = it.temperature.toModel(),
                    icon = it.weather[0].icon
                )
            }.toList()
        }

    if (null == description || null == icon || null == dailyWeather) {
        return null
    } else {
        return WeatherWidgetModel(temperature, icon, description, dailyWeather)
    }
}


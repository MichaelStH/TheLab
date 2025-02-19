package com.riders.thelab.feature.weather.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.riders.thelab.core.data.local.model.compose.weather.WeatherCityUIState
import com.riders.thelab.core.data.local.model.compose.weather.WeatherUIState
import com.riders.thelab.core.data.local.model.weather.TemperatureModel
import com.riders.thelab.core.data.local.model.weather.WeatherModel

private val mockWeather: WeatherModel = WeatherModel(
    dateTimeUTC = 1684160601L,
    latitude = 48.8534,
    longitude = 2.3486,
    sunrise = 1684123772L,
    sunset = 1684178672,
    temperature = TemperatureModel(temperature = 15.75, realFeels = 15.39),
    mainWeather = "Rain",
    description = "light rain",
    weatherIconUrl = "10d",
    hourlyWeather = listOf(
        WeatherModel(
            mainWeather = "Rain",
            description = "light rain",
            weatherIconUrl = "10d",
            temperature = TemperatureModel(temperature = 9.75, realFeels = 15.39),
        ),
        WeatherModel(
            mainWeather = "Rain",
            description = "light rain",
            weatherIconUrl = "10d",
            temperature = TemperatureModel(temperature = 5.75, realFeels = 15.39),
        ),
        WeatherModel(
            mainWeather = "Rain",
            description = "light rain",
            weatherIconUrl = "10d",
            temperature = TemperatureModel(temperature = 15.75, realFeels = 15.39),
        ),
    ),
    dailyWeather = listOf(
        WeatherModel(
            mainWeather = "Rain",
            description = "light rain",
            weatherIconUrl = "10d",
            temperature = TemperatureModel(temperature = 9.75, realFeels = 15.39),
        ),
        WeatherModel(
            mainWeather = "Rain",
            description = "light rain",
            weatherIconUrl = "10d",
            temperature = TemperatureModel(temperature = 5.75, realFeels = 15.39),
        ),
        WeatherModel(
            mainWeather = "Rain",
            description = "light rain",
            weatherIconUrl = "10d",
            temperature = TemperatureModel(temperature = 15.75, realFeels = 15.39),
        ),
    ),
)


class PreviewProviderWeather : PreviewParameterProvider<WeatherUIState> {
    override val values: Sequence<WeatherUIState>
        get() = sequenceOf(
            WeatherUIState.Loading,
            WeatherUIState.Error(),
            WeatherUIState.Success(mockWeather),
            WeatherUIState.SuccessWeatherData(true),
            WeatherUIState.SuccessWeatherData(false),
        )
}

class PreviewProviderWeatherCity : PreviewParameterProvider<WeatherCityUIState> {
    override val values: Sequence<WeatherCityUIState>
        get() = sequenceOf(
            WeatherCityUIState.None,
            WeatherCityUIState.Success(mockWeather),
            WeatherCityUIState.Error(),
        )
}

/*
class PreviewProviderDailyWeather : PreviewParameterProvider<DailyWeather> {
    override val values: Sequence<DailyWeather>
        get() = sequenceOf(
            DailyWeather()
        )
}*/

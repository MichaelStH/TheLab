package com.riders.thelab.feature.weather.core.worker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.riders.thelab.core.common.utils.DateTimeUtils
import com.riders.thelab.core.common.utils.LabAddressesUtils
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.common.utils.LabLocationManager
import com.riders.thelab.core.common.utils.toLocation
import com.riders.thelab.core.data.IRepository
import com.riders.thelab.core.data.local.model.weather.ForecastWeatherWidgetModel
import com.riders.thelab.core.data.local.model.weather.TemperatureModel
import com.riders.thelab.core.data.local.model.weather.WeatherWidgetModel
import com.riders.thelab.core.data.local.model.weather.toModel
import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import com.riders.thelab.core.ui.R
import com.riders.thelab.feature.weather.utils.WeatherUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.Locale
import kotlin.math.roundToInt

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val mRepository: IRepository
) : CoroutineWorker(context, workerParams) {

    private var outputData: Data? = null

    @SuppressLint("NewApi")
    override suspend fun doWork(): Result {
        Timber.d("doWork()")

        val labLocationManager = LabLocationManager(context)
        val location: Location? = labLocationManager.getCurrentLocation()

        if (null == location) {
            Timber.e("Location object is null. Unable to get user's location")
            // Unable to fetch user location
            outputData = createOutputData(WORK_LOCATION_FAILED)
            return Result.failure(outputData!!)
        }

        val geocoder: Geocoder = if (LabCompatibilityManager.isTiramisu()) Geocoder(
            context,
            Locale.getDefault()
        ) else Geocoder(context)

        return runCatching {
            suspendCancellableCoroutine<Result> {
                val oneCallWeatherResponse: OneCallWeatherResponse? =
                    runBlocking { mRepository.getWeatherOneCallAPI(location) }

                // Check if response is null
                if (null == oneCallWeatherResponse) {
                    Timber.e("null == oneCallWeatherResponse | weather call failed, response value is null")

                    outputData = createOutputData(WORK_WEATHER_CALL_FAILED)
                    Result.failure(outputData!!)
                } else {
                    Timber.d("observer.onSuccess(responseFile)")

                    val weatherLocation =
                        (oneCallWeatherResponse.latitude to oneCallWeatherResponse.longitude).toLocation()

                    if (!LabCompatibilityManager.isTiramisu()) {
                        val address =
                            LabAddressesUtils.getDeviceAddressLegacy(geocoder, weatherLocation)

                        // Load city name
                        val cityName = address?.locality
                        val country = address?.countryName

                        // val weatherBundle = buildWeatherBundle(oneCallWeatherResponse, cityName!!, country!!)
                        // updateWidgetViaBroadcast(weatherWidgetBundle)

                        val weatherWidgetBundle =
                            runBlocking {
                                buildWeatherWidget(oneCallWeatherResponse)
                            }

                        if (null == weatherWidgetBundle) {
                            Timber.e("Failed to build weather widget object because fields may be null")
                        } else {
                            // Create and send outputData
                            outputData = createOutputData(WORK_SUCCESS)
                            Result.success(outputData!!)
                        }

                    } else {
                        LabAddressesUtils.getDeviceAddressAndroid13(geocoder, weatherLocation) {
                            it?.let {
                                // Load city name
                                val cityName = it.locality
                                val country = it.countryName

                                /*val weatherBundle = buildWeatherBundle(
                                    oneCallWeatherResponse,
                                    cityName!!,
                                    country!!
                                )*/

                                // updateWidgetViaBroadcast(weatherBundle)

                                val weatherWidgetBundle =
                                    runBlocking { buildWeatherWidget(oneCallWeatherResponse) }
                                weatherWidgetBundle?.let {
                                    // updateWidgetViaBroadcast(weatherWidgetBundle)
                                    // Create and send outputData
                                    outputData = createOutputData(WORK_SUCCESS)
                                    Result.success(outputData!!)
                                }
                                    ?: run { Timber.e("Failed to build weather widget object because fields may be null") }
                            }
                        }
                    }

                    Result.success()
                }
            }
        }
            .onFailure {
                it.printStackTrace()
                Timber.e("runCatching | onFailure | error caught with message: ${it.message}")
            }
            .getOrElse {
                Timber.e("runCatching | getOrElse | error caught with message: ${it.message}")
                outputData = createOutputData(WORK_ERROR_FAILED)
                Result.failure(outputData!!)
            }
    }

    /**
     * Creates ouput data to send back to the activity / presenter which is listening to it
     *
     * @param message
     * @return
     */

    @SuppressLint("RestrictedApi")
    private fun createOutputData(message: String): Data {
        Timber.d("createOutputData() | message: $message")
        return Data.Builder()
            .put("work_result", message)
            .build()
    }


    /**
     * Build bundle to send to widget provider
     *
     */
    private fun buildWeatherBundle(
        response: OneCallWeatherResponse,
        city: String,
        country: String
    ): Bundle {
        Timber.d("buildWeatherBundle()")
        val description = response.currentWeather?.weather?.get(0)?.description
        val temperature =
            "${response.currentWeather?.temperature?.roundToInt()} ${context.getString(R.string.degree_placeholder)}"
        val realFeels =
            "${response.currentWeather?.feelsLike?.roundToInt()} ${
                context.getString(
                    R.string.degree_placeholder
                )
            }"
        val icon =
            response.currentWeather?.weather?.get(0)?.let {
                WeatherUtils.getWeatherIconFromApi(
                    it.icon
                )
            }

        return Bundle().apply {
            putString(EXTRA_WEATHER_CITY, city)
            putString(EXTRA_WEATHER_COUNTRY, country)
            putString(EXTRA_WEATHER_DESCRIPTION, description)
            putString(EXTRA_WEATHER_TEMPERATURE, temperature)
            putString(EXTRA_WEATHER_REAL_FEELS, realFeels)
            putString(EXTRA_WEATHER_ICON, icon)
        }
    }

    /**
     * Build bundle to send to widget provider
     *
     */
    private fun buildWeatherWidget(response: OneCallWeatherResponse): WeatherWidgetModel? {
        Timber.d("buildWeatherWidget()")

        return response.currentWeather?.run {
            val temperature =
                TemperatureModel(temperature = this.temperature, realFeels = this.feelsLike)

            val description: String? = this.weather?.get(0)?.description
            val icon: String? =
                this.weather?.get(0)?.let {
                    WeatherUtils.getWeatherIconFromApi(
                        it.icon
                    )
                }


            val dailyWeather: List<ForecastWeatherWidgetModel>? = response.dailyWeather?.run {
                this.map {
                    ForecastWeatherWidgetModel(
                        day = DateTimeUtils.getDayFromTime(it.dateTimeUTC),
                        temperature = it.temperature.toModel(),
                        icon = it.weather[0].icon
                    )
                }.toList()
            }

            /*val currentTemperature = "${this.temperature.roundToInt()} ${context.getString(R.string.degree_placeholder)}"
            val realFeels = "${this.feelsLike.roundToInt()} ${context.getString(R.string.degree_placeholder)}"*/

            temperature.let { temp ->
                icon?.let { ic ->
                    description?.let { desc ->
                        dailyWeather?.let { daily ->
                            return WeatherWidgetModel(temp, ic, desc, daily)
                        }
                    }
                }
            }
        } ?: run {
            Timber.e("current weather object is null")
            return null
        }
    }


    companion object {
        const val WORK_SUCCESS = "Loading finished"
        const val WORK_ERROR_FAILED: String =
            "Some errors occurred while processing data check log to see more details"
        const val WORK_WEATHER_CALL_FAILED: String = "Weather call failed, response value is null"
        const val WORK_LOCATION_FAILED: String = "Unable to fetch user's location"

        const val EXTRA_WEATHER_WIDGET = "EXTRA_WEATHER_WIDGET"
        const val EXTRA_WEATHER_CITY = "EXTRA_WEATHER_CITY"
        const val EXTRA_WEATHER_COUNTRY = "EXTRA_WEATHER_COUNTRY"
        const val EXTRA_WEATHER_DESCRIPTION = "EXTRA_WEATHER_DESCRIPTION"
        const val EXTRA_WEATHER_TEMPERATURE = "EXTRA_WEATHER_TEMPERATURE"
        const val EXTRA_WEATHER_REAL_FEELS = "EXTRA_WEATHER_REAL_FEELS"
        const val EXTRA_WEATHER_ICON = "EXTRA_WEATHER_ICON"
    }
}
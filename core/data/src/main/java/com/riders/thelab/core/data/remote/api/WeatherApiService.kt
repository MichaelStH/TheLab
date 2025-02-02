package com.riders.thelab.core.data.remote.api

import com.riders.thelab.core.data.remote.dto.weather.OneCallWeatherResponse
import com.riders.thelab.core.data.remote.dto.weather.Weather
import com.riders.thelab.core.data.remote.dto.weather.WeatherResponse

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    /**
     * Description: You can call by city name or city name, state and country code.
     * API responds with a list of results that match a searching word.
     *
     *
     * API call:
     * api.openweathermap.org/data/3.0/weather?q={city name}&appid={your api key}
     * api.openweathermap.org/data/3.0/weather?q={city name},{state}&appid={your api key}
     * api.openweathermap.org/data/3.0/weather?q={city name},{state},{country code}&appid={your api key}
     *
     *
     * Parameters: q city name, state and country code divided by comma, use ISO 3166 country codes.
     * You can specify the parameter not only in English. In this case,
     * the API response should be returned in the same language as the language of requested
     * location name if the location is in our predefined list of more than 200,000 locations.
     *
     *
     * Examples of API calls:
     * api.openweathermap.org/data/3.0/weather?q=London
     * api.openweathermap.org/data/3.0/weather?q=London,uk
     *
     * @param cityName
     * @return
     */
    /*@GET("/data/3.0/weather?")
    Call<WeatherResponse> getCurrentWeatherByCityName(@Query("q") String cityName);*/
    @GET("/data/3.0/weather?")
    suspend fun getCurrentWeatherByCityName(@Query("q") cityName: String): WeatherResponse

    /**
     * We recommend to call API by city ID to get unambiguous result for your city.
     *
     *
     * List of city ID city.list.json.gz can be downloaded here http://bulk.openweathermap.org/sample/
     *
     *
     * API call:
     * api.openweathermap.org/data/3.0/weather?id={city id}&appid={your api key}
     *
     *
     * Parameters: id City ID
     * Examples of API calls: api.openweathermap.org/data/3.0/weather?id=2172797
     *
     * @param cityID
     * @return
     */
    @GET("/data/3.0/weather?")
    fun getCurrentWeatherByCityID(@Query("q") cityID: String): Call<Weather>

    /**
     * API call: api.openweathermap.org/data/3.0/weather?lat={lat}&lon={lon}&appid={your api key}
     * Parameters: lat, lon coordinates of the location of your interest
     * Examples of API calls: api.openweathermap.org/data/3.0/weather?lat=35&lon=139
     *
     * @param lat
     * @param lon
     * @return
     */
    /*@GET("/data/3.0/weather?")
    Call<Weather> getCurrentWeatherByGeographicCoordinates(
            @Query("lat") int lat,
            @Query("lon") int lon);*/
    @GET("/data/3.0/weather?")
    suspend fun getCurrentWeatherByGeographicCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): WeatherResponse


    /**
     * Description: Please note if country is not specified then the search works for USA as a default.
     * API call:
     * api.openweathermap.org/data/3.0/weather?zip={zip code},{country code}&appid={your api key}
     * Examples of API calls: api.openweathermap.org/data/3.0/weather?zip=94040,us
     *
     * @param zipCodeAndCountryCode
     * @return
     */
    @GET("/data/3.0/weather?")
    fun getCurrentWeatherByZipCode(@Query("zip") zipCodeAndCountryCode: String): Call<Weather>

    /**
     * API call: api.openweathermap.org/data/3.0/onecall?lat={lat}&lon={lon}&appid={your api key}
     * Parameters: lat, lon coordinates of the location of your interest
     * Examples of API calls: api.openweathermap.org/data/3.0/onecall?lat=35&lon=139
     *
     * @param lat
     * @param lon
     * @return
     */
    @GET("/data/3.0/onecall?")
    suspend fun getCurrentWeatherWithNewOneCallAPI(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): OneCallWeatherResponse
}
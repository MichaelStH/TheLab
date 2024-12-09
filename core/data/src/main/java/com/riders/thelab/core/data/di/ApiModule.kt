package com.riders.thelab.core.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.riders.thelab.core.common.utils.decrypt
import com.riders.thelab.core.data.BuildConfig
import com.riders.thelab.core.data.local.bean.TimeOut
import com.riders.thelab.core.data.remote.api.ArtistsAPIService
import com.riders.thelab.core.data.remote.api.FlightApiService
import com.riders.thelab.core.data.remote.api.GoogleAPIService
import com.riders.thelab.core.data.remote.api.SpotifyAPIService
import com.riders.thelab.core.data.remote.api.SpotifyAccountAPIService
import com.riders.thelab.core.data.remote.api.TMDBApiService
import com.riders.thelab.core.data.remote.api.TheLabBackApiService
import com.riders.thelab.core.data.remote.api.WeatherApiService
import com.riders.thelab.core.data.remote.api.WeatherBulkApiService
import com.riders.thelab.core.data.remote.api.WikimediaApiService
import com.riders.thelab.core.data.remote.api.YoutubeApiService
import com.riders.thelab.core.data.utils.Constants
import com.riders.thelab.core.data.utils.Headers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber
import java.util.Objects
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {
    @Provides
    fun provideOkHttpLogger(isBulkDownload: Boolean = false): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message: String -> Timber.tag("OkHttp").d(message) }
            .setLevel(if (isBulkDownload) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.BODY)

    /* WEATHER */
    /* Provide OkHttp for the app */
    @Provides
    @Singleton
    fun provideWeatherOkHttp(isBulkDownload: Boolean = false): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(TimeOut.TIME_OUT_READ.value.toLong(), TimeUnit.SECONDS)
        .connectTimeout(TimeOut.TIME_OUT_CONNECTION.value.toLong(), TimeUnit.SECONDS)
        .addInterceptor(
            Interceptor { chain: Interceptor.Chain ->

                val original = chain.request()
                val originalHttpUrl = original.url
                var url: HttpUrl? = null

                // Request customization: add request headers
                var requestBuilder: Request.Builder? = null

                // Avoid key and metrics when requesting for bulk download
                if (!originalHttpUrl.toString().contains("sample")) {
                    try {
                        url = originalHttpUrl.newBuilder()
                            .addQueryParameter(
                                "appid",
                                BuildConfig.SERVER_API_KEY_OPEN_WEATHER.decrypt()
                            )
                            .addQueryParameter("units", "metric")
                            .build()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Timber.e(Objects.requireNonNull(e.message))
                    }

                    assert(url != null)

                    // Request customization: add request headers
                    url?.let {
                        requestBuilder = original.newBuilder()
                            .url(it)
                            .header(
                                Headers.CONTENT_TYPE.value,
                                "application/json; charset=utf-8"
                            )
                            .header(Headers.CONNECTION.value, "close")
                            .header(Headers.ACCEPT_ENCODING.value, "Identity")

                    }
                } else {
                    url = originalHttpUrl.newBuilder().build()

                    // Request customization: add request headers
                    requestBuilder = original.newBuilder()
                        .url(url)
                        .header(Headers.CONTENT_TYPE.value, "text/plain")
                        .header(Headers.CONNECTION.value, "close")
                        .header(Headers.CACHE_CONTROL.value, "max-age=60")
                        .header(Headers.ACCEPT_RANGES.value, "bytes")
                        .header(Headers.ACCEPT_ENCODING.value, "Identity")
                }

                val request = requestBuilder?.build()
                chain.proceed(request!!)
            })
        .addInterceptor(provideOkHttpLogger(isBulkDownload))
        .build()


    @Provides
    @Singleton
    fun provideTMDBOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(TimeOut.TIME_OUT_READ.value.toLong(), TimeUnit.SECONDS)
        .connectTimeout(TimeOut.TIME_OUT_CONNECTION.value.toLong(), TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain: Interceptor.Chain ->

            val original = chain.request()
            val originalHttpUrl = original.url
            // Request customization: add request headers
            var requestBuilder: Request.Builder? = null
            val url: HttpUrl? = try {
                originalHttpUrl
                    .newBuilder()
                    .addQueryParameter(
                        "api_key",
                        BuildConfig.SERVER_API_KEY_TMDB.decrypt()
                    )
                    .build()
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e(Objects.requireNonNull(e.message))
                null
            }

            assert(url != null)

            // Request customization: add request headers
            url?.let {
                requestBuilder = original.newBuilder()
                    .url(it)
                    .header(Headers.CONTENT_TYPE.value, "application/json; charset=utf-8")
                    .header(Headers.CONNECTION.value, "close")
                    .header(Headers.ACCEPT_ENCODING.value, "Identity")
            }

            val request = requestBuilder?.build()
            chain.proceed(request!!)
        })
        .addInterceptor(provideOkHttpLogger())
        .build()

    @Provides
    @Singleton
    fun provideFlightOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(TimeOut.TIME_OUT_READ.value.toLong(), TimeUnit.SECONDS)
        .connectTimeout(TimeOut.TIME_OUT_CONNECTION.value.toLong(), TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val originalHttpUrl = original.url

            // Request customization: add request headers
            val requestBuilder = original.newBuilder()
                .url(originalHttpUrl)
                .header(Headers.CONTENT_TYPE.value, "application/json; charset=utf-8")
                .header("x-apikey", BuildConfig.SERVER_API_KEY_FLIGHT_AWARE_AERO.decrypt())

            val request = requestBuilder.build()
            chain.proceed(request)
        })
        .addInterceptor(provideOkHttpLogger())
        .build()


    @Provides
    @Singleton
    fun provideOkHttpArtists(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(TimeOut.TIME_OUT_READ.value.toLong(), TimeUnit.SECONDS)
            .connectTimeout(TimeOut.TIME_OUT_CONNECTION.value.toLong(), TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                // Customize the request
                val request: Request = original.newBuilder()
                    .build()
                val response: Response = chain.proceed(request)
                response.cacheResponse
                response
            })
            .addInterceptor(provideOkHttpLogger())
            .build()
    }


    /* Provide OkHttp for the app */
    @Provides
    fun provideOkHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(TimeOut.TIME_OUT_READ.value.toLong(), TimeUnit.SECONDS)
            .connectTimeout(TimeOut.TIME_OUT_CONNECTION.value.toLong(), TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original = chain.request()
                // Customize the request
                val request = original.newBuilder()
                    .header(Headers.CONTENT_TYPE.value, "application/json; charset=utf-8")
                    .header(Headers.CONNECTION.value, "close")
                    .header(Headers.ACCEPT_ENCODING.value, "Identity")
                    .build()
                val response = chain.proceed(request)
                response.cacheResponse
                response
            })
            .addInterceptor(provideOkHttpLogger())
            .connectionSpecs(
                listOf(
                    ConnectionSpec.CLEARTEXT,
                    ConnectionSpec.COMPATIBLE_TLS,
                    ConnectionSpec.MODERN_TLS,
                )
            )
            .build()
    }


    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        explicitNulls = false
    }

    private const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"

    /* Provide Retrofit for the app */
    @Provides
    fun provideRetrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(provideOkHttp())
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE_JSON.toMediaType()))
        .build()


    @Provides
    @Singleton
    fun provideRetrofitArtists(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(provideOkHttpArtists())
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE_JSON.toMediaType()))
        .build()


    @Provides
    @Singleton
    fun provideWeatherRetrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(provideWeatherOkHttp())
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE_JSON.toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideWeatherBulkRetrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(provideWeatherOkHttp(true))
        .build()

    @Provides
    @Singleton
    fun provideTMDBRetrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(provideTMDBOkHttp())
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE_JSON.toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideFlightRetrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(provideFlightOkHttp())
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE_JSON.toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideWikimediaRetrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(provideOkHttp())
        .addConverterFactory(json.asConverterFactory(CONTENT_TYPE_JSON.toMediaType()))
        .build()


    @Provides
    @Singleton
    fun provideArtistsAPIService(): ArtistsAPIService {
        return provideRetrofitArtists(Constants.BASE_ENDPOINT_GOOGLE_FIREBASE_API)
            .create(ArtistsAPIService::class.java)
    }


    @Provides
    @Singleton
    fun provideGoogleAPIService(): GoogleAPIService {
        return provideRetrofit(Constants.BASE_ENDPOINT_GOOGLE_MAPS_API).create(
            GoogleAPIService::class.java
        )
    }


    @Provides
    @Singleton
    fun provideYoutubeApiService(): YoutubeApiService {
        return provideRetrofit(Constants.BASE_ENDPOINT_YOUTUBE)
            .create(YoutubeApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        return provideWeatherRetrofit(Constants.BASE_ENDPOINT_WEATHER)
            .create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun proWeatherBulkApiService(): WeatherBulkApiService {
        return provideWeatherBulkRetrofit(Constants.BASE_ENDPOINT_WEATHER_BULK_DOWNLOAD)
            .create(WeatherBulkApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserAPIService(): TheLabBackApiService {
        return provideRetrofit(Constants.BASE_ENDPOINT_THE_LAB_URL)
            .create(TheLabBackApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyAccountAPIService(): SpotifyAccountAPIService {
        return provideRetrofit(Constants.BASE_ENDPOINT_SPOTIFY_ACCOUNT)
            .create(SpotifyAccountAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyAPIService(): SpotifyAPIService {
        return provideRetrofit(Constants.BASE_ENDPOINT_SPOTIFY_API)
            .create(SpotifyAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideTMDBAPIService(): TMDBApiService =
        provideTMDBRetrofit(Constants.BASE_ENDPOINT_TMDB_API).create(TMDBApiService::class.java)

    @Provides
    @Singleton
    fun provideFlightAPIService(): FlightApiService =
        provideFlightRetrofit(Constants.BASE_ENDPOINT_FLIGHT_AWARE_API).create(FlightApiService::class.java)

    @Provides
    @Singleton
    fun provideWikimediaAPIService(): WikimediaApiService =
        provideWikimediaRetrofit(Constants.BASE_ENDPOINT_WIKIMEDIA_API).create(WikimediaApiService::class.java)
}
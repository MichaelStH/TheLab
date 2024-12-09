package com.riders.thelab.core.parser

/*import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types*/
import android.content.Context
import com.riders.thelab.core.data.local.model.app.App
import com.riders.thelab.core.data.local.model.app.LocalApp
import com.riders.thelab.core.data.remote.dto.weather.City
import kotlinx.serialization.json.Json
import timber.log.Timber

object LabParser {

    fun parseJsonFile(context: Context, filename: String): List<App> = runCatching {
        Timber.d("parseJsonFile() | filename: $filename")

        val json = context.resources.assets.open(filename).bufferedReader().use { it.readText() }
        val mLocalAppList: List<LocalApp>? = Json.decodeFromString(json)

        return mLocalAppList?.let {
            it.map { localApp ->
                LocalApp(
                    localApp.id,
                    localApp.title!!,
                    localApp.description!!,
                    LocalApp.getDrawableByName(context, localApp.icon!!),
                    localApp.activity,
                    localApp.date!!
                )
            }
        } ?: run {
            Timber.e("List is null. Return emptyList")
            return arrayListOf()
        }
    }
        .onFailure {
            Timber.e("runCatching - onFailure() | Error caught: ${it.message}")
        }
        .onSuccess {
            Timber.d("runCatching - onSuccess() | app list fetched successfully")
        }
        .getOrThrow<List<App>>()

    inline fun <reified T> parseJsonFile(
        context: Context,
        filename: String
    ): T? = runCatching {
        Timber.d("parseJsonFile() | filename: $filename")

        val json = context.assets.open(filename).bufferedReader().use { it.readText() }
        val mRObject: T = Json.decodeFromString(json)

        if (null == mRObject) {
            Timber.e("List is null. Return emptyList")
            return null
        }

        return mRObject
    }
        .onFailure {
            Timber.e("runCatching - onFailure() | Error caught: ${it.message}")
        }
        .onSuccess {
            Timber.d("runCatching - onSuccess() | app list fetched successfully")
        }
        .getOrThrow<T>()

    inline fun <reified T> parseJsonFile(jsonContent: String): T? = runCatching {
        Timber.d("parseJsonFile() | content length: ${jsonContent.length}")

        val mRObject: T = Json.decodeFromString(jsonContent)

        if (null == mRObject) {
            Timber.e("List is null. Return emptyList")
            return null
        }

        return mRObject
    }
        .onFailure {
            Timber.e("runCatching - onFailure() | Error caught: ${it.message}")
        }
        .onSuccess {
            Timber.d("runCatching - onSuccess() | app list fetched successfully")
        }
        .getOrThrow<T>()

    fun parseJsonFileListWithKotlinSerialization(jsonToParse: String): List<City> {
        Timber.d("parseJsonFileListWithKotlinSerialization() | json length: ${jsonToParse.length}")
        return Json.decodeFromString(jsonToParse)
    }


    /*fun parseJsonFileListWithMoshi(jsonToParse: String): List<City>? {
        return try {
            Timber.d("Build Moshi adapter and build object...")
            // Step 2 convert to class object
            val moshi = Moshi.Builder()
                .add(CitiesEventJsonAdapter())
                .build()
            val type: Type = Types.newParameterizedType(MutableList::class.java, City::class.java)
            val jsonAdapter: JsonAdapter<List<City>> = moshi.adapter(type)
            jsonAdapter.fromJson(jsonToParse)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/
}
package com.riders.thelab.core.data.local

import android.database.Cursor
import com.riders.thelab.core.data.local.model.Contact
import com.riders.thelab.core.data.local.model.User
import com.riders.thelab.core.data.local.model.weather.CityModel
import com.riders.thelab.core.data.local.model.weather.WeatherData
import com.riders.thelab.core.data.remote.dto.weather.City
import kotlinx.coroutines.flow.Flow


interface IDb {
    /////////////////////////////////////
    //
    // USERS
    //
    /////////////////////////////////////
    fun insertUser(user: User): Long
    fun insertAllUsers(users: List<User>)
    fun getUsers(): Flow<List<User>>
    fun getUsersSync(): List<User>
    fun getUserByID(userId: Int): User
    fun getUserByName(lastname: String): User
    fun getUserByUsername(username: String): User
    fun getUserByEmail(email: String): User
    fun getUserWithGoogle(email: String): User?
    fun setUserLogged(userId: Int)
    fun logUser(usernameOrMail: String, encodedPassword: String): User?
    fun logoutUser(userId: Int)
    fun deleteUser(userId: Int)


    /////////////////////////////////////
    //
    // CONTACTS
    //
    /////////////////////////////////////
    fun insertContact(contact: Contact)
    suspend fun insertContactRX(contact: Contact): Long
    fun insertAllContacts(contactDetails: List<Contact>)
    fun getContacts(): List<Contact>
    suspend fun getAllContacts(): List<Contact>
    fun clearData()

    /////////////////////////////////////
    //
    // WEATHER
    //
    /////////////////////////////////////
    suspend fun insertWeatherData(isWeatherData: WeatherData): Long
    suspend fun saveCity(city: CityModel): Long
    suspend fun saveCities(dtoCities: List<City>): List<Long>
    suspend fun searchCity(cityQuery:String): List<CityModel>
    suspend fun getWeatherData(): WeatherData?
    suspend fun getCities(): List<CityModel>
    fun getCitiesCursor(query: String): Cursor
    fun deleteAll()
}
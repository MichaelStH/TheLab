package com.riders.thelab.core.data.local.model

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.riders.thelab.core.common.utils.encodeToSha256
import java.io.Serializable

@Stable
@kotlinx.serialization.Serializable
@Entity(tableName = "user")
data class User(
    @ColumnInfo("first_name")
    val firstname: String,
    @ColumnInfo("last_name")
    val lastname: String,
    @ColumnInfo("username")
    val username: String,
    @ColumnInfo("email")
    val email: String,
    @ColumnInfo("password")
    var password: String,
    @ColumnInfo("last_time_registered")
    val lastTimeRegistered: Long
) : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var _id: Long = 0L

    @ColumnInfo("isAdmin")
    var isAdmin: Boolean = false

    @ColumnInfo("logged")
    var logged: Boolean = false

    @ColumnInfo("isGoogleAuth")
    var isGoogleAuth: Boolean = false

    @ColumnInfo("profilePictureUri")
    var profilePictureUri: String? = null

    constructor() : this("", "", "", "", "", 0L)

    /*init {
        Timber.d("User | init method")
        this.password = password.encodeToSha256()
    }*/

    companion object {
        val mockUserForTests: List<User> = listOf(
            User(
                "Jane",
                "Doe",
                "JaneDoe345",
                "jane.doe@test.com",
                "test1234".encodeToSha256(),
                1702222514L
            ),
            User(
                "John",
                "Smith",
                "JohnSmith27",
                "john.smith@test.com",
                "test1234".encodeToSha256(),
                1702222522L
            ),
            User(
                "Mike",
                "Law",
                "Mike1552",
                "mike@test.fr",
                "test1234".encodeToSha256(),
                1702222536L
            )
        )
    }
}

fun GoogleSignInAccount.toModel(): User = User(
    firstname = this.givenName ?: "",
    lastname = this.familyName ?: "",
    username = this.displayName ?: "",
    email = this.email ?: "",
    password = this.id ?: "",
    lastTimeRegistered = System.nanoTime()
).apply {
    this.profilePictureUri = photoUrl.toString()
    this.isGoogleAuth = true
    this.logged = true
}

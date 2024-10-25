package com.riders.thelab.feature.settings.profile

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.riders.thelab.core.data.IRepository
import com.riders.thelab.core.data.local.model.User
import com.riders.thelab.core.ui.compose.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@Suppress("EmptyMethod")
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: IRepository
) : BaseViewModel() {

    //////////////////////////////////////////
    // Compose states
    //////////////////////////////////////////
    var user: User? by mutableStateOf(null)
        private set
    var firstname: String by mutableStateOf("")
        private set
    var lastname: String by mutableStateOf("")
        private set
    var username: String by mutableStateOf("")
        private set
    var email: String by mutableStateOf("")
        private set
    var password: String by mutableStateOf("")
        private set
    var passwordConfirmation: String by mutableStateOf("")
        private set
    var photoUrl: String by mutableStateOf("")
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val emailHasError: StateFlow<Boolean> =
        snapshotFlow { email }
            .mapLatest {
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val passwordsHasError: StateFlow<Boolean> =
        snapshotFlow { password to passwordConfirmation }
            .mapLatest {
                it.first != it.second
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    private fun updateUser(user: User) {
        this.user = user
    }

    fun updateFirstname(firstname: String) {
        this.firstname = firstname
    }

    fun updateLastname(lastname: String) {
        this.lastname = lastname
    }

    private fun updateEmail(email: String) {
        this.email = email
    }

    private fun updateUsername(username: String) {
        this.username = username
    }

    fun updatePassword(password: String) {
        this.password = password
    }

    fun updatePasswordConfirmation(passwordConfirmation: String) {
        this.passwordConfirmation = passwordConfirmation
    }

    private fun updatePhotoUrl(photoUrl: String) {
        this.photoUrl = photoUrl
    }


    //////////////////////////////////////////
    // Coroutines
    //////////////////////////////////////////
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            Timber.e("coroutineExceptionHandler | ${throwable.message}")
        }

    init {
        viewModelScope.launch {
            repository.isNightMode().collect {
                updateDarkMode(it)
            }

            repository.isVibration().collect {
                updateVibration(it)
            }
        }

        getLoggedUser()
    }

    //////////////////////////////////////////
    //
    // CLASS METHODS
    //
    //////////////////////////////////////////
    private fun getLoggedUser() {
        Timber.d("getLoggedUser()")

        val user: User? =
            runBlocking(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
                repository.getUsersSync().firstOrNull { it.logged }
            }

        user?.let {
            updateUser(it)
            updateFirstname(it.firstname)
            updateLastname(it.lastname)
            updateUsername(it.username)
            updateEmail(it.email)
            it.profilePictureUri?.let { url -> updatePhotoUrl(url.toString()) }
        }
    }

    fun updateUser() {
        Timber.d("updateUser()")
    }
}
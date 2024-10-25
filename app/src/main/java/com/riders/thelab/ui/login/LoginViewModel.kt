package com.riders.thelab.ui.login

import android.util.Patterns
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.riders.thelab.BuildConfig
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.common.network.NetworkState
import com.riders.thelab.core.common.utils.encodeToSha256
import com.riders.thelab.core.data.IRepository
import com.riders.thelab.core.data.local.model.User
import com.riders.thelab.core.data.local.model.compose.LoginFieldsUIState
import com.riders.thelab.core.data.local.model.compose.LoginUiState
import com.riders.thelab.core.data.local.model.toModel
import com.riders.thelab.core.data.remote.dto.ApiResponse
import com.riders.thelab.core.ui.compose.base.BaseViewModel
import com.riders.thelab.navigator.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

@Suppress("EmptyMethod")
@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: IRepository) : BaseViewModel() {

    val list = listOf("test", "mike", "chronopost", "john")

    //////////////////////////////////////////
    // DataStore
    //////////////////////////////////////////
    private val dataStoreEmail = repository.getEmailPref().asLiveData()
    private val dataStorePassword = repository.getPasswordPref().asLiveData()
    private val dataStoreRememberCredentials = repository.isRememberCredentialsPref().asLiveData()

    //////////////////////////////////////////
    // Compose states
    //////////////////////////////////////////
    // Login States
    // User Login State
    private var _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.None)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    // Login field state
    private var _loginFieldUiState =
        MutableStateFlow<LoginFieldsUIState.Login>(LoginFieldsUIState.Login.Idle)
    val loginFieldUiState: StateFlow<LoginFieldsUIState.Login> = _loginFieldUiState

    // Password field state
    private var _passwordFieldUiState =
        MutableStateFlow<LoginFieldsUIState.Password>(LoginFieldsUIState.Password.Idle)
    val passwordFieldUiState: StateFlow<LoginFieldsUIState.Password> = _passwordFieldUiState

    // Network State
    var hasInternetConnection: Boolean by mutableStateOf(false)
        private set

    var login by mutableStateOf(if (BuildConfig.DEBUG) "jane.doe@test.com" else "")
        private set
    var password by mutableStateOf(if (BuildConfig.DEBUG) "test1234" else "")
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val loginHasError: StateFlow<Boolean> =
        snapshotFlow { login }
            .mapLatest {
                // isUsernameAvailable(it)
                list.contains(login)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val loginHasLocalError by derivedStateOf {
        // synchronous call
        // !Patterns.EMAIL_ADDRESS.matcher(login).matches()
        login.isBlank()
    }

    var isRememberCredentials: Boolean by mutableStateOf(false)
        private set


    ////////////////////////////////////////
    // Composable methods
    ////////////////////////////////////////
    private fun updateLoginUiState(newState: LoginUiState) {
        _loginUiState.value = newState
    }

    fun updateLoginFieldUiState(newLoginFieldState: LoginFieldsUIState.Login) {
        _loginFieldUiState.value = newLoginFieldState
    }

    fun updatePasswordFieldUiState(newPasswordFieldState: LoginFieldsUIState.Password) {
        _passwordFieldUiState.value = newPasswordFieldState
    }

    private fun updateHasInternetConnection(hasInternet: Boolean) {
        this.hasInternetConnection = hasInternet
    }

    fun updateLogin(value: String) {
        login = value
    }

    fun updatePassword(value: String) {
        password = value
    }

    fun updateIsRememberCredentials(remember: Boolean) {
        this.isRememberCredentials = remember

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repository.saveRememberCredentialsPref(remember)
        }
    }

    fun isUsernameAvailable(login: String): Boolean = list.contains(login)

    //////////////////////////////////////////
    // Coroutines
    //////////////////////////////////////////
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            Timber.e("coroutineExceptionHandler | ${throwable.message}")

            if (throwable is UnknownHostException) {
                updateHasInternetConnection(false)
            }

            val cs404: CharSequence = "404".subSequence(0, 3)
            val cs503: CharSequence = "503".subSequence(0, 3)
            if (throwable.message?.contains(cs404, true) == true
                || throwable.message?.contains(cs503, true) == true
            ) {
                updateLoginUiState(LoginUiState.Error(ApiResponse("", 404, null)))
            } else {
                // Default error
                updateLoginUiState(
                    LoginUiState.Error(
                        ApiResponse(
                            "Error while logging user. Please verify your credentials",
                            404,
                            null
                        )
                    )
                )
            }
        }


    ///////////////////////////////
    //
    // OVERRIDE
    //
    ///////////////////////////////
    init {
        if (true == dataStoreRememberCredentials.value) {
            dataStoreEmail.value?.let { updateLogin(it) }
            dataStorePassword.value?.let { updatePassword(it) }
            dataStoreRememberCredentials.value?.let { updateIsRememberCredentials(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.e("onCleared()")
    }


    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    fun observeNetworkState(networkManager: LabNetworkManager) {
        Timber.d("observeNetworkState()")

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            networkManager.getNetworkState().collect { networkState ->
                when (networkState) {
                    is NetworkState.Available -> {
                        Timber.d("network state is Available. All set.")
                        updateHasInternetConnection(true)
                    }

                    is NetworkState.Losing -> {
                        Timber.w("network state is Losing. Internet connection about to be lost")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Lost -> {
                        Timber.e("network state is Lost. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Unavailable -> {
                        Timber.e("network state is Unavailable. Should not allow network calls initialization")
                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Undefined -> {
                        Timber.i("network state is Undefined. Do nothing")
                    }
                }
            }
        }
    }

    /**
     * logging in user. Will make http post request with name, email
     * as parameters
     */
    fun login() {
        Timber.d("login()")

        if (loginHasError.value) {
            Timber.e("loginHasError.value")
            return
        }
        if (loginHasLocalError) {
            Timber.e("loginHasLocalError")
            return
        }

        if (!isValidLogin()) {
            Timber.e("!isValidLogin()")
            return
        }
        if (!isValidPassword()) {
            Timber.e("!isValidPassword()")
            return
        }

        updateLoginUiState(LoginUiState.Connecting)

        logUser(login, password)
    }


    private fun isValidLogin(): Boolean =
        (login.trim().isNotEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(login).matches()) ||
                (login.trim().isNotEmpty() && login.length >= 2)

    private fun isValidPassword(): Boolean = password.trim().isNotEmpty() && password.length >= 4


    private fun logUser(usernameOrEmail: String, password: String) {
        Timber.d("logUser() | username Or Email: $usernameOrEmail, password:$password")

        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {

            // Simulate long-time running operation
            delay(1_500)

            repository.logUser(usernameOrEmail, password.encodeToSha256())?.let {
                Timber.d("user found: $it")
                if (isRememberCredentials) {
                    saveUserDataInDataStore(usernameOrEmail, password)
                }
                updateLoginUiState(LoginUiState.UserSuccess(it))
            } ?: run {
                Timber.e("user object is null. Unable to get user, please make sure to enter a valid username or email with a valid password.")
                updateLoginUiState(
                    LoginUiState.UserError("user object is null. Unable to get user, please make sure to enter a valid username or email with a valid password.")
                )
            }
        }
    }

    private fun saveUserDataInDataStore(email: String, password: String) =
        runCatching {
            Timber.d("saveUserDataInDataStore() | runCatching")
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveEmailPref(email)
                repository.savePasswordPref(password)
            }
        }
            .onFailure {
                Timber.e("runCatching | onFailure | error caught with message: ${it.message}")
            }

    fun onEvent(event: UiEvent) {
        Timber.d("onEvent() | $event")

        when (event) {
            is UiEvent.OnUpdateLogin -> {
                updateLogin(event.newLogin)
            }

            is UiEvent.OnUpdatePassword -> {
                updatePassword(event.newPassword)
            }

            is UiEvent.OnUpdateIsRememberCredentials -> {
                updateIsRememberCredentials(event.checked)
            }

            is UiEvent.OnLoginClicked -> {
                login()
            }

            else -> {
                Timber.e("onEvent() | Else branch")
            }
        }
    }

    fun updateGoogleUser(navigator: Navigator, account: GoogleSignInAccount) {
        val user: User = account.toModel()
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val id = repository.insertUser(user)

            if (-1L != id) {
                saveUserDataInDataStore(email = user.email, password = user.password)
                navigator.callMainActivity()
            }
        }
    }

    fun isGoogleUserLogged(navigator: Navigator) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val email = repository.getEmailPref().first()
            val googleUser: User? = repository.getUserWithGoogle(email)

            googleUser?.let {
                navigator.callMainActivity()
            } ?: run { Timber.e("isGoogleUserLogged() | googleUser is null") }
        }
    }
}
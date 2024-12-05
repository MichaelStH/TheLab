package com.riders.thelab.ui.login

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.riders.thelab.R
import com.riders.thelab.core.data.local.model.compose.LoginFieldsUIState
import com.riders.thelab.core.data.local.model.compose.LoginUiState
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.component.LabHtmlText
import com.riders.thelab.core.ui.compose.theme.Shapes
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.theme.md_theme_dark_onPrimaryContainer
import com.riders.thelab.core.ui.compose.theme.md_theme_light_onPrimaryContainer
import com.riders.thelab.core.ui.compose.utils.animatePlacement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


///////////////////////////////
//
// COMPOSE
//
///////////////////////////////
@Composable
fun GoogleButton(uiEvent: (UiEvent) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { uiEvent.invoke(UiEvent.OnGoogleButtonLoginClicked) },
            colors = ButtonDefaults.buttonColors(containerColor = if (!isSystemInDarkTheme()) md_theme_dark_onPrimaryContainer else md_theme_light_onPrimaryContainer),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = com.riders.thelab.core.ui.R.drawable.googleg_color),
                    contentDescription = "google_icon"
                )
                Text(
                    text = "Continue with Google".uppercase(Locale.getDefault()),
                    style = TextStyle(color = if (!isSystemInDarkTheme()) Color.Black else Color.White)
                )
            }
        }
    }
}

@Composable
fun SignUpButton(uiEvent: (UiEvent) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { uiEvent.invoke(UiEvent.OnSignUpClicked) },
            colors = ButtonDefaults.buttonColors(containerColor = if (!isSystemInDarkTheme()) md_theme_dark_onPrimaryContainer else md_theme_light_onPrimaryContainer),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LabHtmlText(
                    modifier = Modifier.fillMaxWidth(),
                    stringResId = com.riders.thelab.core.ui.R.string.no_account_register,
                    textAlignment = View.TEXT_ALIGNMENT_CENTER,
                    onClick = { uiEvent.invoke(UiEvent.OnSignUpClicked) }
                )
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun LoginContent(
    version: String,
    loginUiState: LoginUiState,
    loginFieldState: LoginFieldsUIState.Login,
    login: String,
    loginHasError: Boolean,
    loginHasLocalError: Boolean,
    passwordFieldState: LoginFieldsUIState.Password,
    password: String,
    isRememberCredentialsChecked: Boolean,
    uiEvent: (UiEvent) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val versionVisibility = remember { mutableStateOf(true) }
    val formVisibility = remember { mutableStateOf(false) }
    val registerVisibility = remember { mutableStateOf(false) }

    var arrangement: Arrangement.Vertical by remember { mutableStateOf(Arrangement.Center) }

    TheLabTheme {

        // Main column
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            // Logo icon with version
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (arrangement == Arrangement.Center) {
                    Arrangement.Center
                } else {
                    Arrangement.Bottom
                }
            ) {
                Card(
                    modifier = Modifier
                        .size(96.dp)
                        .animatePlacement(),
                    shape = Shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.ic_lab_twelve_background))
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        painter = painterResource(id = R.drawable.ic_the_lab_12_logo_white),
                        contentDescription = "Lab Icon"
                    )
                }

                // Version
                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterHorizontally),
                    visible = if (LocalInspectionMode.current) false else versionVisibility.value,
                    exit = fadeOut()
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = if (LocalInspectionMode.current) "12.0.0" else version,
                        style = TextStyle(
                            color = if (!isSystemInDarkTheme()) Color.Black else Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // Form visibility
            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                visible = if (LocalInspectionMode.current) true else formVisibility.value
            ) {
                Form(
                    loginUiState = loginUiState,
                    loginFieldState = loginFieldState,
                    login = login,
                    loginHasError = loginHasError,
                    loginHasLocalError = loginHasLocalError,
                    passwordFieldState = passwordFieldState,
                    password = password,
                    isRememberCredentialsChecked = isRememberCredentialsChecked,
                    uiEvent = uiEvent
                )
            }

            // Register button
            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(40.dp)
                    .weight(1f),
                visible = if (LocalInspectionMode.current) true else registerVisibility.value
            ) {
                // SignUp button
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoogleButton(uiEvent = uiEvent)

                    HorizontalDivider(modifier = Modifier.fillMaxWidth(.85f))

                    SignUpButton(uiEvent = uiEvent)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            delay(200L)
            versionVisibility.value = false
            delay(200L)
            formVisibility.value = true
            registerVisibility.value = true
            delay(150L)
            arrangement = Arrangement.Bottom

            /*if (BuildConfig.DEBUG) {
                uiEvent.invoke(UiEvent.OnLoginClicked)
            }*/
        }
    }

    if (loginUiState is LoginUiState.UserSuccess) {
        LaunchedEffect(Unit) {
            uiEvent.invoke(UiEvent.OnLaunchMainActivity)
        }
    }
}


///////////////////////////////////////////////////
//
// PREVIEWS
//
///////////////////////////////////////////////////
@DevicePreviews
@Composable
fun PreviewSignUpButton() {
    TheLabTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            SignUpButton {}
        }
    }
}

@DevicePreviews
@Composable
fun PreviewLoginContent(@PreviewParameter(PreviewProviderLoginState::class) loginUiState: LoginUiState) {
    val loginFieldUiState: LoginFieldsUIState.Login = LoginFieldsUIState.Login.Ok
    val passwordUiState: LoginFieldsUIState.Password = LoginFieldsUIState.Password.Ok

    val login =
        if (loginUiState is LoginUiState.UserSuccess) loginUiState.user.email else "john.smith@test.com"
    val password =
        if (loginUiState is LoginUiState.UserSuccess) loginUiState.user.password else "test1234"

    TheLabTheme {
        LoginContent(
            version = "12.10.3",
            loginUiState = loginUiState,
            loginFieldState = loginFieldUiState,
            login = login,
            loginHasError = false,
            loginHasLocalError = false,
            passwordFieldState = passwordUiState,
            password = password,
            isRememberCredentialsChecked = true
        ) {}
    }
}
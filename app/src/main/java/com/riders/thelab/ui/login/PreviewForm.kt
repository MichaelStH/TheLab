package com.riders.thelab.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riders.thelab.R
import com.riders.thelab.core.data.local.model.compose.LoginFieldsUIState
import com.riders.thelab.core.data.local.model.compose.LoginUiState
import com.riders.thelab.core.data.local.model.compose.WindowSizeClass
import com.riders.thelab.core.ui.compose.annotation.DevicePreviews
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.compose.theme.md_theme_dark_primary
import com.riders.thelab.core.ui.compose.theme.md_theme_dark_primaryContainer
import com.riders.thelab.core.ui.compose.theme.md_theme_light_primary
import com.riders.thelab.core.ui.compose.theme.md_theme_light_primaryContainer

///////////////////////////////////////////////////
//
// COMPOSE
//
///////////////////////////////////////////////////
@Composable
fun Login(
    loginFieldState: LoginFieldsUIState.Login,
    focusRequester: FocusRequester,
    login: String,
    loginHasError: Boolean,
    loginHasLocalError: Boolean,
    uiEvent: (UiEvent) -> Unit
) {

    TheLabTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = login,
                onValueChange = { uiEvent.invoke(UiEvent.OnUpdateLogin(it)) },
                placeholder = { Text(text = "Login") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp),
                // Change different colors of the text field view
                colors = TextFieldDefaults.colors(
                    // containerColor = if (!focus.value) Color.DarkGray else lightBlue,
                    // textColor = if (!focus.value) Color.Gray else Color.White,
                    cursorColor = Color.Blue,
                    focusedIndicatorColor = Color.Transparent, //hide the indicator
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            AnimatedVisibility(visible = loginHasError) {
                Text(
                    text = "Login not available. Please choose a different one.",
                    color = Color(0xFFF02828)
                )
            }

            AnimatedVisibility(visible = loginHasLocalError) {
                Text(
                    text = "Login contains invalid characters!",
                    color = Color(0xFFF02828)
                )
            }
        }
    }
}

@Composable
fun Password(
    passwordFieldState: LoginFieldsUIState.Password,
    focusRequester: FocusRequester,
    password: String,
    uiEvent: (UiEvent) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordVisible = remember { mutableStateOf(false) }

    TheLabTheme {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = password,
            onValueChange = { uiEvent.invoke(UiEvent.OnUpdatePassword(it)) },
            placeholder = { Text(text = "Password (6+ characters") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisible.value = !passwordVisible.value
                }) {
                    Icon(
                        imageVector = if (!passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            shape = RoundedCornerShape(bottomStart = 35.dp, bottomEnd = 35.dp),
            maxLines = 1,
            singleLine = true,
            // Change different colors of the text field view
            colors = TextFieldDefaults.colors(
                // containerColor = if (!focus.value) Color.DarkGray else lightBlue,
                // textColor = if (!focus.value) Color.Gray else Color.White,
                cursorColor = Color.Blue,
                focusedIndicatorColor = Color.Transparent, //hide the indicator
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ErrorMessage(uiState: LoginUiState) {
    val alphaAnimation = remember { Animatable(0f) }

    // Text error
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnimation.value),
        text = stringResource(id = R.string.err_msg_wrong_email_or_password),
        color = Color.Red
    )

    if (uiState is LoginUiState.UserError) {
        LaunchedEffect(uiState) {
            when (uiState) {
                is LoginUiState.UserError -> {
                    alphaAnimation.animateTo(1f)
                }

                else -> {
                    alphaAnimation.animateTo(0f)
                }
            }
        }
    }
}

@Composable
fun RememberUser(
    modifier: Modifier,
    isRememberCredentialsChecked: Boolean,
    uiEvent: (UiEvent) -> Unit
) {
    Box(modifier = modifier, contentAlignment = CenterStart) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isRememberCredentialsChecked,
                onCheckedChange = { uiEvent.invoke(UiEvent.OnUpdateIsRememberCredentials(it)) }
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = true,
                        onClick = {
                            uiEvent.invoke(
                                UiEvent.OnUpdateIsRememberCredentials(!isRememberCredentialsChecked)
                            )
                        }),
                text = stringResource(id = R.string.remember_me),
                fontSize = 12.sp,
                maxLines = 2,
                color = if (!isSystemInDarkTheme()) Color.DarkGray else Color.LightGray
            )
        }
    }
}

@Composable
fun Submit(modifier: Modifier, uiState: LoginUiState, uiEvent: (UiEvent) -> Unit) {

    val buttonContainerColor =
        if (!isSystemInDarkTheme()) md_theme_light_primaryContainer else md_theme_dark_primaryContainer
    val buttonContentColor = if (!isSystemInDarkTheme()) Color.DarkGray else Color.LightGray
    TheLabTheme {
        Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    onClick = { uiEvent.invoke(UiEvent.OnLoginClicked) },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    enabled = uiState !is LoginUiState.Connecting,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonContainerColor)
                ) {
                    AnimatedContent(
                        targetState = uiState,
                        label = "login_button_animated_content"
                    ) { targetState ->
                        when (targetState) {
                            LoginUiState.Connecting -> {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = if (!isSystemInDarkTheme()) md_theme_dark_primary else md_theme_light_primary
                                    )
                                }
                            }

                            else -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        text = "Log In",
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        color = buttonContentColor
                                    )


                                    Icon(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(.5f),
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = buttonContentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Form(
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
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val windowSizeClass: WindowSizeClass = if (LocalInspectionMode.current) {
        WindowSizeClass.COMPACT
    } else {
        (context as LoginActivity).getDeviceWindowsSizeClass()
    }

    TheLabTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    when (windowSizeClass) {
                        WindowSizeClass.MEDIUM -> {
                            56.dp
                        }

                        WindowSizeClass.EXPANDED -> {
                            72.dp
                        }

                        WindowSizeClass.COMPACT -> {
                            16.dp
                        }

                        else -> {
                            0.dp
                        }
                    }
                ),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Login(
                loginFieldState = loginFieldState,
                focusRequester = focusRequester,
                login = login,
                loginHasError = loginHasError,
                loginHasLocalError = loginHasLocalError,
                uiEvent = uiEvent
            )

            Password(
                passwordFieldState = passwordFieldState,
                focusRequester = focusRequester,
                password = password,
                uiEvent = uiEvent
            )

            AnimatedVisibility(visible = loginUiState is LoginUiState.UserError) {
                Spacer(modifier = Modifier.size(8.dp))
                ErrorMessage(uiState = loginUiState)
                Spacer(modifier = Modifier.size(8.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RememberUser(
                    modifier = Modifier.weight(2f),
                    isRememberCredentialsChecked = isRememberCredentialsChecked,
                    uiEvent = uiEvent
                )

                Submit(
                    modifier = Modifier.weight(1f),
                    uiState = loginUiState,
                    uiEvent = uiEvent
                )
            }
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
fun PreviewLogin(@PreviewParameter(PreviewProviderLoginFieldsUIState::class) uiState: LoginFieldsUIState.Login) {
    TheLabTheme {
        Login(
            loginFieldState = uiState,
            login = "John5521",
            focusRequester = FocusRequester(),
            loginHasError = false,
            loginHasLocalError = false
        ) {}
    }
}

@DevicePreviews
@Composable
fun PreviewPassword(@PreviewParameter(PreviewProviderPasswordFieldsUIState::class) uiState: LoginFieldsUIState.Password) {
    TheLabTheme {
        Password(
            passwordFieldState = uiState,
            password = "test1234",
            focusRequester = FocusRequester()
        ) {}
    }
}

@DevicePreviews
@Composable
fun PreviewRememberUser() {
    TheLabTheme {
        RememberUser(
            modifier = Modifier.height(56.dp),
            isRememberCredentialsChecked = true
        ) {}
    }
}

@DevicePreviews
@Composable
fun PreviewSubmit(@PreviewParameter(PreviewProviderLoginState::class) uiState: LoginUiState) {
    TheLabTheme { Submit(modifier = Modifier, uiState = uiState) {} }
}

@DevicePreviews
@Composable
fun PreviewForm(@PreviewParameter(PreviewProviderLoginState::class) uiState: LoginUiState) {
    val loginFieldUiState: LoginFieldsUIState.Login = LoginFieldsUIState.Login.Ok
    val passwordUiState: LoginFieldsUIState.Password = LoginFieldsUIState.Password.Ok

    val login =
        if (uiState is LoginUiState.UserSuccess) uiState.user.email else "john.smith@test.com"
    val password = if (uiState is LoginUiState.UserSuccess) uiState.user.password else "test1234"

    TheLabTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Form(
                loginUiState = uiState,
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
}
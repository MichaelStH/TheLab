package com.riders.thelab.core.data.local.model.compose.settings

import com.riders.thelab.core.data.local.model.User
import kotools.types.text.NotBlankString

sealed interface UserUiState {
    data class Success(val user: User) : UserUiState
    data class Error(val message: NotBlankString, val throwable: Throwable? = null) : UserUiState
    data object Loading : UserUiState
    data object NotConnected : UserUiState
}
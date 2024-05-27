package com.riders.thelab.feature.googledrive.ui

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.riders.thelab.core.data.local.model.google.GoogleAccountModel

sealed interface UiEvent {
    data class OnSignIn(val context:Context): UiEvent
    data class OnHandleAccount(val account: GoogleAccountModel): UiEvent
    data object OnSignOut: UiEvent
}
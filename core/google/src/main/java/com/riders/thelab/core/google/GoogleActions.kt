package com.riders.thelab.core.google

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface GoogleActions {
    fun onConnected(account: GoogleSignInAccount)

    fun onDisconnected()
}
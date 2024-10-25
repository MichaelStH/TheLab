package com.riders.thelab.core.google

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

abstract interface GoogleActions {
    abstract fun onConnected(account: GoogleSignInAccount)

    abstract fun onDisconnected()
}
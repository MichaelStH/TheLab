package com.riders.thelab.feature.googledrive.core.google

import android.annotation.SuppressLint
import android.credentials.GetCredentialException
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.services.drive.DriveScopes
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.data.local.model.google.GoogleAccountModel
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.feature.googledrive.BuildConfig
import com.riders.thelab.feature.googledrive.base.BaseGoogleActivity
import com.riders.thelab.feature.googledrive.utils.toGoogleAccountModel
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID


class GoogleSignInManager(private val activity: BaseGoogleActivity) : GoogleKeyValidation() {

    ////////////////////////////////////////////////////////////
    // Variables
    ////////////////////////////////////////////////////////////
    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    var mGoogleSignInClient: GoogleSignInClient? = null

    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    var mLastGoogleAccount: GoogleSignInAccount? = null

    private var mLastGoogleAccountCredential: GoogleIdTokenCredential? = null


    ////////////////////////////////////////////////////////////
    // Authentication
    ////////////////////////////////////////////////////////////
    // TODO: This sign in method will be removed in 2025 by Google. Use Credential Manager instead.
    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    fun signInLegacy() {
        Timber.i("signInLegacy()")

        GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.riders.thelab.core.ui.R.string.server_client_id))
            .requestEmail()
            .requestProfile()
            .build()
            .run {
                signInLegacy(this)
            }

    }

    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    fun signInLegacyForDrive() {
        Timber.i("signInLegacyForDrive()")

        GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.riders.thelab.core.ui.R.string.server_client_id))
            .requestEmail()
            .requestProfile()
            .requestScopes(
                Scope(DriveScopes.DRIVE),
                Scope(DriveScopes.DRIVE_FILE),
                Scope(DriveScopes.DRIVE_PHOTOS_READONLY),
                Scope(DriveScopes.DRIVE_METADATA_READONLY)
            )
            .build()
            .run {
                signInLegacy(this)
            }
    }

    private fun signInLegacy(googleSignInOptions: GoogleSignInOptions) {
        Timber.i("signInLegacy()")
        val message: String

        if (!isUserSignedInLegacy()) {
            message = "User is NOT signed in"
            Timber.e("signInLegacy() | $message")

            mGoogleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions)

            // From the GoogleSignInClient object we can get the signInIntent
            // which we can use in startActivityForResult to trigger the login flow
            val signInIntent = mGoogleSignInClient?.signInIntent

            if (null != signInIntent) {
                activity.mGoogleSignInRequestLauncher.launch(signInIntent)
            }
        } else {
            message = "user is already signed in"
            Timber.e("signInLegacy() | $message")
            UIManager.showToast(activity, message)
        }
    }

    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    fun getGoogleSignInClientLegacy(): GoogleSignInClient =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
            .apply {
                Timber.d("getGoogleSignInClientLegacy()")
            }
            .run {
                getGoogleSignInClientLegacy(this)
            }


    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    fun getGoogleSignInClientLegacyForDrive(): GoogleSignInClient =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(DriveScopes.DRIVE),
                Scope(DriveScopes.DRIVE_FILE),
                Scope(DriveScopes.DRIVE_PHOTOS_READONLY),
                Scope(DriveScopes.DRIVE_METADATA_READONLY)
            )
            .build()
            .apply {
                Timber.d("getGoogleSignInClientLegacyForDrive()")
            }
            .run {
                getGoogleSignInClientLegacy(this)
            }

    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    fun getGoogleSignInClientLegacy(signInOptions: GoogleSignInOptions): GoogleSignInClient =
        GoogleSignIn.getClient(activity, signInOptions).also {
            Timber.d("getGoogleSignInClient() | client: $it")
        }


    suspend fun signIn(onAccountFetched: (GoogleAccountModel) -> Unit) {
        Timber.d("signIn()")

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activity.getString(com.riders.thelab.core.ui.R.string.server_client_id))
            .setAutoSelectEnabled(true)
            .setNonce(hashedNonce)
            .build()
            .also {
                Timber.d("signIn() | google Id Option: $it")
            }

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
            .also {
                Timber.d("signIn() | request: $it")
            }

        // Use your app or activity context to instantiate a client instance of CredentialManager.
        val credentialManager = CredentialManager.create(activity)

        runCatching {
            val result = credentialManager.getCredential(context = activity, request = request)
                .also {
                    Timber.d("signIn() | result: $it")
                }

            val credential = result.credential.also {
                Timber.d("signIn() | credential: $it")
            }

            GoogleIdTokenCredential.createFrom(credential.data).also { tokenCredential ->
                mLastGoogleAccountCredential = tokenCredential

                onAccountFetched(
                    tokenCredential.toGoogleAccountModel()
                        .also { Timber.d("signIn() | account: $it") }
                )
            }
        }
            .onFailure { exception ->
                Timber.e("signIn() | onFailure | error caught with message: ${exception.message} (class: ${exception.javaClass.canonicalName})")

                if (LabCompatibilityManager.isTiramisu()) {
                    @SuppressLint("NewApi")
                    when (exception) {
                        is GetCredentialException -> exception.printStackTrace()
                    }
                }

                when (exception) {
                    is GoogleIdTokenParsingException -> exception.printStackTrace()
                    else -> {}
                }

                // Timber.e("signIn() | onFailure | error caught with message: ${exception.message} (class: ${exception.javaClass.canonicalName})")
            }
            .onSuccess {
                if (BuildConfig.DEBUG) {
                    Timber.d("signIn() | onSuccess | google id: ${mLastGoogleAccountCredential?.idToken}")
                }
            }
    }


    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    fun signOut(
        activity: BaseGoogleActivity,
        onFailure: (throwable: Throwable?) -> Unit,
        onSuccess: (Boolean) -> Unit
    ) {
        Timber.e("signOut()")

        if (null == mGoogleSignInClient) {
            mGoogleSignInClient = getGoogleSignInClientLegacy()
        }

        mGoogleSignInClient?.let { signInClient ->
            signInClient.signOut()
                .addOnFailureListener(activity) { throwable ->
                    Timber.e("task | addOnFailureListener | message: ${throwable.message} (class: ${throwable::class.java.canonicalName})")
                    onFailure(throwable)
                }
                .addOnSuccessListener(activity) {
                    Timber.d("task | addOnSuccessListener | value: $it")
                }
                .addOnCompleteListener(activity) {
                    Toast.makeText(activity, "Signed Out", Toast.LENGTH_SHORT).show()
                    mLastGoogleAccount = null
                    onSuccess(true)
                }
        } ?: run {
            Timber.e("signOut | mGoogleSignInClient object is null")

            mLastGoogleAccount?.let {
                mLastGoogleAccount = null
                Toast.makeText(activity, "Signed Out", Toast.LENGTH_SHORT).show()
                onSuccess(true)
            } ?: run {
                Timber.e("signOut | mLastGoogleAccount object is null")
            }
        }
    }

    ////////////////////////////////////////////////////////////
    // Fetching
    ////////////////////////////////////////////////////////////
    fun getLastSignedInAccount(): GoogleIdTokenCredential? = mLastGoogleAccountCredential


    ////////////////////////////////////////////////////////////
    // Checks
    ////////////////////////////////////////////////////////////
    @Deprecated("This sign in method will be removed in 2025 by Google. Use Credential Manager instead")
    fun isUserSignedInLegacy(): Boolean = GoogleSignIn.getLastSignedInAccount(activity)
        .apply {
            Timber.i("isUserSignedIn() | $this")
            if (null != this) mLastGoogleAccount = this
        }
        .run { this != null }

    fun isUserSignedIn(): Boolean =
        run { null == mLastGoogleAccountCredential }.also { Timber.i("isUserSignedIn() | $this") }


    /*fun handleGoogleSignInResult(requestCode: Int, intent: Intent) {
        Timber.d("handleGoogleSignInResult()")

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)

            if (task.isSuccessful) {
                // Sign in succeeded, proceed with account
                val account: GoogleSignInAccount = task.result
                Timber.d("handleGoogleSignInResult() | Sign in succeeded, proceed with accountSign in succeeded, proceed with account")
            } else {
                // Sign in failed, handle failure and update UI
                Timber.e("handleGoogleSignInResult() | Sign in failed, handle failure and update UI")
                // ...
            }
        }
    }*/

    fun setGoogleAccount(account: GoogleSignInAccount) {
        Timber.i("setGoogleAccount() | account: $account")
        this.mLastGoogleAccount = account
    }

    companion object {
        private const val RC_SIGN_IN: Int = 9001

        private var mInstance: GoogleSignInManager? = null
        fun getInstance(activity: BaseGoogleActivity): GoogleSignInManager =
            mInstance ?: GoogleSignInManager(activity).also { mInstance = it }
    }
}
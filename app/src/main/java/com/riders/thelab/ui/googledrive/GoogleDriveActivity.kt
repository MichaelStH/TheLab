package com.riders.thelab.ui.googledrive

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.internal.OnConnectionFailedListener
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.riders.thelab.R
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.databinding.ActivityGoogleDriveBinding
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class GoogleDriveActivity
    : BaseAppCompatActivity(),
    CoroutineScope, OnConnectionFailedListener {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()


    private var _viewBinding: ActivityGoogleDriveBinding? = null

    private val binding get() = _viewBinding!!

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewBinding = ActivityGoogleDriveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // For sample only: make sure there is a valid server client ID.
        validateServerClientID()
    }

    override fun onPause() {
        super.onPause()
        Timber.e("onPause()")
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume()")

        //mGoogleApiClient.connect()
    }


    @Deprecated("DEPRECATED - Use registerActivityForResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.i("onActivityResult() - $requestCode")

        when (requestCode) {
            REQUEST_SIGN_IN -> {
                if (resultCode == RESULT_OK && data != null) {
                    handleSignData(data)
                } else {
                    Timber.d("Signin request failed")
                }
            }

            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            CONST_SIGN_IN -> {
                handleSignData(data)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun backPressed() {
        finish()
    }

    override fun onStop() {
        super.onStop()
        Timber.e("onStop()")

        // disconnect Google Android Drive API connection.
        //mGoogleApiClient.disconnect();
        signOut()

    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("onDestroy()")
        _viewBinding = null
    }


    /**
     * Validates that there is a reasonable server client ID in strings.xml, this is only needed
     * to make sure users of this sample follow the README.
     */
    private fun validateServerClientID() {
        Timber.i("validateServerClientID()")
        val serverClientId = getString(R.string.server_client_id)
        val suffix = ".apps.googleusercontent.com"

        if (!serverClientId.trim { it <= ' ' }.endsWith(suffix)) {
            val message =
                "Invalid server client ID in strings.xml, must end with $suffix"
            Timber.d(message)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } else {
            googleSignIn()
        }
    }


    private fun isUserSignedIn(): Boolean {
        Timber.i("isUserSignedIn()")
        val account = GoogleSignIn.getLastSignedInAccount(this)
        return account != null
    }

    private fun googleSignIn() {
        Timber.i("googleSignIn()")

        if (!isUserSignedIn()) {
            Timber.e("NOT isUserSignedIn()")

            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestProfile()
                .requestScopes(Scope(DriveScopes.DRIVE))
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                //.requestScopes(Scope(DriveScopes.DRIVE_METADATA_READONLY))
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

            // From the GoogleSignInClient object we can get the signInIntent
            // which we can use in startActivityForResult to trigger the login flow
            val signInIntent = mGoogleSignInClient?.signInIntent

            if (null != signInIntent) {
                startActivityForResult(signInIntent, CONST_SIGN_IN)
            }
        }
    }

    private fun signOut() {
        mGoogleSignInClient
            ?.signOut()
            ?.addOnCompleteListener(this) {
                Toast.makeText(this@GoogleDriveActivity, "Signed Out", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun handleSignData(data: Intent?) {
        Timber.i("handleSignData()")
        // The Task returned from this call is always completed, no need to attach
        // a listener.
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener {
                Timber.d("addOnSuccessListener()")
                Timber.e("authentication succeed - $it")
            }
            .addOnFailureListener {
                Timber.d("addOnFailureListener()")
                Timber.e("exception : ${it.message}")
            }
            .addOnCompleteListener {
                Timber.d("addOnCompleteListener()")
                if (it.isSuccessful) {
                    Timber.d("isSuccessful ${it.isSuccessful}")
                    // user successfully logged-in
                    Timber.d("account ${it.result.account}")
                    Timber.d("displayName ${it.result.displayName}")
                    Timber.d("Email ${it.result.email}")

                    accessDriveFiles()
                } else {
                    // authentication failed
                    Timber.e("authentication failed - exception : ${it.exception}")
                }
            }
    }

    private fun checkLastSignedInAccount(): Drive? {
        Timber.i("checkLastSignedInAccount()")
        GoogleSignIn.getLastSignedInAccount(this)?.let { googleAccount ->
            Timber.d("connected account - mail : ${googleAccount.email}")

            // Now we need to create a GoogleAccountCredential to authenticate the signed-in account
            // with DRIVE_FILE permission to access the user google drive contents.
            val credential =
                GoogleAccountCredential.usingOAuth2(
                    this,
                    listOf(
                        DriveScopes.DRIVE,
                        DriveScopes.DRIVE_FILE,
                        DriveScopes.DRIVE_METADATA_READONLY
                    )
                )

            credential.selectedAccount = googleAccount.account!!

            // we need to create the Drive service instance via its builder
            // where need to pass AndroidHttp, JacksonFactory,
            // and GoogleAccountCredential as parameters
            /*return Drive
                .Builder(
                    NetHttpTransport.Builder().build(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                )
                .setApplicationName(getString(R.string.app_name))
                .build()*/

        }

        return null
    }


    /**
     * Access contents in google drive
     * No that we’ve authenticated drive instance, lets get started by accessing the existing files in user google drive.
     * Inside the drive class we’ve a function called files() —
     * An accessor for creating requests from the Files collection.
     */
    private fun accessDriveFiles() {
        Timber.i("accessDriveFiles()")
        checkLastSignedInAccount()?.let { googleDriveService ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Timber.d("googleDriveService.files()")

                    var pageToken: String?

                    do {
                        val result: FileList = googleDriveService.files().list().apply {
                            /* spaces — A comma-separated list of spaces to query within the corpus.
                             * Supported values are drive, appDataFolder and photos. Here we use drive.
                             */
                            spaces = "drive, appDataFolder"

                            /* fields — The information required about each file like id, name, etc.
                             */
                            fields = "nextPageToken, files(id, name)"
                            pageToken = this.pageToken
                        }.execute()

                        for (file in result.files) {
                            Timber.e("name=${file.name} id=${file.id}")
                        }
                    } while (pageToken != null)

                } catch (ex: UserRecoverableAuthIOException) {
                    Timber.e(ex.message)
                    ex.printStackTrace()

                    withContext(Dispatchers.Main) {
                        startActivityForResult(ex.intent, REQUEST_SIGN_IN)
                    }
                } catch (transientEx: IOException) {
                    // network or server error, the call is expected to succeed if you try again later.
                    // Don't attempt to call again immediately - the request is likely to
                    // fail, you'll hit quotas or back-off.
                    Timber.e(transientEx.message)
                    transientEx.printStackTrace()
                } catch (authEx: GoogleAuthException) {
                    // Failure. The call is not expected to ever succeed so it should not be
                    // retried.
                    Timber.e(authEx.message)
                    authEx.printStackTrace()
                }
            }

        }
    }


    fun downloadFileFromGDrive(id: String) {
        Timber.i("downloadFileFromGDrive()")
        checkLastSignedInAccount()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.Default).launch {
                val gDriveFile = googleDriveService.Files().get(id).execute()
            }
        } ?: Timber.d("Signin error - not logged in")
    }

    private fun init() {

        // Build a new authorized API client service.
        val httpTransport = NetHttpTransport()
        val service: Drive = Drive.Builder(
            httpTransport,
            JSON_FACTORY,
            getCredentials(httpTransport)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()

        // Print the names and IDs for up to 10 files.
        val result: FileList = service.files().list()
            .setPageSize(10)
            .setFields("nextPageToken, files(id, name)")
            .execute()
        val files: MutableList<com.google.api.services.drive.model.File>? = result.files
        if (files.isNullOrEmpty()) {
            println("No files found.")
        } else {
            println("Files:")
            for (file in files) {
                System.out.printf("%s (%s)\n", file.name, file.id)
            }
        }
    }

    /**
     * Creates an authorized Credential object.
     * @param netHttpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    fun getCredentials(netHttpTransport: NetHttpTransport): HttpRequestInitializer {
        // Load client secrets.
        val mInputStream: InputStream =
            this@GoogleDriveActivity.javaClass.getResourceAsStream(CREDENTIALS_FILE_PATH)
                ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets: GoogleClientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(mInputStream))

        // Build flow and trigger user authorization request.
        val flow: GoogleAuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
            netHttpTransport, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // Called whenever the API client fails to connect.
        Timber.e("GoogleApiClient connection failed: %s", result.toString())

        if (!result.hasResolution()) {

            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0)
                ?.show()
            return
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

        try {

            //result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (e: IntentSender.SendIntentException) {

            e.printStackTrace()
            Timber.e("Exception while starting resolution activity")
        }
    }

    companion object {

        private const val APPLICATION_NAME: String = "Google Drive API Java Quickstart"

        //        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
        private val JSON_FACTORY: JsonFactory? = null
        private const val TOKENS_DIRECTORY_PATH: String = "tokens"

        /**
         * Global instance of the scopes required by this quickstart.
         * If modifying these scopes, delete your previously saved tokens/ folder.
         */
        private val SCOPES: List<String> =
            Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY)
        private const val CREDENTIALS_FILE_PATH: String = "/credentials.json"

        private const val REQUEST_SIGN_IN = 1238
        private const val CONST_SIGN_IN = 1236
    }
}
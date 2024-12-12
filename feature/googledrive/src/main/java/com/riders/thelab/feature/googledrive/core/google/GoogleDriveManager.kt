package com.riders.thelab.feature.googledrive.core.google

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.drive.DriveClient
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.riders.thelab.core.ui.R
import com.riders.thelab.feature.googledrive.base.BaseGoogleActivity
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class GoogleDriveManager private constructor(private val activity: BaseGoogleActivity) {

    ////////////////////////////////////////////////////////////
    // Variables
    ////////////////////////////////////////////////////////////
    private var mDriveService: Drive? = null
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mJsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    /**
     * Handles high-level drive functions like sync
     */
    private var mDriveClient: DriveClient? = null

    /**
     * Handle access to Drive resources/files.
     */
    private var mDriveResourceClient: DriveResourceClient? = null


    fun getDriveClient(): Drive? {
        Timber.i("getDriveClient()")

        // Now we need to create a GoogleAccountCredential to authenticate the signed-in account
        // with DRIVE_FILE permission to access the user google drive contents.
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
            activity,
            listOf(
                DriveScopes.DRIVE,
                DriveScopes.DRIVE_FILE,
                DriveScopes.DRIVE_PHOTOS_READONLY,
                DriveScopes.DRIVE_METADATA_READONLY
            )
        )

        // we need to create the Drive service instance via its builder
        // where need to pass AndroidHttp, JacksonFactory,
        // and GoogleAccountCredential as parameters
        val drive = Drive.Builder(
            NetHttpTransport.Builder().build(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(activity.getString(R.string.app_name))
            .build()
            .also { mDriveService = it }

        return drive
    }

    fun getDriveClientLegacy(account: GoogleSignInAccount): Drive? {
        Timber.i("getDriveClient()")

        // Now we need to create a GoogleAccountCredential to authenticate the signed-in account
        // with DRIVE_FILE permission to access the user google drive contents.
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
            activity,
            listOf(
                DriveScopes.DRIVE,
                DriveScopes.DRIVE_FILE,
                DriveScopes.DRIVE_PHOTOS_READONLY,
                DriveScopes.DRIVE_METADATA_READONLY
            )
        ).apply {
            selectedAccount = account.account
        }

        // we need to create the Drive service instance via its builder
        // where need to pass AndroidHttp, JacksonFactory,
        // and GoogleAccountCredential as parameters
        val drive = Drive.Builder(
            NetHttpTransport.Builder().build(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(activity.getString(R.string.app_name))
            .build()
            .also { mDriveService = it }

        return drive
    }


    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    fun initializeDriveClientLegacy(signInAccount: GoogleSignInAccount) {
        Timber.i("initializeDriveClientLegacy()")

        mDriveClient = com.google.android.gms.drive.Drive.getDriveClient(activity, signInAccount)
        mDriveResourceClient =
            com.google.android.gms.drive.Drive.getDriveResourceClient(activity, signInAccount)
    }

    fun getDrivesFoldersLegacy() {
        Timber.i("getDrivesFoldersLegacy()")

        mDriveClient?.getDriveId("My drive")
            ?.addOnFailureListener(activity) { throwable ->
                Timber.e("getDrivesFoldersLegacy() | addOnFailureListener | message: ${throwable.message} (class: ${throwable::class.java.canonicalName})")
            }
            ?.addOnSuccessListener(activity) {
                Timber.d("getDrivesFoldersLegacy() | addOnSuccessListener | value: $it")
            }
            ?.addOnCompleteListener(activity) { task ->
                if (!task.isSuccessful) {
                    Timber.e("getDrivesFoldersLegacy() | addOnCompleteListener | getDrives failed")
                } else {
                    Timber.i("getDrivesFoldersLegacy() | addOnCompleteListener | getDrives in successful")
                    val result = task.result

                    if (null != result) {
                        Timber.i("getDrivesFoldersLegacy() | result: $result")
                    }
                }
            }
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    fun createFile(): Task<String> {
        Timber.d("createFile()")

        return Tasks.call(mExecutor) {
            val metadata = File()
                .setParents(listOf("root"))
                .setMimeType("text/plain")
                .setName("Untitled file")
            val googleFile = mDriveService?.files()?.create(metadata)?.execute()
                ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        }
    }

    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * contents.
     */
    fun readFile(fileId: String?): Task<Pair<String, String>> {
        Timber.d("readFile()")

        return Tasks.call(mExecutor) {

            // Retrieve the metadata as a File object.
            val metadata = mDriveService?.files()?.get(fileId)?.execute()
            val name = metadata?.name

            mDriveService?.files()
                ?.get(fileId)
                ?.executeMediaAsInputStream()
                .use { stream ->
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        val stringBuilder = StringBuilder()
                        var line: String?

                        while ((reader.readLine().also { line = it }) != null) {
                            stringBuilder.append(line)
                        }
                        val contents = stringBuilder.toString()
                        return@call name?.let { Pair(it, contents) } ?: Pair("", "")
                    }
                }
        }
    }

    /**
     * Updates the file identified by `fileId` with the given `name` and `content`.
     */
    fun saveFile(fileId: String?, name: String?, content: String?): Task<Any?> {
        Timber.d("saveFile() | fileId: ${fileId}, name: ${name}, content: $content")

        return Tasks.call<Any?>(
            mExecutor
        ) {
            // Create a File containing any metadata changes.
            val metadata = File().setName(name)

            // Convert content to an AbstractInputStreamContent instance.
            val contentStream =
                ByteArrayContent.fromString("text/plain", content)

            // Update the metadata and contents.
            mDriveService?.files()?.update(fileId, metadata, contentStream)?.execute()
            null
        }
    }

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
     * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    fun queryFiles(): Task<FileList>? {
        Timber.d("queryFiles()")

        return mDriveService?.let { service ->
            Tasks.forResult(
                service.files()
                    ?.list()
                    ?.setSpaces("drive")
                    ?.execute()
            )
        } ?: run {
            Timber.e("queryFiles() | mDriveService is null")
            null
        }

        /*return Tasks.call(mExecutor) {
            mDriveService?.files()
                ?.list()
                ?.setSpaces("drive")
                ?.execute()
                ?: run {
                    Timber.e("queryFiles() | mDriveService is null")
                    null
                }
        }*/
    }

    fun getFileForId(fileId:String): Task<File>? {
        Timber.d("queryFiles()")

        return mDriveService?.let { service ->
            Tasks.forResult(
                service.files()
                    ?.get(fileId)
                    ?.execute()
            )
        } ?: run {
            Timber.e("queryFiles() | mDriveService is null")
            null
        }
    }
    fun getInputStreamForId(fileId:String): Task<InputStream>? {
        Timber.d("queryFiles()")

        return mDriveService?.let { service ->
            Tasks.forResult(
                service.files()
                    ?.get(fileId)
                    ?.executeMediaAsInputStream()
            )
        } ?: run {
            Timber.e("queryFiles() | mDriveService is null")
            null
        }
    }

    /**
     * Returns an [Intent] for opening the Storage Access Framework file picker.
     */
    fun createFilePickerIntent(): Intent {
        Timber.d("createFilePickerIntent()")

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("text/plain")

        return intent
    }

    /**
     * Opens the file at the `uri` returned by a Storage Access Framework [Intent]
     * created by [.createFilePickerIntent] using the given `contentResolver`.
     */
    fun openFileUsingStorageAccessFramework(
        contentResolver: ContentResolver,
        uri: Uri?
    ): Task<Pair<String, String>> {
        Timber.d("openFileUsingStorageAccessFramework()")

        return Tasks.call(mExecutor) {
            // Retrieve the document's display name from its metadata.
            var name: String
            contentResolver.query(uri!!, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex =
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    name = cursor.getString(nameIndex)
                } else {
                    throw IOException("Empty cursor returned for file.")
                }
            }
            // Read the document's contents as a String.
            var content: String
            contentResolver.openInputStream(uri).use { `is` ->
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        stringBuilder.append(line)
                    }
                    content = stringBuilder.toString()
                }
            }
            Pair(name, content)
        }
    }

    companion object {
        private var mInstance: GoogleDriveManager? = null
        fun getInstance(activity: BaseGoogleActivity): GoogleDriveManager =
            mInstance ?: GoogleDriveManager(activity).also { mInstance = it }
    }
}
package com.riders.thelab.feature.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.riders.thelab.core.common.storage.FileManager
import com.riders.thelab.core.data.IRepository
import com.riders.thelab.core.data.local.model.compose.Download
import com.riders.thelab.core.ui.compose.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.UnknownHostException
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@Suppress("EmptyMethod")
@HiltViewModel
class DownloadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: IRepository
) : BaseViewModel() {

    private var errorCount: Int = 0

    /////////////////////////////////////////
    // Composable states
    /////////////////////////////////////////
    private var _downloadListState: MutableStateFlow<MutableList<Download>> =
        MutableStateFlow(mutableListOf())
    val downloadListState: StateFlow<List<Download>> = _downloadListState.asStateFlow()

    var downloadList by mutableStateOf(emptyList<Download>())
        private set
    var canDownload: Boolean by mutableStateOf(false)
        private set
    var isDownloadStarted: Boolean by mutableStateOf(false)
        private set

    private var message: String by mutableStateOf("")

    private var eventsList by mutableStateOf(emptyList<String>())

    private var downloadIds: List<Long> by mutableStateOf(emptyList())

    fun updateIsDownloadStarted(isStarted: Boolean) {
        this.isDownloadStarted = isStarted
    }

    fun updateMessage(message: String) {
        this.message = message
        updateEventList(message)
    }

    private fun updateEventList(newEvent: String) {
        this.eventsList += newEvent
    }

    private fun addToDownloadList(filename: String) {
        Timber.d("addToDownloadList() | filename: $filename")

        this.downloadList = downloadList
            .toMutableList()
            .apply {
                add(
                    Download(
                        id = if (downloadList.isEmpty()) 0 else downloadList.size,
                        filename = filename
                    )
                )
            }
            .run { toList() }

        this._downloadListState.value.add(
            Download(
                id = if (this._downloadListState.value.isEmpty()) 0 else this._downloadListState.value.size,
                filename = filename
            )
        )
    }

    fun updateCanDownload(canDownload: Boolean) {
        // Timber.d("updateCanDownload() | canDownload: $canDownload")
        this.canDownload = canDownload
    }

    private fun appendDownloadID(downloadId: Long) {
        // Timber.d("appendDownloadID() | id: $downloadId")
        this.downloadIds += downloadId
    }

    private fun updateDownloadRefId(reference: Long) {
        if (downloadList.isEmpty()) {
            Timber.e("updateDownloadRefId() | list is empty")
        } else {
            Timber.d("updateDownloadRefId() | reference: $reference")

            downloadList.last().apply {
                this.downloadRefId = reference
            }

            this._downloadListState.value.last().downloadRefId = reference
        }
    }

    fun updateFinishedDownloadItem(reference: Long) {
        Timber.d("updateFinishedDownloadItem() | reference: $reference")

        if (downloadList.isEmpty()) {
            Timber.e("updateFinishedDownloadItem() | list is empty")
        } else {
            downloadList.find { reference == it.downloadRefId }?.let {
                it.progress = 100
                it.isComplete = true

                Timber.d("updateFinishedDownloadItem() | matched reference: ${it.downloadRefId}")
            }

            this._downloadListState.value.find { reference == it.downloadRefId }?.let {
                it.progress = 100
                it.isComplete = true

                Timber.d("updateFinishedDownloadItem() | matched reference: ${it.downloadRefId}")
            }
        }
    }

    ///////////////////////////////
    // Coroutine
    ///////////////////////////////
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            Timber.e("Coroutine Exception caught with message: ${throwable.message} (${throwable.javaClass})")

            if (throwable is UnknownHostException
                && (throwable.message?.contains("c11.it-local", true) == true ||
                        throwable.message?.contains("bad gateway", true) == true)
                && downloadList.isNotEmpty()
            ) {
                downloadList[0].apply {
                    this.isComplete = true
                    this.isError = true to "${throwable.message}"
                }
                _downloadListState.value[0].apply {
                    this.isComplete = true
                    this.isError = true to "${throwable.message}"
                }
            }

            errorCount += 1

            if (1 == errorCount) {
                getSpeedTest()
            } else if (2 == errorCount) {
                getIsoFile()
            }
        }


    ///////////////////////////////
    //
    // OVERRIDE
    //
    ///////////////////////////////
    init {

        // Disable log W/System: A resource failed to call release
        try {
            Class.forName("dalvik.system.CloseGuard")
                .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
                .invoke(null, true)
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }
    }

    override fun onCleared() {
        super.onCleared()

        Timber.e("onCleared()")
        updateCanDownload(false)
        updateIsDownloadStarted(false)

        cancelDownloads()
    }

    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    fun getSpeedTest() {
        Timber.d("getSpeedTest()")

        val speedTestUrl = Constants.URL_SPEED_TEST
        val filename = speedTestUrl.getFilenameFromUrl()
        Timber.d("filename: $filename")

        viewModelScope.launch {
            if (isActive && isDownloadStarted) {
                withContext(Dispatchers.Main) {
                    addToDownloadList(filename = filename)
                }

                delay(1_500)
                launchDownload(speedTestUrl) {
                    getIsoFile()
                }
            }
        }
    }

    private fun getIsoFile() {
        Timber.d("getIsoFile()")

        val isoUrl = Constants.URL_ISO_TEST
        val filename = isoUrl.getFilenameFromUrl()
        Timber.d("filename: $filename")

        viewModelScope.launch {
            if (isActive && isDownloadStarted) {
                withContext(Dispatchers.Main) {
                    addToDownloadList(filename = filename)
                }

                delay(1_500)
                launchDownload(isoUrl) {
                    updateIsDownloadStarted(false)
                }
            }
        }
    }

    @SuppressLint("Range")
    fun launchDownload(url: String, filename: String? = null, onFinishedDownload: () -> Unit) {
        Timber.d("launchDownload() | url: $url")

        val fileName = filename ?: url.getFilenameFromUrl()
        Timber.d("fileName: $fileName")

        val file: File = FileManager.getInstance(context).createFileInDownloads(fileName)

        // enqueue puts the download request in the queue.
        val reference: Long = repository.downloadFile(context, url)

        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {

            appendDownloadID(reference)
            updateDownloadRefId(reference)

            var finishDownload = false
            var progress: Int

            while (!finishDownload) {
                val downloadManagerQueryCursor: Cursor = repository
                    .getDownloadManager(context)
                    .query(DownloadManager.Query().setFilterById(reference))

                downloadManagerQueryCursor.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val status =
                            cursor.getInt(downloadManagerQueryCursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                        when (status) {
                            DownloadManager.STATUS_FAILED -> {
                                Timber.e("DownloadManager.STATUS_FAILED")

                                downloadList.find { reference == it.downloadRefId }?.let {
                                    it.isComplete = true
                                    it.isError = true to "Download failed"

                                    Timber.e("item: $this")
                                }
                                _downloadListState.value.find { reference == it.downloadRefId }
                                    ?.let {
                                        it.isComplete = true
                                        it.isError = true to "Download failed"

                                        Timber.e("item: $this")
                                    }

                                finishDownload = true
                            }

                            DownloadManager.STATUS_PAUSED -> {
                                Timber.e("DownloadManager.STATUS_PAUSED")
                            }

                            DownloadManager.STATUS_PENDING -> {
                                Timber.d("DownloadManager.STATUS_PENDING")
                            }

                            DownloadManager.STATUS_RUNNING -> {
                                // Timber.d("DownloadManager.STATUS_RUNNING")
                                val total =
                                    downloadManagerQueryCursor.getLong(
                                        downloadManagerQueryCursor.getColumnIndex(
                                            DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                                        )
                                    )
                                if (total >= 0) {
                                    val downloaded =
                                        downloadManagerQueryCursor.getLong(
                                            downloadManagerQueryCursor.getColumnIndex(
                                                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                                            )
                                        )
                                    progress = (downloaded * 100L / total).toInt()
                                    Timber.d("downloaded: $progress %")

                                    // val progressFloat = (progress / 100).toFloat()

                                    downloadList.find { reference == it.downloadRefId }?.let {
                                        // val index = downloadList.indexOf(it)
                                        // updateProgressAtIndex(index, progress)
                                        withContext(Dispatchers.Main) {
                                            it.progress = progress
                                        }
                                    }
                                        ?: run { Timber.e("download item not found with id: $reference") }

                                    _downloadListState.value.find { reference == it.downloadRefId }
                                        ?.let {
                                            // val index = downloadList.indexOf(it)
                                            // updateProgressAtIndex(index, progress)
                                            withContext(Dispatchers.Main) {
                                                it.progress = progress
                                            }
                                        }
                                        ?: run { Timber.e("download item not found with id: $reference") }

                                    // if you use downloadmanger in async task, here you can use like this to display progress.
                                    // Don't forget to do the division in long to get more digits rather than double.
                                    //  publishProgress((int) ((downloaded * 100L) / total));
                                }
                            }

                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Timber.d("DownloadManager.STATUS_SUCCESSFUL")

                                progress = 100
                                // if you use aysnc task
                                // publishProgress(100);
                                finishDownload = true

                                downloadList.find { reference == it.downloadRefId }?.let {
                                    it.progress = progress
                                    it.isComplete = true

                                    Timber.d("item: $this")
                                }

                                _downloadListState.value.find { reference == it.downloadRefId }
                                    ?.let {
                                        it.progress = progress
                                        it.isComplete = true

                                        Timber.d("item: $this")
                                    }

                                delay(500L)
                                onFinishedDownload()
                            }
                        }
                    }
                }
            }
        }
    }

    fun cancelDownloads() {
        val canceled = repository.cancelDownloads(downloadIds)
        Timber.e("cancelDownloads() | canceled: $canceled")
    }
}
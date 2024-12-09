package com.riders.thelab.feature.musicrecognition.ui.acrcloud

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acrcloud.rec.ACRCloudClient
import com.acrcloud.rec.ACRCloudConfig
import com.acrcloud.rec.ACRCloudResult
import com.acrcloud.rec.IACRCloudListener
import com.acrcloud.rec.utils.ACRCloudLogger
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.common.network.NetworkState
import com.riders.thelab.core.data.IRepository
import com.riders.thelab.core.data.local.model.Song
import com.riders.thelab.core.data.local.model.compose.ACRUiState
import com.riders.thelab.core.data.remote.dto.spotify.SpotifyResponse
import com.riders.thelab.core.data.remote.dto.spotify.SpotifyToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

///////////////////////////////
//
// COMPOSE
//
///////////////////////////////
/*
 * To do that we will write an extension Composable function for ViewModel which
 * will receive Composable lifecycle Owner LocalLifecycleOwner.current.lifecycle
 * and will add observer and remove observer on onDispose block.
 *
 * The ViewModel will implement DefaultLifecycleObserver and will start receiving lifecycle events.
 */
@SuppressLint("ComposableNaming")
@Composable
fun <viewModel : LifecycleObserver> viewModel.observeLifecycleEvents(lifecycle: Lifecycle) {
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(this@observeLifecycleEvents)
        onDispose {
            lifecycle.removeObserver(this@observeLifecycleEvents)
        }
    }
}

@Suppress("EmptyMethod")
@HiltViewModel
class ACRCloudViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel(), DefaultLifecycleObserver, IACRCloudListener {

    // Network
    lateinit var mNetworkState: StateFlow<NetworkState>

    private var mClient: ACRCloudClient? = null
    private var mConfig: ACRCloudConfig? = null

    private var _uiState: MutableStateFlow<ACRUiState> = MutableStateFlow(ACRUiState.Idle)
    val uiState: StateFlow<ACRUiState> = _uiState

    var canLaunchAudioRecognition by mutableStateOf(false)
        private set

    var isRecognizing by mutableStateOf(false)
        private set

    var result: String? by mutableStateOf(null)
        private set

    private var spotifyToken: String? by mutableStateOf(null)
    private var showToastWithMessage: Pair<Boolean, String> by mutableStateOf(Pair(false, ""))

    private fun updateUiState(newState: ACRUiState) {
        this._uiState.value = newState
    }


    private fun updateCanLaunchAudioRecognition(canLaunch: Boolean) {
        this.canLaunchAudioRecognition = canLaunch
    }

    private fun updateIsRecognizing(isRecognizing: Boolean) {
        this.isRecognizing = isRecognizing
    }

    private fun updateResult(newValue: String) {
        this.result = newValue
    }

    private fun updateSpotifyToken(newValue: String) {
        this.spotifyToken = newValue
    }

    fun updateShowToastWithMessage(newValue: Pair<Boolean, String>) {
        this.showToastWithMessage = newValue
    }


    //////////////////////////////////////////
    // Coroutines
    //////////////////////////////////////////
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            Timber.e("coroutineExceptionHandler | ${throwable.message}")
        }

    ///////////////////////////////
    //
    // INITIALIZATION
    //
    ///////////////////////////////
    init {
        Timber.i("init method")
    }


    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    fun observeNetworkState(networkManager: LabNetworkManager) {
        Timber.d("observeNetworkState()")
        mNetworkState = networkManager.networkState


        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            networkManager.getNetworkState().collect { networkState ->
                when (networkState) {
                    is NetworkState.Available -> {
                        Timber.d("network state is Available. All set.")
//                        updateHasInternetConnection(true)

                        if (spotifyToken.isNullOrBlank()) {
                            getSpotifyToken()
                        }
                    }

                    is NetworkState.Losing -> {
                        Timber.w("network state is Losing. Internet connection about to be lost")
//                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Lost -> {
                        Timber.e("network state is Lost. Should not allow network calls initialization")
//                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Unavailable -> {
                        Timber.e("network state is Unavailable. Should not allow network calls initialization")
//                        updateHasInternetConnection(false)
                    }

                    is NetworkState.Undefined -> {
                        Timber.i("network state is Undefined. Do nothing")
                    }
                }
            }
        }
    }


    fun initACRCloud(context: Context) {
        Timber.d("initACRCloud()")
        if (null == mConfig) {
            Timber.d("init ACRCloudConfig")
            mConfig = ACRCloudConfig().apply {
                this.acrcloudListener = this@ACRCloudViewModel
                this.context = context

                // Please create project in "http://console.acrcloud.cn/service/avr".
                // Please create project in "http://console.acrcloud.cn/service/avr".
                this.host = "identify-eu-west-1.acrcloud.com"
                this.accessKey = "2ca3a1020d39133e1130e3b6e0ca40da"
                this.accessSecret = "oCP4NsfdfubfOtkY392029J7Fk6Dvbj2dX7nwuR0"

                this.recorderConfig.rate = 8000
                this.recorderConfig.channels = 1
            }
        }

        if (null == mClient) {
            Timber.d("init ACRCloudClient")
            mClient = ACRCloudClient().apply {
                //if (BuildConfig.DEBUG) {
                ACRCloudLogger.setLog(true)
                //}
                if (!initWithConfig(mConfig)) {
                    Timber.e("initWithConfig() | ACRCloudClient Configuration error. Please verify your information")
                    updateUiState(ACRUiState.Error("ACRCloudClient Configuration error"))
                } else {
                    Timber.d("initWithConfig() | ACRCloudClient Configuration successful.")
                    updateCanLaunchAudioRecognition(true)
                }
            }
        }
    }

    fun startRecognition() {
        mClient?.let {
            if (it.startRecognize()) {
                updateUiState(ACRUiState.ProcessRecognition(running = true))
                updateIsRecognizing(true)
                updateResult("Recognizing...")
            } else {
                updateResult("Init error")
                updateUiState(ACRUiState.Error("Init error"))
            }
        } ?: run {
            updateResult("Client not ready")
            updateCanLaunchAudioRecognition(false)
            updateUiState(ACRUiState.Error("Client not ready"))
        }
    }

    private fun handleResult(acrResult: String) {
        Timber.d("handleResult() | acrResult: $acrResult")

        updateIsRecognizing(false)

        val songFetched = runCatching {
            val json = JSONObject(acrResult)
            val status: JSONObject = json.getJSONObject("status")
            val code = status.getInt("code")
            when (code) {
                0 -> {
                    val metadata: JSONObject = json.getJSONObject("metadata")
                    if (metadata.has("music")) {
                        val musics = metadata.getJSONArray("music")
                        val tt = musics[0] as JSONObject
                        val genres =
                            runCatching { tt.getJSONArray("genres") }.getOrElse { JSONArray() }
                        // val genre = genres[0] as JSONObject
                        val title = tt.getString("title")
                        val label = tt.getString("label")
                        val releaseDate = tt.getString("release_date")
                        val artists = tt.getJSONArray("artists")
                        val art = artists[0] as JSONObject
                        val artist = art.getString("name")
                        val externalMetadata = tt.getJSONObject("external_metadata")
                        val spotify = externalMetadata.getJSONObject("spotify")
                        val track = spotify.getJSONObject("track")
                        val trackName = track.getString("name")
                        val trackID = track.getString("id")
                        val album = spotify.getJSONObject("album")
                        val albumName = album.getString("name")
                        val albumID = album.getString("id")

                        // build song object
                        val song = Song.toModel(
                            genres,
                            title,
                            artists,
                            label,
                            releaseDate,
                            album,
                            externalMetadata
                        )

                        Timber.d("Song created: $song")

                        if (null != song.externalMetadata && null != song.externalMetadata["trackID"]) {
                            getInfoFromSpotify(song, song.externalMetadata["trackID"].toString())
                        } else {
                            Timber.e("trackID key not found. Make sure that the key is correctly typed.")
                            updateUiState(ACRUiState.RecognitionSuccessful(song))
                        }

                        "$title ($artist)"
                    } else {
                        Timber.e("acrResult JSONObject has no metadata")
                        updateUiState(ACRUiState.Error("Error while parsing data"))
                        acrResult
                    }
                }

                1001 -> {
                    Timber.e("code == 1001 | result: $status")
                    updateUiState(ACRUiState.RecognitionError("Else branch | Error while parsing data"))
                    acrResult
                }

                else -> {
                    // TODO: Handle error
                    updateUiState(ACRUiState.Error("Else branch | Error while parsing data"))
                    acrResult
                }
            }
        }
            .onFailure {
                Timber.e("runCatching | onFailure | error caught with message: ${it.message} (class: ${it.javaClass.canonicalName})")
            }
            .onSuccess {
                Timber.d("runCatching | onSuccess | song fetched successfully")
            }
            .getOrElse {
                Timber.e("runCatching | getOrElse | error caught with message: ${it.message}")
                updateUiState(ACRUiState.Error("Error while parsing data, message: it.message"))
                "Error parsing metadata"
            }

        updateResult(songFetched)
    }


    ///////////////////////////////
    //
    // OVERRIDE
    //
    ///////////////////////////////
    override fun onCleared() {
        super.onCleared()
        Timber.e("onCleared()")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.d("onStart()")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Timber.e("onPause()")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Timber.d("onResume()")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.e("onStop()")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Timber.e("onDestroy()")
        mClient?.let {
            it.release()
            mClient = null
        }
    }

    override fun onResult(acrResult: ACRCloudResult?) {
        Timber.d("onResult() | result: $acrResult")
        acrResult?.let {
            Timber.d("acr cloud result received: ${it.result}")
            handleResult(it.result)
        } ?: {
            updateUiState(ACRUiState.RecognitionError("Error message"))
        }
    }

    override fun onVolumeChanged(volume: Double) {
        Timber.e("onVolumeChanged() | volume: $volume")
    }

    private fun getSpotifyToken() {
        Timber.d("getSpotifyToken()")

        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            // val mockToken = "BQD0Raowk2EjeF41DxIA4On2pkY67QH1t63CZseyUT8HMEeC2JwkmfJYOKt0P3z2jEzOMNeXPf2L_IHBt-H4ib-4ZZB0WY271fAeyN9a98cb38BobLY"
            val token = runCatching {
                repository.getToken(
                    // SpotifyRequestToken(
                    clientId = "1714852f79e04b24afd8a49d04068558",
                    clientSecret = "a6cf1fe73aa5486eb2490131e2fe2b45"
                    //)
                )
            }
                .onFailure {
                    it.printStackTrace()
                    Timber.e("runCatching - onFailure() | Error caught: ${it.message}")
                }
                .onSuccess {
                    Timber.d("runCatching - onSuccess() | spotify token fetched successfully")
                }
                .getOrNull()
                ?.createBearerToken()

            token?.let {
                Timber.d("token fetched: $it")
                updateSpotifyToken(it)
            } ?: run {
                Timber.e("Unable to get token")
            }
        }
    }

    private fun getInfoFromSpotify(song: Song, trackID: String) {
        Timber.d("getInfoFromSpotify()")

        viewModelScope.launch(Dispatchers.IO + SupervisorJob() + coroutineExceptionHandler) {
            runCatching {
                if (SpotifyToken.bearerToken.isNotBlank()) {
                    val trackInfo: SpotifyResponse = repository.getTrackInfo(
                        bearerToken = SpotifyToken.bearerToken,
                        trackId = trackID
                    )

                    Timber.d("info: ${trackInfo.album.images[0]}")
                    val albumThumbnail = trackInfo.album.images[0].url

                    song.albumThumbUrl = albumThumbnail
                    Timber.d("song: $song")

                    updateUiState(ACRUiState.RecognitionSuccessful(song))
                }
            }
                .onFailure {
                    it.printStackTrace()
                    Timber.e("runCatching - onFailure() | Error caught: ${it.message}")
                }
                .onSuccess {
                    Timber.d("runCatching - onSuccess() | spotify track info fetched successfully")
                }
                .getOrNull()
        }
    }
}


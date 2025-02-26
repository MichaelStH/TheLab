package com.riders.thelab.ui.mainactivity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.riders.thelab.R
import com.riders.thelab.TheLabApplication
import com.riders.thelab.core.broadcast.LocationBroadcastReceiver
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.common.utils.LabLocationManager
import com.riders.thelab.core.data.local.model.Permission
import com.riders.thelab.core.data.local.model.app.App
import com.riders.thelab.core.data.local.model.app.LocalApp
import com.riders.thelab.core.data.local.model.app.PackageApp
import com.riders.thelab.core.location.GpsUtils
import com.riders.thelab.core.location.OnGpsListener
import com.riders.thelab.core.permissions.PermissionManager
import com.riders.thelab.core.service.TheLabVoiceAssistantService
import com.riders.thelab.core.speechtotext.SpeechRecognizerError
import com.riders.thelab.core.speechtotext.SpeechToTextManager
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.ui.mainactivity.fragment.exit.ExitDialog
import com.riders.thelab.utils.Constants.GPS_REQUEST
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : BaseComponentActivity(), LocationListener, OnGpsListener, RecognitionListener {

    private val mViewModel: MainActivityViewModel by viewModels()

    private var mPermissionManager: PermissionManager? = null

    // Location
    private var mLabLocationManager: LabLocationManager? = null
    private var locationReceiver: LocationBroadcastReceiver? = null
    private lateinit var mGpsUtils: GpsUtils
    private var isGPS: Boolean = false
    private var lastKnowLocation: Location? = null

    // Network
    private var mLabNetworkManager: LabNetworkManager? = null

    // Speech
    private var mSpeechToTextManager: SpeechToTextManager? = null

    /*private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null*/
    private var message: String? = null


    /////////////////////////////////////
    //
    // OVERRIDE
    //
    /////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val w = window
        w.allowEnterTransitionOverlap = true

        // In Activity's onCreate() for instance
        // make fully Android Transparent Status bar
        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                w.statusBarColor = Color.TRANSPARENT
            }

            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            }
        }
        window.navigationBarColor = ContextCompat.getColor(this, R.color.default_dark)

        mPermissionManager = PermissionManager.from(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                setContent {

                    val dynamicIslandUiState by mViewModel.dynamicIslandState.collectAsStateWithLifecycle()

                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainContent(
                                dynamicIslandUiState = dynamicIslandUiState,
                                isDynamicIslandVisible = mViewModel.isDynamicIslandVisible,
                                searchedAppRequest = mViewModel.searchedAppRequest,
                                onSearchAppRequestChanged = mViewModel::updateSearchAppRequest,
                                filteredList = mViewModel.filteredList,
                                whatsNewList = mViewModel.whatsNewAppList,
                                isMicrophoneEnabled = mViewModel.isMicrophoneEnabled,
                                onUpdateMicrophoneEnabled = mViewModel::updateMicrophoneEnabled,
                                isKeyboardVisible = mViewModel.keyboardVisible,
                                onUpdateKeyboardVisible = mViewModel::updateKeyboardVisible,
                                isPagerAutoScroll = mViewModel.isPagerAutoScroll
                            ) { event ->
                                when (event) {
                                    is UiEvent.OnAppItemClicked -> launchApp(event.app)
                                    else -> mViewModel.onEvent(event)
                                }
                            }
                        }
                    }
                }
            }

            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (mViewModel.appList.toList().isEmpty()) {

                    // Retrieve applications
                    mViewModel.retrieveApplications(TheLabApplication.getInstance().getContext())
                    mViewModel.retrieveRecentApps(TheLabApplication.getInstance().getContext())
                }
            }
        }

        checkPermissions()
    }

    override fun onPause() {
        Timber.e("onPause()")

        // Unregister Location receiver
        runCatching {
            locationReceiver?.let {
                // View Models implementation
                // don't forget to remove receiver data source
                //mViewModel.removeDataSource(locationReceiver!!.getLocationStatus())
                unregisterReceiver(locationReceiver)
            }
        }
            .onFailure { it.printStackTrace() }

        // Stop update timer
        if (mViewModel.isPagerAutoScroll) {
            mViewModel.updatePagerAutoScroll(false)
        }

        mLabLocationManager?.stopUsingGPS()

        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume()")

        // Register Lab Location manager
        // registerLabLocationManager()

        mViewModel.updatePagerAutoScroll(true)

        /*val intentFilter = IntentFilter()
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)*/

        // View Models implementation
        // add data source
//        mViewModel.addDataSource(locationReceiver.getLocationStatus())
//        registerReceiver(locationReceiver, intentFilter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GPS_REQUEST) {
            isGPS = true // flag maintain before get location
        }
    }

    override fun backPressed() {
        Timber.e("backPressed()")
        ExitDialog(this)
            .apply { window?.setBackgroundDrawableResource(android.R.color.transparent) }
            .show()
    }

    override fun onDestroy() {
        Timber.d("onDestroy()")
        Timber.d("unregister network callback()")
        try {
            // networkManager.let { mConnectivityManager?.unregisterNetworkCallback(it) }
        } catch (exception: RuntimeException) {
            Timber.e("NetworkCallback was already unregistered")
        }

//        if (speech != null) speech!!.stopListening()
        mSpeechToTextManager?.stopListening()


        super.onDestroy()
    }


    /////////////////////////////////////
    //
    // CLASS METHODS
    //
    /////////////////////////////////////
    private fun checkPermissions() {
        val setOfPermissions: Set<String> = buildSet {
            Permission.Location.permissions.forEach { add(it) }
            Permission.AudioRecord.permissions.forEach { add(it) }
        }
        val arrayOfPermissions: Array<String> = setOfPermissions.toTypedArray()

        val hasPermission = arrayOfPermissions.all {
            val granted = checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            Timber.d("checkPermissions() | permission ${it.toString()} granted ? = $granted")
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }

        Timber.d("checkPermissions() | has all Permissions ?: $hasPermission")

        mPermissionManager?.let {
            it
                .request(Permission.Location, Permission.AudioRecord)
                .rationale("Location is needed to discover some features\nAudio Record is needed to use the vocal assistant. You can enable this feature in the settings screen")
                .checkPermission { granted: Boolean ->

                    if (!granted) {
                        Timber.e("Permissions are denied. User may access to app with limited location related features")

                    } else {

                        // Variables
                        initActivityVariables()

                        // Retrieve applications
                        mViewModel.retrieveApplications(
                            TheLabApplication.getInstance().getContext()
                        )
                        mViewModel.retrieveRecentApps(
                            TheLabApplication.getInstance().getContext()
                        )

                        startVoiceService()
                    }
                }
        }
    }

    private fun initActivityVariables() {
        mViewModel.initNavigator(this@MainActivity)

        mLabNetworkManager = LabNetworkManager
            .getInstance(this@MainActivity, lifecycle)
            .also { mViewModel.observeNetworkState(it) }

        /*locationReceiver = LocationBroadcastReceiver()
        mGpsUtils = GpsUtils(this@MainActivity)

        mLabLocationManager =
            LabLocationManager(
                activity = this@MainActivity,
                locationListener = this@MainActivity
            )*/
    }

    private fun startVoiceService() {
        val serviceIntent = Intent(this, TheLabVoiceAssistantService::class.java).apply {
            action = getString(R.string.voice_assistant_service_action_start_listening)
        }

        if (LabCompatibilityManager.isOreo()) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun registerLocationReceiver() {
        Timber.d("registerLocationReceiver()")

        val intentFilter = IntentFilter()
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)

        if (null != locationReceiver) {
            // View Models implementation
            // add data source
            //mViewModel.addDataSource(locationReceiver!!.getLocationStatus())
            registerReceiver(locationReceiver, intentFilter)
        }
    }


    private fun registerLabLocationManager() {
        Timber.d("registerLabLocationManager()")

        mLabLocationManager?.let {
            if (!it.canGetLocation()) {
                Timber.e("Cannot get location please enable position")

                /*binding.includeToolbarLayout.ivLocationStatus.setBackgroundResource(
                    R.drawable.ic_location_off
                )*/
                // TODO : Should show alert with compose dialog
                // labLocationManager?.showSettingsAlert()
            } else {
                it.setLocationListener()
                it.getCurrentLocation()

                /*binding.includeToolbarLayout.ivLocationStatus.setBackgroundResource(
                    R.drawable.ic_location_on
                )*/
            }
        }
            ?: run { Timber.e("Lab Location Manager is null | Cannot register location callback events") }
    }

    private fun toggleLocation() {
        Timber.e("toggleLocation()")
        if (!isGPS) mGpsUtils.turnGPSOn(this)
    }

    fun launchSpeechToText() {
        // Check permission first

        val manager = checkNotNull(mPermissionManager) {
            "Permission Manager is null. Then cannot launch speech to text."
        }

        manager
            .request(Permission.AudioRecord)
            .rationale("Microphone permission is mandatory in order to use the vocal searching features.")
            .checkPermission { granted: Boolean ->

                if (!granted) {
                    // if the permissions are not accepted we are displaying
                    // a toast message as permissions denied on below line.
                    UIManager.showToast(this@MainActivity, "Permissions Denied..")
                } else {
                    // if all the permissions are granted we are displaying
                    // a simple toast message.
                    UIManager.showToast(this@MainActivity, "Permissions Granted..")

                    initSpeechToText()

                    // Start listening (TTS)
                    Timber.i("launchSpeechToText() | startListeningLegacy() ... ")
//                    speech?.startListeningLegacy(recognizerIntent)
                    mSpeechToTextManager?.startListeningLegacy()
                }
            }
    }

    private fun initSpeechToText() {
        Timber.i("initSpeechToText()")

        mSpeechToTextManager = SpeechToTextManager.Builder(this@MainActivity)
            .setSpeechRecognizerIntent(maxResults = 3)
            .setRecognitionListener(this)
            .build()

        // Init Speech To Text Variables
        /*speech = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(this@MainActivity)
        }

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this@MainActivity.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }*/
    }

    @SuppressLint("InlinedApi")
    private fun toggleWifi() {
        Timber.d("toggleWifi()")
        if (LabCompatibilityManager.isAndroid10()) {
            val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            @Suppress("DEPRECATION")
            startActivityForResult(panelIntent, 0)
        } else {
            // use previous solution, add appropriate permissions to AndroidManifest file (see answers above)
            (this.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)
                ?.apply {
                    // isWifiEnabled = true /*or false*/
                    if (!isWifiEnabled) {
                        Timber.d("(this.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager) $isWifiEnabled")
                        Timber.d("This should activate wifi")

                        @Suppress("DEPRECATION")
                        isWifiEnabled = true

                    } else {
                        Timber.d("(this.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager) $isWifiEnabled")
                        Timber.d("This should disable wifi")

                        @Suppress("DEPRECATION")
                        isWifiEnabled = false

                    }
                    @Suppress("DEPRECATION")
                    this.isWifiEnabled = !isWifiEnabled
                }
        }
    }

    fun launchApp(item: App) {
        Timber.d("launchApp : $item")
        when {
            item is LocalApp && item.title?.lowercase()?.contains("drive") == true -> {
                UIManager.showToast(
                    this@MainActivity,
                    "Please check this functionality later. Problem using Drive REST API v3"
                )
                return
            }

            item is LocalApp && (-1).toByte() != item.id -> {
                mViewModel.launchActivityOrPackage(item)
            }

            item is PackageApp -> {
                mViewModel.launchActivityOrPackage(item)
            }

            else -> {
                Timber.e("Item id == -1 , not app activity. Should launch package intent.")
            }
        }
    }


    /////////////////////////////////////
    //
    // IMPLEMENTS
    //
    /////////////////////////////////////
    override fun gpsStatus(isGPSEnable: Boolean) {
        Timber.d("gpsStatus()")
        Timber.d("turn on/off GPS - isGPSEnable : $isGPSEnable")
        isGPS = isGPSEnable
    }

    override fun onLocationChanged(location: Location) {
        Timber.d("onLocationChanged | location: $location")
    }


    override fun onReadyForSpeech(params: Bundle?) {
        Timber.e("onReadyForSpeech()")
    }

    override fun onBeginningOfSpeech() {
        Timber.i("onBeginningOfSpeech()")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Timber.d("onRmsChanged() : volume $rmsdB")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Timber.d("onBufferReceived() : %s", buffer)
    }

    override fun onEndOfSpeech() {
        Timber.d("onEndOfSpeech()")
    }

    override fun onError(error: Int) {
        message = this@MainActivity.getString(SpeechRecognizerError.getIntErrorAsStringRes(error))
        Timber.e("onError() | Error message caught: $message")

        mViewModel.updateMicrophoneEnabled(false)
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.let { matches ->
                for (element in matches) {
                    Timber.d("onResults() | match element found: $element")
                }

                // Take first result should be the most accurate word
                mViewModel.updateSearchAppRequest(matches[0])
                mViewModel.updateMicrophoneEnabled(false)
            }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Timber.i("onPartialResults()")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Timber.i("onEvent()")
    }
}
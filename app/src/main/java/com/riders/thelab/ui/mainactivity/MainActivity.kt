package com.riders.thelab.ui.mainactivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.riders.thelab.R
import com.riders.thelab.TheLabApplication
import com.riders.thelab.core.broadcast.LocationBroadcastReceiver
import com.riders.thelab.core.common.network.LabNetworkManager
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.common.utils.LabLocationManager
import com.riders.thelab.core.data.local.model.app.App
import com.riders.thelab.core.data.local.model.app.LocalApp
import com.riders.thelab.core.data.local.model.app.PackageApp
import com.riders.thelab.core.location.GpsUtils
import com.riders.thelab.core.location.OnGpsListener
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.compose.theme.TheLabTheme
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.ui.mainactivity.fragment.exit.ExitDialog
import com.riders.thelab.utils.Constants.GPS_REQUEST
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale


class MainActivity : BaseComponentActivity(), LocationListener, OnGpsListener, RecognitionListener {

    private val mViewModel: MainActivityViewModel by viewModels()

    // Location
    private var mLabLocationManager: LabLocationManager? = null
    private var locationReceiver: LocationBroadcastReceiver? = null
    private lateinit var mGpsUtils: GpsUtils
    private var isGPS: Boolean = false
    private var lastKnowLocation: Location? = null

    // Network
    private var mLabNetworkManager: LabNetworkManager? = null

    // Speech
    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
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

        // Variables
        initActivityVariables()

        // Retrieve applications
        mViewModel.retrieveApplications(TheLabApplication.getInstance().getContext())
        mViewModel.retrieveRecentApps(TheLabApplication.getInstance().getContext())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                setContent {
                    TheLabTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainContent(viewModel = mViewModel)
                        }
                    }
                }
            }
        }
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
        registerLabLocationManager()

        mViewModel.updatePagerAutoScroll(true)

        val intentFilter = IntentFilter()
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)

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

        if (speech != null) speech!!.stopListening()

        super.onDestroy()
    }


    /////////////////////////////////////
    //
    // CLASS METHODS
    //
    /////////////////////////////////////
    private fun initActivityVariables() {
        Timber.d("initActivityVariables()")

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
        Dexter
            .withContext(this@MainActivity)
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(grantedResponse: PermissionGrantedResponse?) {
                    // if all the permissions are granted we are displaying
                    // a simple toast message.
                    UIManager.showToast(this@MainActivity, "Permissions Granted..")

                    initSpeechToText()

                    // Start listening (TTS)
                    Timber.i("startListening() ... ")
                    speech?.startListening(recognizerIntent)
                }

                override fun onPermissionDenied(permissionDenied: PermissionDeniedResponse?) {
                    // if the permissions are not accepted we are displaying
                    // a toast message as permissions denied on below line.
                    UIManager.showToast(this@MainActivity, "Permissions Denied..")
                }

                // on below line we are calling on permission
                // rational should be shown method.
                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    // in this method we are calling continue
                    // permission request until permissions are not granted.
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {

                // on below line method will be called when dexter
                // throws any error while requesting permissions.
                UIManager.showToast(this@MainActivity, it.name)
            }
            .check()
    }

    // Init Speech To Text Variables
    private fun initSpeechToText() {
        Timber.i("initSpeechToText()")

        speech = SpeechRecognizer.createSpeechRecognizer(this).apply {
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
        }
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
                UIManager.showActionInToast(
                    this@MainActivity,
                    "Please check this functionality later. Problem using Drive REST API v3"
                )
                return
            }

            item is LocalApp && -1L != item.id -> {
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
        Timber.e("FAILED %s", error)

        message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> getString(R.string.error_audio_error)
            SpeechRecognizer.ERROR_CLIENT -> getString(R.string.error_client)
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> getString(R.string.error_permission)
            SpeechRecognizer.ERROR_NETWORK -> getString(R.string.error_network)
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> getString(
                R.string.error_timeout
            )

            SpeechRecognizer.ERROR_NO_MATCH -> getString(R.string.error_no_match)
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> getString(R.string.error_busy)
            SpeechRecognizer.ERROR_SERVER -> getString(R.string.error_server)
            else -> getString(R.string.error_understand)
        }

        Timber.e("Error message caught: $message")

        mViewModel.updateMicrophoneEnabled(false)
    }

    override fun onResults(results: Bundle?) {
        Timber.e("onResults()")

        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.let { matches ->
                for (element in matches) {
                    Timber.d("match element found: $element")
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
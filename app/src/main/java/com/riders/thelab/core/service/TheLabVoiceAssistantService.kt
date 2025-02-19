package com.riders.thelab.core.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.riders.thelab.R
import com.riders.thelab.core.common.utils.LabNotificationManager
import com.riders.thelab.navigator.Navigator
import com.riders.thelab.ui.mainactivity.MainActivity
import timber.log.Timber

class TheLabVoiceAssistantService : Service(), RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognizerIntent: Intent? = null
    private var isListening = false

    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_AUDIO -> {
                    Timber.e("recognitionListener.onError() | Speech recognition error: ERROR_AUDIO")
                }

                SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> {
                    Timber.e("recognitionListener.onError() | Speech recognition error: ERROR_LANGUAGE_NOT_SUPPORTED")
                }

                SpeechRecognizer.ERROR_NO_MATCH -> {
                    Timber.e("recognitionListener.onError() | Speech recognition error: ERROR_NO_MATCH")
                }

                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    Timber.e("recognitionListener.onError() | Speech recognition error: ERROR_INSUFFICIENT_PERMISSIONS")
                }

                else -> {
                    Timber.e("recognitionListener.onError() | Speech recognition error: $error")
                }
            }

            isListening = false

            startListening() // Restart listening on error
        }

        override fun onResults(results: Bundle?) {
            val spokenWords = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            spokenWords?.forEach { word ->
                if (word.contains(getString(R.string.voice_assistant_service_trigger_phrase), ignoreCase = true)) {
                    Navigator.callVoiceAssistantActivity(this@TheLabVoiceAssistantService)
                    stopListening() // Stop listening after launching activity
                }
                Timber.i("recognitionListener.onResults() | Detected words: $word")
            }
            startListening() // Continue listening after processing results
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partialResults =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Timber.i("recognitionListener.onPartialResults() | partial results = ${partialResults?.joinToString(",")}")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }


    ///////////////////////////////
    //
    // OVERRIDE
    //
    ///////////////////////////////
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate()")

        createNotificationChannel()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@TheLabVoiceAssistantService)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        speechRecognizer?.setRecognitionListener(recognitionListener)
    }

    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand() | action : ${intent?.action.toString()}")

        when (intent?.action) {
            getString(R.string.voice_assistant_service_action_start_listening) -> {
                startForeground(
                    getString(R.string.voice_assistant_service_notification_channel_id).toInt(),
                    LabNotificationManager.createNotification(
                        context = baseContext,
                        notificationIntent = Intent(
                            this@TheLabVoiceAssistantService,
                            MainActivity::class.java
                        ),
                        stopIntent = Intent(
                            this@TheLabVoiceAssistantService,
                            TheLabVoiceAssistantService::class.java
                        ).apply {
                            action =
                                getString(R.string.voice_assistant_service_action_stop_listening)
                        },
                        notificationChannelId = getString(R.string.voice_assistant_service_notification_channel_id),
                        title = getString(R.string.voice_assistant_service_notification_title),
                        contentText = "Listening for '${getString(R.string.voice_assistant_service_trigger_phrase)}'",
                        smallIcon = R.mipmap.ic_lab_round
                    )
                )
                startListening()
            }

            getString(R.string.voice_assistant_service_action_stop_listening) -> {
                stopListening()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }

    ///////////////////////////////
    //
    // CLASS METHODS
    //
    ///////////////////////////////
    private fun createNotificationChannel() {
        LabNotificationManager.createNotificationChannel(
            context = baseContext,
            notificationChannelId = R.string.voice_assistant_service_notification_channel_id,
            notificationName = R.string.voice_assistant_service_notification_channel_name,
            notificationDescription = R.string.voice_assistant_service_notification_channel_description,
            notificationImportance = NotificationManager.IMPORTANCE_DEFAULT
        )
    }

    private fun startListening() {
        Timber.i("startListening()")
        if (!isListening /*&& SpeechRecognizer.isRecognitionAvailable(this)*/) {
            speechRecognizer?.startListening(speechRecognizerIntent)
            isListening = true
            Timber.v("Start listening")
        }
    }

    private fun stopListening() {
        Timber.e("stopListening()")
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            Timber.e("Stop listening")
        }
    }

    ///////////////////////////////
    //
    // IMPLEMENTS
    //
    ///////////////////////////////
    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        when (error) {
            SpeechRecognizer.ERROR_AUDIO -> {
                Timber.e("onError() | Speech recognition error: ERROR_AUDIO")
            }

            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> {
                Timber.e("onError() | Speech recognition error: ERROR_LANGUAGE_NOT_SUPPORTED")
            }

            SpeechRecognizer.ERROR_NO_MATCH -> {
                Timber.e("onError() | Speech recognition error: ERROR_NO_MATCH")
            }

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                Timber.e("onError() | Speech recognition error: ERROR_INSUFFICIENT_PERMISSIONS")
            }

            else -> {
                Timber.e("onError() | Speech recognition error: $error")
            }
        }

        startListening() // Restart listening on error
    }

    override fun onResults(results: Bundle?) {
        val spokenWords = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        spokenWords?.forEach { word ->
            if (word.contains(getString(R.string.voice_assistant_service_trigger_phrase), ignoreCase = true)) {
                Navigator.callVoiceAssistantActivity(this)
                stopListening() // Stop listening after launching activity
            }
            Timber.i("onResults() | Detected words: $word")
        }
        startListening() // Continue listening after processing results
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
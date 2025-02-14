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
    private val triggerPhrase = "hi siri"
    private var isListening = false


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

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        speechRecognizer?.setRecognitionListener(this)
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
                        notificationIntent = Intent(this, MainActivity::class.java),
                        stopIntent = Intent(this, TheLabVoiceAssistantService::class.java).apply {
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
        if (!isListening && SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer?.startListening(speechRecognizerIntent)
            isListening = true
            Timber.d("Start listening")
        }
    }

    private fun stopListening() {
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
        Timber.e("onError() | Speech recognition error: $error")
        startListening() // Restart listening on error
    }

    override fun onResults(results: Bundle?) {
        val spokenWords = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        spokenWords?.forEach { word ->
            if (word.contains(triggerPhrase, ignoreCase = true)) {
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
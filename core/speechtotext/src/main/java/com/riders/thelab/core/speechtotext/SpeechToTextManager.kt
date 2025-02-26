package com.riders.thelab.core.speechtotext

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.Executors

/*
 * Source https://medium.com/@andraz.pajtler/android-speech-to-text-the-missing-guide-part-1-824e2636c45a
 */
class SpeechToTextManager internal constructor(builder: Builder) {

    val mContext: Context = builder.context
    val mSpeechRecognizer: SpeechRecognizer = builder.speechRecognizer
    val mSpeechRecognizerIntent: Intent = builder.speechRecognizerIntent
    val mRecognitionListener: RecognitionListener = builder.recognitionListener
    protected var isListening: Boolean = false
        private set

    fun getAllSupportedLanguages() {
        val intent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)

        mContext.sendOrderedBroadcast(intent, null, object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (resultCode == Activity.RESULT_OK) {
                    val results = getResultExtras(true)

                    // Supported languages
                    val prefLang:String? = results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)
                    val allLanguages:  java. util. ArrayList<CharSequence?>? = results.getCharSequenceArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)
                }
            }
        },
            null,
            Activity.RESULT_OK,
            null,
            null
        )
    }

    @SuppressLint("NewApi")
    fun startListening() {
        Timber.i("startListening()")

        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Timber.e("Permission not granted. Please make sure that the access to the microphone is granted")
            return
        }

        // Get SpeechRecognizer instance
        if (!SpeechRecognizer.isRecognitionAvailable(mContext)) {
            // Speech recognition service NOT available
            Timber.e("Speech recognition service NOT available")
            return
        }

        if(LabCompatibilityManager.isTiramisu()) {
            mSpeechRecognizer.checkRecognitionSupport(
                mSpeechRecognizerIntent,
                Executors.newSingleThreadExecutor(),
                object : RecognitionSupportCallback {
                    override fun onSupportResult(recognitionSupport: RecognitionSupport) {

                        if (!isListening) {
                            mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
                            isListening = true
                            Timber.v("LabCompatibilityManager.isTiramisu() | Start listening")
                        }
                    }

                    override fun onError(error: Int) {
                        // ...
                        Timber.e("LabCompatibilityManager.isTiramisu() | startListening() | onError() | error: $error")
                    }
                })
        } else {
            if (!isListening) {
                mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
                isListening = true
                Timber.v("!LabCompatibilityManager.isTiramisu() | Start listening")
            }
        }
    }

    fun stopListening() {
        Timber.e("stopListening()")
        if (isListening) {
            mSpeechRecognizer?.stopListening()
            isListening = false
            Timber.e("Stop listening")
        }
    }

    companion object {
        @Volatile
        @JvmStatic
        private var mInstance: SpeechToTextManager? = null

        /*fun getInstance(context: Context) = mInstance ?: synchronized(this) {
            mInstance ?: TextToSpeechManager(context)
        }*/
    }

    class Builder(internal val context: Context) {
        internal var speechRecognizer: SpeechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(context)
        internal lateinit var speechRecognizerIntent: Intent
        internal lateinit var recognitionListener: RecognitionListener

        fun setSpeechRecognizerIntent(maxResults: Int = 5, language:String = "en-US"): SpeechToTextManager.Builder {
            this.speechRecognizerIntent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(
                        RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        context.packageName
                    )
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults)
                }

            return this
        }

        fun setRecognitionListener(recognitionListener: RecognitionListener): SpeechToTextManager.Builder {
            this.recognitionListener = recognitionListener
            return this
        }

        fun build(): SpeechToTextManager {
            return SpeechToTextManager(this)
        }
    }
}
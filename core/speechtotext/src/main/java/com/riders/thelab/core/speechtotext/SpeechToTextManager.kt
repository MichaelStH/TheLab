package com.riders.thelab.core.speechtotext

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.util.Locale

class SpeechToTextManager internal constructor(builder: Builder) {

    val mContext: Context = builder.context
    val mSpeechRecognizer: SpeechRecognizer = builder.speechRecognizer
    val mSpeechRecognizerIntent: Intent = builder.speechRecognizerIntent
    val mRecognitionListener: RecognitionListener = builder.recognitionListener
    protected var isListening: Boolean = false
        private set

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

        if (!isListening /*&& SpeechRecognizer.isRecognitionAvailable(this)*/) {
            mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
            isListening = true
            Timber.v("Start listening")
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
        internal var speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        internal lateinit var speechRecognizerIntent: Intent
        internal lateinit var recognitionListener: RecognitionListener

        fun setSpeechRecognizerIntent(maxResults: Int = 5): SpeechToTextManager.Builder {
            this.speechRecognizerIntent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
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
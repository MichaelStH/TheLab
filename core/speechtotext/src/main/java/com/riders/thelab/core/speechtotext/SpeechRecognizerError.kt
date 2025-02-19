package com.riders.thelab.core.speechtotext

import android.speech.SpeechRecognizer
import androidx.annotation.StringRes

enum class SpeechRecognizerError(val intSpeechToTextError: Int, @StringRes val resIdError: Int) {
    ERROR_AUDIO(SpeechRecognizer.ERROR_AUDIO, R.string.error_audio_error),
    ERROR_CLIENT(SpeechRecognizer.ERROR_CLIENT, R.string.error_client),
    ERROR_INSUFFICIENT_PERMISSIONS(
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
        R.string.error_permission
    ),
    ERROR_NETWORK(SpeechRecognizer.ERROR_NETWORK, R.string.error_network),
    ERROR_NETWORK_TIMEOUT(SpeechRecognizer.ERROR_NETWORK_TIMEOUT, R.string.error_timeout),
    ERROR_SPEECH_TIMEOUT(SpeechRecognizer.ERROR_SPEECH_TIMEOUT, R.string.error_timeout),
    ERROR_NO_MATCH(SpeechRecognizer.ERROR_NO_MATCH, R.string.error_no_match),
    ERROR_RECOGNIZER_BUSY(SpeechRecognizer.ERROR_RECOGNIZER_BUSY, R.string.error_busy),
    ERROR_SERVER(SpeechRecognizer.ERROR_SERVER, R.string.error_server),
    UNKNOWN(-1, R.string.error_understand);


    companion object {
        fun getIntErrorAsName(error: Int): String =
            entries.firstOrNull { it.intSpeechToTextError == error }?.name ?: UNKNOWN.name

        fun getIntErrorAsStringRes(error: Int): Int =
            entries.firstOrNull { it.intSpeechToTextError == error }?.resIdError
                ?: UNKNOWN.resIdError
    }
}
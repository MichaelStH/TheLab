package com.riders.thelab.ui.vocalassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jtransforms.fft.DoubleFFT_1D
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class AudioProcessor(private val onFrequenciesUpdated: (List<Double>) -> Unit) {

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize =
        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2
    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private val fft = DoubleFFT_1D(bufferSize.toLong())

    private var recordingJob: Job? = null

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording.get()) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Timber.e("AudioRecord initialization failed")
            return
        }

        isRecording.set(true)
        audioRecord?.startRecording()

        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            val audioData = ShortArray(bufferSize)
            val fftData = DoubleArray(bufferSize)

            try {
                while (isRecording.get()) {
                    val readSize = audioRecord?.read(audioData, 0, bufferSize) ?: 0
                    if (readSize > 0) {
                        for (i in 0 until readSize) {
                            fftData[i] = audioData[i].toDouble()
                        }
                        fft.realForward(fftData)

                        val frequencies = calculateFrequencies(fftData)

                        withContext(Dispatchers.Main) {
                            onFrequenciesUpdated(frequencies)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error during audio processing: ${e.message}")
            } finally {
                stopRecording()
            }
        }
    }

    fun stopRecording() {
        isRecording.set(false)
        recordingJob?.cancel()
        recordingJob = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun calculateFrequencies(fftData: DoubleArray): List<Double> {
        val frequencies = mutableListOf<Double>()
        val halfSize = fftData.size / 2

        for (i in 1 until halfSize) {
            val real = fftData[2 * i]
            val imaginary = fftData[2 * i + 1]
            val magnitude = kotlin.math.sqrt(real * real + imaginary * imaginary)
            frequencies.add(magnitude)
        }

        return frequencies
    }
}
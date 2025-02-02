package com.riders.thelab.ui.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.riders.thelab.R
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.databinding.ActivityCameraBinding
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class CameraActivity : AppCompatActivity() {

    private var _viewBinding: ActivityCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _viewBinding!!

    private var camera: Camera? = null
    private var hasFlashLight: Boolean = false

    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var flashStateClicked: Int = 0

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == UNKNOWN_ORIENTATION) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalyzer?.targetRotation = rotation
                imageCapture?.targetRotation = rotation
            }
        }
    }

    // Using a DisplayListener allows you to update the target rotation
    // of the camera use cases in certain situations, for instance
    // when the system doesn't destroy and recreate the Activity
    // after the device rotates by 180 degrees.
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayChanged(displayId: Int) {
            if (binding.root.display.displayId == displayId) {
                val rotation = binding.root.display.rotation
                imageAnalyzer?.targetRotation = rotation
                imageCapture?.targetRotation = rotation
            }
        }

        override fun onDisplayAdded(displayId: Int) {
            // Ignored
        }

        override fun onDisplayRemoved(displayId: Int) {
            // Ignored
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if device has camera flash light capability
        // val hasFlashLight = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        //binding.cameraCaptureButton.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        initViewsModelsObservers()
    }


    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    override fun onPause() {
        camera?.let { CameraUtils.turnOffCameraFlash(it) }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume()")
    }

    override fun onStop() {
        super.onStop()

        orientationEventListener.disable()

        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.unregisterDisplayListener(displayListener)

        camera?.let { CameraUtils.turnOffCameraFlash(it) }
    }


    override fun onDestroy() {
        Timber.e("onDestroy()")
        cameraExecutor.shutdown()
        _viewBinding = null

        super.onDestroy()
    }

    fun onSwitchFlashStateClicked(view: View) {
        if (3 == flashStateClicked) {
            // reset
            flashStateClicked = 0
        }

        when (flashStateClicked) {
            FLASH_STATE_OFF -> {
                binding.btnFlashState.setImageDrawable(
                    UIManager.getDrawable(this@CameraActivity, R.drawable.ic_flash_off)
                )
                CameraUtils.turnOffCameraFlash(camera!!)
            }

            FLASH_STATE_ON -> {
                binding.btnFlashState.setImageDrawable(
                    UIManager.getDrawable(this@CameraActivity, R.drawable.ic_flash_on)
                )
                CameraUtils.turnOnCameraFlash(camera!!)
            }

            FLASH_STATE_AUTO -> {
                binding.btnFlashState.setImageDrawable(
                    UIManager.getDrawable(this@CameraActivity, R.drawable.ic_flash_auto)
                )
            }
        }

        flashStateClicked++
    }


    fun takePhoto(view: View) {
        Timber.e("takePhoto()")
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create a file to hold the image. Add in a time stamp so the file name will be unique.
        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file plus metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Timber.e("Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Timber.d(msg)

                    val intent =
                        Intent(this@CameraActivity, CameraPictureDetailActivity::class.java)
                    intent.putExtra(
                        CameraPictureDetailActivity.EXTRA_IMAGE_PATH,
                        photoFile.absolutePath
                    )
                    startActivity(intent)
                }
            })
    }


    private fun startCamera() {
        Timber.e("startCamera()")

        // This is used to bind the lifecycle of cameras to the lifecycle owner.
        // This eliminates the task of opening and closing the camera since CameraX is lifecycle-aware.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@CameraActivity)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview : initialize your Preview object, call build on it,
            // get a surface provider from viewfinder, and then set it on the preview.
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.viewFinder.surfaceProvider
                }

            imageCapture = ImageCapture.Builder().build()

            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        // Timber.d("Average luminosity: $luma")
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera =
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalyzer
                    )

                camera?.let { hasFlashLight = it.cameraInfo.hasFlashUnit() }

                binding.btnFlashState.visibility = if (!hasFlashLight) View.GONE else View.VISIBLE
            } catch (exc: Exception) {
                Timber.e("Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        Timber.e("getOutputDirectory()")
        /*val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }*/

        val mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }


    private fun initViewsModelsObservers() {
        Timber.e("initViewsModelsObservers()")
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val UNKNOWN_ORIENTATION = -1

        private const val FLASH_STATE_OFF = 0
        private const val FLASH_STATE_ON = 1
        private const val FLASH_STATE_AUTO = 2
    }
}
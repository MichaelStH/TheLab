package com.riders.thelab.feature.mlkit.ui.xml

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.common.base.Objects
import com.riders.thelab.core.common.utils.LabCompatibilityManager
import com.riders.thelab.core.data.local.model.Permission
import com.riders.thelab.core.permissions.PermissionManager
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.core.ui.utils.UIManager
import com.riders.thelab.feature.mlkit.R
import com.riders.thelab.feature.mlkit.bean.WorkflowState
import com.riders.thelab.feature.mlkit.databinding.ActivityLiveBarcodeBinding
import com.riders.thelab.feature.mlkit.ui.xml.barcodedetection.BarcodeField
import com.riders.thelab.feature.mlkit.ui.xml.barcodedetection.BarcodeProcessor
import com.riders.thelab.feature.mlkit.ui.xml.barcodedetection.BarcodeResultFragment
import com.riders.thelab.feature.mlkit.ui.xml.camera.CameraSource
import com.riders.thelab.feature.mlkit.ui.xml.camera.CameraSourcePreview
import com.riders.thelab.feature.mlkit.ui.xml.camera.GraphicOverlay
import com.riders.thelab.feature.mlkit.ui.xml.settings.SettingsActivity
import com.riders.thelab.feature.mlkit.viewmodel.WorkflowModel
import timber.log.Timber
import java.io.IOException

/** Demonstrates the barcode scanning workflow using camera preview.  */
class LiveBarcodeScanningActivity : BaseAppCompatActivity(), OnClickListener {

    private var _viewBinding: ActivityLiveBarcodeBinding? = null
    private val binding get() = _viewBinding!!

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var settingsButton: View? = null
    private var flashButton: View? = null
    private var promptChip: Chip? = null
    private var promptChipAnimator: AnimatorSet? = null
    private val workflowModel: WorkflowModel by viewModels()
    private var currentWorkflowState: WorkflowState? = null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewBinding = ActivityLiveBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PermissionManager
            .from(this@LiveBarcodeScanningActivity)
            .request(
                Permission.Camera,
                if (!LabCompatibilityManager.isTiramisu()) Permission.Storage else Permission.MediaLocationAndroid13
            )
            .rationale("Camera and Media Storage permissions are mandatory to discover some features")
            .checkPermission { granted: Boolean ->

                if (!granted) {
                    Timber.e("Permissions are denied. User may access to app with limited location related features")
                    UIManager.showToast(
                        this,
                        "Permissions are denied. User may access to app with limited location related features"
                    )
                } else {
                    initViews()
                    setUpWorkflowModel()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        workflowModel.markCameraFrozen()
        settingsButton?.isEnabled = true
        currentWorkflowState = WorkflowState.NOT_STARTED
        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel))
        workflowModel.setWorkflowState(WorkflowState.DETECTING)
    }

    override fun onPostResume() {
        super.onPostResume()
        BarcodeResultFragment.dismiss(supportFragmentManager)
    }

    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    override fun backPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
        cameraSource = null

        _viewBinding = null

    }

    private fun initViews() {
        Timber.d("initViews()")

        preview = binding.cameraPreview
        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
            setOnClickListener(this@LiveBarcodeScanningActivity)
            cameraSource = CameraSource(this)
        }

        promptChip = findViewById(R.id.bottom_prompt_chip)
        promptChipAnimator =
            (AnimatorInflater.loadAnimator(
                this,
                R.animator.bottom_prompt_chip_enter
            ) as AnimatorSet).apply {
                setTarget(promptChip)
            }

        findViewById<View>(R.id.close_button).setOnClickListener(this)
        flashButton = findViewById<View>(R.id.flash_button).apply {
            setOnClickListener(this@LiveBarcodeScanningActivity)
        }
        settingsButton = findViewById<View>(R.id.settings_button).apply {
            setOnClickListener(this@LiveBarcodeScanningActivity)
        }
    }

    private fun startCameraPreview() {
        Timber.d("startCameraPreview()")
        val workflowModel = this.workflowModel
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                preview?.start(cameraSource)
            } catch (e: IOException) {
                Timber.e(e, "Failed to start camera preview!")
                cameraSource.release()
                this.cameraSource = null
            }
        }
    }

    private fun stopCameraPreview() {
        Timber.e("stopCameraPreview()")
        val workflowModel = this.workflowModel
        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            flashButton?.isSelected = false
            preview?.stop()
        }
    }

    private fun setUpWorkflowModel() {
        Timber.d("setUpWorkflowModel()")

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel.workflowState.observe(this, Observer { workflowState ->
            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                return@Observer
            }

            currentWorkflowState = workflowState
            Timber.d("Current workflow state: " + currentWorkflowState!!.name)

            val wasPromptChipGone = promptChip?.visibility == View.GONE

            when (workflowState) {
                WorkflowState.DETECTING -> {
                    promptChip?.visibility = View.VISIBLE
                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
                    startCameraPreview()
                }

                WorkflowState.CONFIRMING -> {
                    promptChip?.visibility = View.VISIBLE
                    promptChip?.setText(R.string.prompt_move_camera_closer)
                    startCameraPreview()
                }

                WorkflowState.SEARCHING -> {
                    promptChip?.visibility = View.VISIBLE
                    promptChip?.setText(R.string.prompt_searching)
                    stopCameraPreview()
                }

                WorkflowState.DETECTED, WorkflowState.SEARCHED -> {
                    promptChip?.visibility = View.GONE
                    stopCameraPreview()
                }

                else -> promptChip?.visibility = View.GONE
            }

            val shouldPlayPromptChipEnteringAnimation =
                wasPromptChipGone && promptChip?.visibility == View.VISIBLE
            promptChipAnimator?.let {
                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
            }
        })

        workflowModel.detectedBarcode.observe(this) { barcode ->
            if (barcode != null) {
                val barcodeFieldList = ArrayList<BarcodeField>()
                barcodeFieldList.add(
                    BarcodeField(
                        "Raw Value",
                        barcode.rawValue ?: ""
                    )
                )
                BarcodeResultFragment.show(supportFragmentManager, barcodeFieldList)
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.close_button -> backPressed()
            R.id.flash_button -> {
                flashButton?.let {
                    if (it.isSelected) {
                        it.isSelected = false
                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                    } else {
                        it.isSelected = true
                        cameraSource!!.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                    }
                }
            }

            R.id.settings_button -> {
                settingsButton?.isEnabled = false
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }
}

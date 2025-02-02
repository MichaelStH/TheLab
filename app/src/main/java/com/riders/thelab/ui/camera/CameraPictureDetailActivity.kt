package com.riders.thelab.ui.camera

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.riders.thelab.core.ui.compose.base.BaseAppCompatActivity
import com.riders.thelab.databinding.ActivityCameraPictureDetailBinding
import timber.log.Timber
import java.io.File

class CameraPictureDetailActivity : BaseAppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
    }

    private var _viewBinding: ActivityCameraPictureDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _viewBinding!!

    private var bundleImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewBinding = ActivityCameraPictureDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras

        if (null == extras) {
            Timber.e("null extras")
            return
        }

        bundleImagePath = extras.getString(EXTRA_IMAGE_PATH)

        /*val myBitmap = BitmapFactory.decodeFile(bundleImagePath)
        binding.image.setImageBitmap(myBitmap)*/

        Glide.with(this).load(bundleImagePath).into(binding.image)
    }

    override fun backPressed() {
        finish()
    }

    override fun onDestroy() {
        Timber.e("onDestroy()")
        _viewBinding = null

        super.onDestroy()
    }

    fun savePicture(view: View) {
        Timber.d("savePicture()")
        finish()
    }

    fun cancelPictureSaving(view: View) {
        Timber.e("cancelPictureSaving()")

        bundleImagePath?.let {

            try {
                val fileToDelete = File(it)
                fileToDelete.delete()

                if (fileToDelete.exists()) {
                    fileToDelete.canonicalFile.delete()

                    if (fileToDelete.exists()) {
                        applicationContext.deleteFile(fileToDelete.name)
                    }
                }

                Timber.e("Picture deleted")
            } catch (exception: Exception) {
                exception.printStackTrace()
            } finally {
                backPressed()
            }
        }

    }
}
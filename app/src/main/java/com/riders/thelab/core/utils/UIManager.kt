package com.riders.thelab.core.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.riders.thelab.R
import com.riders.thelab.core.views.toast.TheLabToast
import com.riders.thelab.core.views.toast.ToastTypeEnum
import com.riders.thelab.data.local.bean.SnackBarType
import jp.wasabeef.glide.transformations.BlurTransformation
import timber.log.Timber

class UIManager private constructor() {
    companion object {

        fun showView(view: View) {
            view.visibility = View.VISIBLE
        }

        fun hideView(view: View) {
            view.visibility = View.GONE
        }


        /**
         * Hide the keyboard
         *
         * @param view
         */
        fun hideKeyboard(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }


        /**
         * Show an alertDialog
         *
         * @param
         * @param title
         * @param message
         * @param negativeMessage
         * @param positiveMessage
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun showAlertDialog(activity: Activity?,
                            context: Context?,
                            title: String?,
                            message: String?,
                            negativeMessage: String,
                            positiveMessage: String?) {
            Timber.i("Show alert dialog")
            val alertDialog = AlertDialog.Builder(context)

            // Setting Dialog Title
            alertDialog.setTitle(title)

            // Setting Dialog Message
            alertDialog.setMessage(message)
            alertDialog.setNegativeButton(negativeMessage) { dialog: DialogInterface, which: Int ->
                showActionInToast(context, negativeMessage)
                if (negativeMessage.equals("Réessayer", ignoreCase = true)) {
                    //launchActivity(context, MainActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NEW_TASK, null, null);
                }
                if (negativeMessage.equals("Réessayer", ignoreCase = true)
                        && LabNetworkManager.isConnected(context)) {
                    dialog.dismiss()
                }
            }
            alertDialog.setPositiveButton(positiveMessage) { dialog: DialogInterface?, which: Int ->
                showActionInToast(context, positiveMessage)
                activity?.onBackPressed()
                if (negativeMessage.equals("Quitter", ignoreCase = true)) {
                    activity!!.finish()
                }
            }
            alertDialog.setCancelable(false)

            // Showing Alert Message
            alertDialog.show()
        }


        fun showActionInToast(context: Context?, textToShow: String?) {
            Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show()
        }

        fun showCustomToast(context: Context?, type: ToastTypeEnum?, message: String?) {
            TheLabToast.Builder(context)
                    .setType(type)
                    .setText(message)
                    .show()
        }

        fun showActionInSnackBar(context: Activity, view: View?,
                                 message: String?, type: SnackBarType,
                                 actionText: String?,
                                 listener: View.OnClickListener?) {
            // create instance
            val snackBar = Snackbar.make(
                    context.findViewById(R.id.content),
                    message!!,
                    if (listener != null) BaseTransientBottomBar.LENGTH_INDEFINITE else BaseTransientBottomBar.LENGTH_LONG)
            snackBar.setBackgroundTint(ContextCompat.getColor(context, type.backgroundColor))

            // get snackBar view
            val snackBarView = snackBar.view

            // change snackBar text color
            val snackBarTextId = com.google.android.material.R.id.snackbar_text
            val textView = snackBarView.findViewById<View>(snackBarTextId) as TextView
            // set action text color
            textView.setTextColor(ContextCompat.getColor(context, type.textColor))
            if (null != actionText && null != listener) {

                // change snackBar button text color
                val snackBarButtonId = com.google.android.material.R.id.snackbar_action
                val buttonView = snackBarView.findViewById<View>(snackBarButtonId) as Button
                // set action text color
                buttonView.setBackgroundColor(ContextCompat.getColor(context, type.textColor))
                snackBar.setAction(actionText, listener)
            }
            snackBar.show()
        }


        // Showing the status in Snackbar
        fun showConnectionStatusInSnackBar(context: Activity, isConnected: Boolean) {
            Timber.d("showConnectionStatusInSnackBar()")
            val snackbar = Snackbar.make(
                    context.findViewById(R.id.content),
                    context.getString(
                            if (!isConnected) R.string.network_status_disconnected else R.string.network_status_connected),
                    BaseTransientBottomBar.LENGTH_LONG)
            snackbar.setBackgroundTint(
                    ContextCompat.getColor(
                            context,
                            if (!isConnected) R.color.locationColorPrimaryDark else R.color.contactsDatabaseColorPrimaryDark))
            val sbView = snackbar.view
            val textView = sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(Color.WHITE)
            snackbar.show()
        }

        /**
         * Returns the background color of a view (mostly requested the background of the root view)
         *
         *
         * source : https://stackoverflow.com/questions/14779259/get-background-color-of-a-layout
         *
         * @param xmlRootView
         * @return
         */
        fun getDefaultBackgroundColor(xmlRootView: View): Int {
            var color = Color.TRANSPARENT
            val background = xmlRootView.background
            if (background is ColorDrawable) color = background.color
            return color
        }

        /**
         * Reference : https://stackoverflow.com/questions/37775675/imageview-set-color-filter-to-gradient
         *
         * @param originalBitmap
         * @return
         */
        fun addGradientToImageView(context: Context?, originalBitmap: Bitmap): Bitmap? {
            val width = originalBitmap.width
            val height = originalBitmap.height
            val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(updatedBitmap)
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)
            val paint = Paint()
            val colors = intArrayOf(
                    ContextCompat.getColor(context!!, R.color.admin_splash_bg),
                    ContextCompat.getColor(context, R.color.adminDashboardColorPrimary),
                    ContextCompat.getColor(context, R.color.adminDashboardSelectedItemAccent),
                    ContextCompat.getColor(context, R.color.multiPaneColorPrimaryDark))

            val shader = LinearGradient(
                    0f, 0f,
                    0f, height.toFloat(),
                    colors,
                    null,
                    Shader.TileMode.CLAMP)
            paint.shader = shader
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            return updatedBitmap
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            var width = drawable.intrinsicWidth
            width = if (width > 0) width else 1
            var height = drawable.intrinsicHeight
            height = if (height > 0) height else 1
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }


        fun loadImageBlurred(context: Context?,
                             imageURL: Any?,
                             targetImageView: ShapeableImageView?) {
            //Load the background  thumb image
            Glide.with(context!!)
                    .load(imageURL)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(5, 5)))
                    .into(targetImageView!!)
        }
    }
}
package com.riders.thelab.core.ui.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.RequestListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.riders.thelab.core.ui.R
import com.riders.thelab.core.ui.compose.utils.executeOnBackPressed
import com.riders.thelab.core.ui.data.SnackBarType
import timber.log.Timber

object UIManager {

    fun showView(view: View) {
        if (View.VISIBLE != view.visibility)
            view.visibility = View.VISIBLE
    }

    fun hideView(view: View) {
        if (View.VISIBLE == view.visibility)
            view.visibility = View.GONE
    }


    /**
     * show the keyboard
     *
     * @param view
     */
    fun showKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
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
    fun showAlertDialog(
        activity: Activity?,
        context: Context,
        title: String,
        message: String,
        negativeMessage: String?,
        positiveMessage: String
    ) {
        Timber.i("Show alert dialog")
        val alertDialog = AlertDialog.Builder(context)

        // Setting Dialog Title
        alertDialog.setTitle(title)

        // Setting Dialog Message
        alertDialog.setMessage(message)
        alertDialog.setNegativeButton(negativeMessage) { _: DialogInterface, _: Int ->
            if (negativeMessage != null) {
                this.showToast(context, negativeMessage)

                if (negativeMessage.equals("Réessayer", ignoreCase = true)) {
                    //launchActivity(context, MainActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NEW_TASK, null, null);
                }
            }
            /*if (negativeMessage.equals("Réessayer", ignoreCase = true)
                && LabNetworkManagerNewAPI.getInstance(context).isOnline()
            ) {
                dialog.dismiss()
            }*/
        }
        alertDialog.setPositiveButton(positiveMessage) { _: DialogInterface?, _: Int ->
            this.showToast(context, positiveMessage)
//            activity?.onBackPressed()
            activity?.let { executeOnBackPressed(it) }
            if (negativeMessage.equals("Quitter", ignoreCase = true)) {
                activity!!.finish()
            }
        }
        alertDialog.setCancelable(false)

        // Showing Alert Message
        alertDialog.show()
    }


    fun showToast(context: Context, @StringRes stringResId: Int) {
        this.showToast(context, context.getString(stringResId))
    }

    fun showToast(context: Context, message: String) =
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    fun showActionInSnackBar(
        context: Activity,
        message: String,
        type: SnackBarType,
        actionText: String? = null,
        listener: View.OnClickListener? = null
    ) {
        // create instance
        val snackBar = Snackbar.make(
            context.findViewById(android.R.id.content),
            message,
            if (listener != null) BaseTransientBottomBar.LENGTH_INDEFINITE else BaseTransientBottomBar.LENGTH_LONG
        )
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
            context.findViewById(android.R.id.content),
            context.getString(
                if (!isConnected) R.string.network_status_disconnected else R.string.network_status_connected
            ),
            BaseTransientBottomBar.LENGTH_LONG
        )
        snackbar.setBackgroundTint(
            ContextCompat.getColor(
                context,
                if (!isConnected) R.color.locationColorPrimaryDark else R.color.contactsDatabaseColorPrimaryDark
            )
        )
        val sbView = snackbar.view
        val textView =
            sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
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
    fun addGradientToImageView(context: Context, originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val paint = Paint()
        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.admin_splash_bg),
            ContextCompat.getColor(context, R.color.adminDashboardColorPrimary),
            ContextCompat.getColor(context, R.color.adminDashboardSelectedItemAccent),
            ContextCompat.getColor(context, R.color.multiPaneColorPrimaryDark)
        )

        val shader = LinearGradient(
            0f, 0f,
            0f, height.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        return updatedBitmap
    }

    /**
     * Convert drawable to bitmap
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
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

    fun convertVectorToBitmap(drawable: Drawable): Bitmap? {
        return try {
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: OutOfMemoryError) {
            // Handle the error
            e.printStackTrace()
            null
        }
    }

    fun loadImage(
        context: Context,
        iconResDrawable: Any,
        targetImageView: ShapeableImageView
    ) {
        LabGlideUtils.getInstance().loadImage(context, iconResDrawable, targetImageView)
    }

    fun loadImage(
        context: Context,
        iconResDrawable: Any,
        targetImageView: ShapeableImageView,
        listener: RequestListener<Drawable>
    ) {
        LabGlideUtils.getInstance()
            .loadImage(context, iconResDrawable, targetImageView, listener)
    }

    /**
     * Load Image in view and apply a blurry effect
     */
    fun loadImageBlurred(
        context: Context,
        imageURL: Any,
        targetImageView: ShapeableImageView
    ) {
        LabGlideUtils.getInstance().loadImageBlurred(context, imageURL, targetImageView)
    }


    fun getBitmapFromDrawable(context: Context, drawableResId: Int): Bitmap? {
        if (0 == drawableResId)
            return null
        return drawableToBitmap(ContextCompat.getDrawable(context, drawableResId)!!)
    }

    fun getDrawable(context: Context, drawableResId: Int): Drawable? {
        if (0 == drawableResId)
            return null
        return ContextCompat.getDrawable(
            context,
            drawableResId
        )
    }

    fun getDrawableAsBitmap(context: Context, drawableRedId: Int): Bitmap? {
        if (0 == drawableRedId)
            return null
        return drawableToBitmap(ContextCompat.getDrawable(context, drawableRedId)!!)
    }

    fun setBackgroundColor(context: Context, targetView: View, colorResID: Int) {
        targetView.setBackgroundColor(ContextCompat.getColor(context, colorResID))
    }

    fun updateToolbarIcon(activity: Activity, menu: Menu, actionId: Int, drawableResId: Int) {
        menu.findItem(actionId)?.icon = ContextCompat.getDrawable(activity, drawableResId)
    }
}
package com.riders.thelab.core.permissions

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.riders.thelab.core.data.local.model.Permission
import timber.log.Timber
import java.lang.ref.WeakReference

class PermissionManager {

    private var mActivity: WeakReference<ComponentActivity>? = null
    private var mFragment: WeakReference<Fragment>? = null

    private val requiredPermissions = mutableListOf<Permission>()
    private var rationale: String? = null
    private var callback: (Boolean) -> Unit = {}
    private var detailedCallback: (Map<Permission, Boolean>) -> Unit = {}
    private var permissionCheck: ActivityResultLauncher<Array<String>>? = null

    constructor(activity: ComponentActivity) {
        this.mActivity = WeakReference<ComponentActivity>(activity)
        this.permissionCheck = mActivity?.get()
            ?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
                sendResultAndCleanUp(grantResults)
            }
    }

    constructor(fragment: Fragment) {
        this.mFragment = WeakReference<Fragment>(fragment)
        permissionCheck = mFragment?.get()
            ?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
                sendResultAndCleanUp(grantResults)
            }
    }


    @SuppressLint("ObsoleteSdkInt")
    private fun shouldAskPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun shouldAskPermission(context: Context, permission: String): Boolean {
        if (shouldAskPermission()) {
            val permissionResult = ActivityCompat.checkSelfPermission(context, permission)
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    fun rationale(description: String): PermissionManager {
        rationale = description
        return this
    }

    fun request(vararg permission: Permission): PermissionManager {
        requiredPermissions.addAll(permission)
        return this
    }

    @JvmName("requestWithArray")
    fun request(permission: Array<Permission>): PermissionManager {
        requiredPermissions.addAll(permission)
        return this
    }

    fun checkPermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        handlePermissionRequest()
    }

    fun checkDetailedPermission(callback: (Map<Permission, Boolean>) -> Unit) {
        this.detailedCallback = callback
        handlePermissionRequest()
    }

    private fun handlePermissionRequest() {
        Timber.d("handlePermissionRequest()")
        if (null != mFragment) {
            Timber.d("null != mFragment")
            mFragment?.get()?.let { fragment ->
                when {
                    areAllPermissionsGranted(fragment) -> sendPositiveResult()
                    shouldShowPermissionRationale(fragment) -> displayRationale(fragment)
                    else -> requestPermissions()
                }
            }
        }

        if (null != mActivity) {
            Timber.d("null != mActivity")
            mActivity?.get()?.let { activity ->
                when {
                    areAllPermissionsGranted(activity) -> sendPositiveResult()
                    shouldShowPermissionRationale(activity) -> displayRationale(activity)
                    else -> requestPermissions()
                }
            }
        }
    }

    private fun displayRationale(activity: ComponentActivity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog_permission_title))
            .setMessage(rationale ?: activity.getString(R.string.dialog_permission_default_message))
            .setCancelable(false)
            .setPositiveButton(activity.getString(R.string.dialog_permission_button_positive)) { _, _ ->
                requestPermissions()
            }
            .show()
    }


    private fun displayRationale(fragment: Fragment) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.dialog_permission_title))
            .setMessage(rationale ?: fragment.getString(R.string.dialog_permission_default_message))
            .setCancelable(false)
            .setPositiveButton(fragment.getString(R.string.dialog_permission_button_positive)) { _, _ ->
                requestPermissions()
            }
            .show()
    }

    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associateWith { true })
    }

    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        callback(grantResults.all { it.value })
        detailedCallback(grantResults.mapKeys { Permission.from(it.key) })
        cleanUp()
    }

    private fun cleanUp() {
        requiredPermissions.clear()
        rationale = null
        callback = {}
        detailedCallback = {}
    }

    private fun requestPermissions() {
        permissionCheck?.launch(getPermissionList())
    }

    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

    // ACTIVITY
    private fun areAllPermissionsGranted(activity: ComponentActivity) =
        requiredPermissions.all { it.isGranted(activity) }

    private fun shouldShowPermissionRationale(activity: ComponentActivity) =
        requiredPermissions.any { it.requiresRationale(activity) }

    private fun Permission.isGranted(activity: ComponentActivity) =
        permissions.all { hasPermission(activity, it) }

    @SuppressLint("NewApi")
    private fun Permission.requiresRationale(activity: ComponentActivity) =
        permissions.any { activity.shouldShowRequestPermissionRationale(it) }

    private fun hasPermission(activity: ComponentActivity, permission: String) =
        ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED


    // FRAGMENT
    private fun areAllPermissionsGranted(fragment: Fragment) =
        requiredPermissions.all { it.isGranted(fragment) }

    private fun shouldShowPermissionRationale(fragment: Fragment) =
        requiredPermissions.any { it.requiresRationale(fragment) }

    /*private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()*/

    private fun Permission.isGranted(fragment: Fragment) =
        permissions.all { hasPermission(fragment, it) }

    private fun Permission.requiresRationale(fragment: Fragment) =
        permissions.any { fragment.shouldShowRequestPermissionRationale(it) }

    private fun hasPermission(fragment: Fragment, permission: String) =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED


    companion object {
        fun from(activity: ComponentActivity) = PermissionManager(activity)
        fun from(fragment: Fragment) = PermissionManager(fragment)
    }
}
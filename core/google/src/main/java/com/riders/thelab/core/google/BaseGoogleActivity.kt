package com.riders.thelab.core.google

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.riders.thelab.core.ui.compose.base.BaseComponentActivity
import com.riders.thelab.core.ui.utils.UIManager
import timber.log.Timber

abstract class BaseGoogleActivity : BaseComponentActivity(), GoogleActions {

    open val mGoogleSignInRequestLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val intent: Intent? = result.data

            when (result.resultCode) {
                RESULT_CANCELED -> {
                    Timber.e("ActivityResultContracts.StartActivityForResult | Activity.RESULT_CANCELED")

                    if (null == intent) {
                        UIManager.showToast(this, "Google Login Error!")
                        return@registerForActivityResult
                    }
                    Timber.e("ActivityResultContracts.StartActivityForResult | Activity.RESULT_CANCELED | bundle: ${intent.extras?.toString()}")
                }

                RESULT_OK -> {
                    Timber.d("ActivityResultContracts.StartActivityForResult | Activity.RESULT_OK")

                    if (null == intent) {
                        UIManager.showToast(this, "Google Login Error!")
                        return@registerForActivityResult
                    }
                    UIManager.showToast(this, "Google Login Success!")

                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(intent)

                    /**
                     * handle [task] result
                     */
                    Timber.d("ActivityResultContracts.StartActivityForResult | handle [task] result")

                    task
                        .addOnFailureListener { throwable ->
                            Timber.e("task | addOnFailureListener | message: ${throwable.message} (class: ${throwable::class.java.canonicalName})")
                        }
                        .addOnSuccessListener {
                            Timber.d("task | addOnSuccessListener | value: $it")
                        }
                        .addOnCompleteListener {
                            if (!task.isSuccessful) {
                                Timber.e("task | addOnCompleteListener | Google Sign In Failed")
                            } else {
                                Timber.i("task | addOnCompleteListener | Sign in successful")
                                val account: GoogleSignInAccount? = task.result

                                account?.let { onConnected(it) } ?: run {
                                    Timber.e("task | addOnCompleteListener | account is null")
                                }
                            }
                        }
                }

                else -> {
                    Timber.d("ActivityResultContracts.StartActivityForResult | else branch with result code : ${result.resultCode}")
                }
            }
        }
}
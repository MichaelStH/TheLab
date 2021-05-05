package com.riders.thelab.ui.biometric;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.riders.thelab.R;
import com.riders.thelab.core.utils.LabCompatibilityManager;
import com.riders.thelab.core.utils.LabDeviceManager;
import com.riders.thelab.core.utils.UIManager;
import com.riders.thelab.data.local.bean.SnackBarType;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.infinum.goldfinger.Goldfinger;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class BiometricActivity extends AppCompatActivity {

    private Context context;

    // Views
    @BindView(R.id.finger_print_btn)
    AppCompatImageButton fingerPrintButton;

    ///////////////////////
    //
    // OVERRIDE
    //
    ///////////////////////
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric);

        this.init();

        if (!LabCompatibilityManager.isMarshmallow()) {
            Timber.e("Incompatibility detected - Device runs on API below API 23 (Marshmallow)");

            UIManager
                    .showActionInSnackBar(
                            this,
                            findViewById(android.R.id.content),
                            "Incompatibility detected - Device runs on API below API 23 (Marshmallow)",
                            SnackBarType.ALERT,
                            "LEAVE",
                            v -> {
                                finish();
                            });
        } else {

            // initGoldFinger
            this.initGoldFinger();
        }
    }


    ///////////////////////
    //
    // BUTTERKNIFE
    //
    ///////////////////////
    @OnClick(R.id.finger_print_btn)
    public void onFingerPrintClicked() {
        Timber.d("on fingerprint button clicked");

        if (!LabDeviceManager.getRxGoldFinger().canAuthenticate()) {
            Timber.e("Cannot authenticate");
            return;
        }

        authenticateWithRX();
    }


    ///////////////////////
    //
    // CLASSES METHODS
    //
    ///////////////////////
    private void init() {

        context = this;

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.activity_title_biometric));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initGoldFinger() {
        LabDeviceManager.initFingerPrintWithRx(context, this);

        // Check if fingerprint hardware is available
        if (!LabDeviceManager.hasFingerPrintHardware()) {
            Timber.e("The device doesn't have finger print hardware");

            UIManager
                    .showActionInSnackBar(
                            this,
                            findViewById(android.R.id.content),
                            "The device doesn't have finger print hardware",
                            SnackBarType.ALERT,
                            "LEAVE",
                            v -> {
                                finish();
                            });
            return;
        }

        Timber.d("Fingerprint hardware ok");

        // Check if device can authenticate
        if (!LabDeviceManager.getRxGoldFinger().canAuthenticate()) {
            Timber.e("Cannot authenticate");

            UIManager
                    .showActionInSnackBar(
                            this,
                            findViewById(android.R.id.content),
                            "Cannot authenticate",
                            SnackBarType.ALERT,
                            "QUIT",
                            v -> {
                                finish();
                            });
            return;
        }

        // Init successfully executed
        UIManager
                .showActionInSnackBar(
                        this,
                        findViewById(android.R.id.content),
                        "Fingerprint initialization successfully executed",
                        SnackBarType.NORMAL,
                        "CONTINUE",
                        v -> {
                            Timber.d("User action validate");
                        });
    }

    private void authenticateWithRX() {
        Timber.d("authenticateWithRX()");
        //rxGoldFinger
        LabDeviceManager.getRxGoldFinger()
                .authenticate(LabDeviceManager.getGoldFingerPromptParams())
                .subscribe(new DisposableObserver<Goldfinger.Result>() {

                    @Override
                    public void onComplete() {
                        /* Fingerprint authentication is finished */
                        Timber.d("Authentication complete");
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        /* Critical error happened */
                        Timber.e(e);
                    }

                    @Override
                    public void onNext(Goldfinger.@NotNull Result result) {
                        /* Result received */

                        Timber.d(
                                "Result :\n" +
                                        "type : " + result.type() + "\n" +
                                        "value : " + result.value() + "\n" +
                                        "reason : " + result.reason() + "\n" +
                                        "message : " + result.message() + "\n" +
                                        "");
                    }
                });
    }

}

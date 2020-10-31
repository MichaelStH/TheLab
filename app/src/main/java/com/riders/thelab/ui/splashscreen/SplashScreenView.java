package com.riders.thelab.ui.splashscreen;

import com.riders.thelab.navigator.Navigator;
import com.riders.thelab.ui.base.BaseViewImpl;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class SplashScreenView extends BaseViewImpl<SplashScreenPresenter>
        implements SplashScreenContract.View {

    @Inject
    Navigator navigator;

    private SplashScreenActivity context;

    @Inject
    SplashScreenView(SplashScreenActivity context) {
        this.context = context;
    }


    @Override
    public void onCreate() {

        ButterKnife.bind(this, context.findViewById(android.R.id.content));

        getPresenter().attachView(this);

        getPresenter().hasPermissions(context);

    }


    @Override
    public void onPermissionsGranted() {
        Completable
                .complete()
                .delay(5, TimeUnit.SECONDS)
                .doOnComplete(() -> {
                    if (context != null && navigator != null) {
                        navigator.callMainActivity();
                        context.finish();
                    }

                })
                .doOnError(Timber::e)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    @Override
    public void onPermissionsDenied() {
        Timber.e("onPermissionsDenied()");
        closeApp();
    }

    @Override
    public void closeApp() {
        context.finish();
    }

    @Override
    public void onDestroy() {
        getPresenter().detachView();
        context = null;
    }
}

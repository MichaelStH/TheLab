package com.riders.thelab.ui.base;

public interface BaseView<T> {

    void onCreate();

    /*void onActivityCreated();

    void onSaveInstanceState(Bundle savedInstanceState);

    void onRestoreInstanceState(Bundle savedInstanceState);

    void onPause();

    void onResume();

    void onDetach();*/

    void onDestroy();
}

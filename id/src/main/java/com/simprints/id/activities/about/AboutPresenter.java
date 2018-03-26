package com.simprints.id.activities.about;


import android.support.annotation.NonNull;
import android.view.WindowManager;

import com.simprints.id.data.DataManager;
import com.simprints.id.domain.ALERT_TYPE;
import com.simprints.id.domain.Constants;
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError;
import com.simprints.id.tools.AlertLauncher;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class AboutPresenter implements AboutContract.Presenter {

    @NonNull
    private final AboutContract.View aboutView;

    private static boolean recoveryRunning = false;

    private DataManager dataManager;

    private AlertLauncher alertLauncher;

    /**
     * @param view The AboutActivity
     */
    AboutPresenter(@NonNull AboutContract.View view, DataManager dataManager, AlertLauncher alertLauncher) {
        this.dataManager = dataManager;
        aboutView = view;
        this.alertLauncher = alertLauncher;
    }

    @Override
    public void start() {
        initView();
    }

    private void initView() {
        try {
            initVersions();
            initCounts();
            initRecoveryAvailability();
        } catch (UninitializedDataManagerError error) {
            dataManager.logError(error);
            alertLauncher.launch(ALERT_TYPE.UNEXPECTED_ERROR ,0);
        }
    }

    private void initVersions() {
        aboutView.setVersionData(
            dataManager.getAppVersionName(),
            dataManager.getLibVersionName(),
            dataManager.getHardwareVersionString());
    }

    private void initCounts() {
        aboutView.setDbCountData(
            Long.toString(dataManager.getPeopleCount(Constants.GROUP.USER)),
            Long.toString(dataManager.getPeopleCount(Constants.GROUP.MODULE)),
            Long.toString(dataManager.getPeopleCount(Constants.GROUP.GLOBAL)));
    }

    private void initRecoveryAvailability() {
        if (recoveryRunning) {
            aboutView.setRecoverDbUnavailable();
        } else {
            aboutView.setRecoverDbAvailable();
        }
    }

    @Override
    public void recoverDb() {
        try {
            recoveryRunning = true;
            dataManager.recoverRealmDb(Constants.GROUP.GLOBAL)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action() {
                    @Override
                    public void run() {
                        recoveryRunning = false;
                        try {
                            aboutView.setSuccessRecovering();
                            aboutView.setRecoverDbAvailable();
                        } catch (WindowManager.BadTokenException e) {
                            dataManager.logSafeException(e);
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        recoveryRunning = false;
                        try {
                            aboutView.setErrorRecovering(throwable.getMessage());
                            aboutView.setRecoverDbAvailable();
                        } catch (WindowManager.BadTokenException e) {
                            dataManager.logSafeException(e);
                            e.printStackTrace();
                        }
                    }
                });

        } catch (UninitializedDataManagerError error) {
            dataManager.logError(error);
            alertLauncher.launch(ALERT_TYPE.UNEXPECTED_ERROR, 0);
        }
    }
}

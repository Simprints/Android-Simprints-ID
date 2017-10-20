package com.simprints.id.activities.about;


import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import com.simprints.id.tools.AppState;
import com.simprints.id.tools.InternalConstants;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.tools.Constants;

class AboutPresenter implements AboutContract.Presenter {

    @NonNull
    private final AboutContract.View aboutView;

    private static boolean recoveryRunning = false;

    private AppState appState;
    private RecoverDbHandlerThread recoverDbHandlerThread;

    /**
     * @param view      The AboutActivity
     */
    AboutPresenter(@NonNull AboutContract.View view) {
        appState = AppState.getInstance();

        aboutView = view;
        aboutView.setPresenter(this);
    }

    @Override
    public void start() {
        aboutView.setVersionData(
                appState.getAppVersion() != null ? appState.getAppVersion() : "null",
                InternalConstants.LIBSIMPRINTS_VERSION,
                appState.getHardwareVersion() > -1 ? String.valueOf(appState.getHardwareVersion()) : "null");

        aboutView.setDbCountData(
                Long.toString(appState.getData().getPeopleCount(Constants.GROUP.USER)),
                Long.toString(appState.getData().getPeopleCount(Constants.GROUP.MODULE)),
                Long.toString(appState.getData().getPeopleCount(Constants.GROUP.GLOBAL)));

        if (recoveryRunning) aboutView.setRecoverDbUnavailable();
        else aboutView.setRecoverDbAvailable();
    }

    @Override
    public void recoverDb() {
        recoveryRunning = true;
        recoverDbHandlerThread = new RecoverDbHandlerThread("recoverDbHandlerThread");
        recoverDbHandlerThread.start();
        recoverDbHandlerThread.prepareHandler();
        recoverDbHandlerThread.postTask(new Runnable() {
            @Override
            public void run() {
                String androidId = appState.getDeviceId() != null? appState.getDeviceId() : "no-device-id";
                appState.getData().recoverRealmDb(androidId + "_" + Long.toString(System.currentTimeMillis()) + ".json",
                        androidId,
                        Constants.GROUP.GLOBAL,
                        new DataCallback() {
                            @Override
                            public void onSuccess() {
                                recoverDbHandlerThread.quit();
                                recoveryRunning = false;
                                try {
                                    aboutView.setSuccessRecovering();
                                    aboutView.setRecoverDbAvailable();
                                } catch (WindowManager.BadTokenException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(DATA_ERROR data_error) {
                                recoverDbHandlerThread.quit();
                                recoveryRunning = false;
                                try {
                                    aboutView.setErrorRecovering(data_error.details());
                                    aboutView.setRecoverDbAvailable();
                                } catch (WindowManager.BadTokenException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });
    }

    private class RecoverDbHandlerThread extends HandlerThread {

        Handler handler;

        RecoverDbHandlerThread(String name) {
            super(name);
        }

        void postTask(Runnable task) {
            handler.post(task);
        }

        void prepareHandler() {
            handler = new Handler(getLooper());
        }
    }
}

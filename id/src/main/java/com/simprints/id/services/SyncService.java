package com.simprints.id.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.simprints.id.tools.SharedPref;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.ResultListener;

public class SyncService extends Service {
    private final IBinder binder = new SyncBinder();
    private ResultListener dataResultListener;
    private ResultListener callback;
    private boolean syncStarted = false;

    private class SyncBinder extends Binder {
        void setListener(ResultListener resultListener) {
            SyncService.this.callback = resultListener;
        }
    }

    public static ServiceConnection buildListener(@Nullable final ResultListener resultListener) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SyncBinder binder = (SyncBinder) service;
                if (resultListener != null)
                    binder.setListener(resultListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    @Override
    public void onCreate() {
        dataResultListener = new com.simprints.libdata.ResultListener() {
            @Override
            public void onSuccess() {
                if (callback != null)
                    callback.onSuccess();
                syncStarted = false;
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                if (callback != null)
                    callback.onFailure(data_error);
                syncStarted = false;
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (syncStarted)
            return START_NOT_STICKY;

        syncStarted = true;
        SharedPref sharedPref = new SharedPref(getApplicationContext());

        switch (sharedPref.getSyncGroup()) {
            case GLOBAL:
                new DatabaseSync(getApplicationContext(),
                        sharedPref.getAppKeyString(),
                        dataResultListener).sync();
                break;
            case USER:
                new DatabaseSync(getApplicationContext(),
                        sharedPref.getAppKeyString(),
                        dataResultListener,
                        sharedPref.getLastUserIdString()).sync();
                break;
        }

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}

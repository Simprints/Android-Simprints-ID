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
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.DatabaseSync;

import java.util.ArrayList;

public class SyncService extends Service {
    private final IBinder binder = new SyncBinder();
    private DataCallback dataCallback;
    private boolean syncStarted = false;
    private ArrayList<DataCallback> resultListeners;

    private class SyncBinder extends Binder {
        void setListener(DataCallback resultListener) {
            if (!SyncService.this.resultListeners.contains(resultListener))
                SyncService.this.resultListeners.add(resultListener);
        }
    }

    public static ServiceConnection buildListener(@Nullable final DataCallback resultListener) {
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
        resultListeners = new ArrayList<>();
        dataCallback = new com.simprints.libdata.DataCallback() {
            @Override
            public void onSuccess() {
                for (DataCallback callback : resultListeners)
                    callback.onSuccess();
                syncStarted = false;
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                for (DataCallback callback : resultListeners)
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

        //Get and check data in case background sync fires before someone logs in
        SharedPref sharedPref = new SharedPref(getApplicationContext());
        String appKey = sharedPref.getAppKeyString();
        String userId = sharedPref.getLastUserIdString();
        if (appKey == null || appKey.isEmpty()) {
            dataCallback.onFailure(DATA_ERROR.SYNC_INTERRUPTED);
            return START_NOT_STICKY;
        }
        if (userId == null || userId.isEmpty()) {
            dataCallback.onFailure(DATA_ERROR.SYNC_INTERRUPTED);
            return START_NOT_STICKY;
        }

        switch (sharedPref.getSyncGroup()) {
            case MODULE:
            case GLOBAL:
                new DatabaseSync(getApplicationContext(), appKey, dataCallback).sync();
                break;
            case USER:
                new DatabaseSync(getApplicationContext(), appKey, dataCallback, userId).sync();
                break;
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}

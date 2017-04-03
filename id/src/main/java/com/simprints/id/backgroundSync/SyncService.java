package com.simprints.id.backgroundSync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.simprints.id.tools.SharedPref;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.DataCallback;

import java.util.HashSet;
import java.util.Set;

public class SyncService implements DataCallback {

    private static SyncService instance = null;

    private boolean syncInProgress;
    private final Set<DataCallback> resultListeners;

    private SyncService() {
        syncInProgress = false;
        resultListeners = new HashSet<>();
    }

    public synchronized static SyncService getInstance() {
        if (instance == null) {
            instance = new SyncService();
        }
        return instance;
    }

    /**
     * Don't start a new sync if there is only one in progress
     */
    public synchronized boolean startAndListen(@NonNull Context appContext, @Nullable DataCallback dataCallback) {
        SharedPref sharedPref = new SharedPref(appContext);
        String appKey = sharedPref.getAppKeyString();
        String userId = sharedPref.getLastUserIdString();
        if (appKey == null || appKey.isEmpty() || userId == null || userId.isEmpty()) {
            return false;
        }

        if (dataCallback != null)
            resultListeners.add(dataCallback);

        if (!syncInProgress) {
            syncInProgress = true;
            switch (sharedPref.getSyncGroup()) {
                case GLOBAL:
                    new DatabaseSync(appContext, appKey, this).sync();
                    break;
                case USER:
                    new DatabaseSync(appContext, appKey, this, userId).sync();
                    break;
            }
        }
        return true;
    }

    public synchronized void stopListening(@Nullable DataCallback dataCallback) {
        if (dataCallback != null)
            resultListeners.remove(dataCallback);
    }

    @Override
    public synchronized void onSuccess() {
        for (DataCallback callback : resultListeners)
            callback.onSuccess();

        resultListeners.clear();
        syncInProgress = false;
    }

    @Override
    public synchronized void onFailure(DATA_ERROR data_error) {
        for (DataCallback callback : resultListeners)
            callback.onFailure(data_error);

        resultListeners.clear();
        syncInProgress = false;
    }
}

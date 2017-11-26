package com.simprints.id.backgroundSync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.simprints.id.data.DataManager;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.DatabaseSync;

import java.util.HashSet;
import java.util.Set;

public class SyncService implements DataCallback {

    private static SyncService instance = null;

    private boolean syncInProgress;
    private final Set<DataCallback> resultListeners;
    private DataManager dataManager;

    private SyncService(DataManager dataManager) {
        syncInProgress = false;
        resultListeners = new HashSet<>();
        this.dataManager = dataManager;
    }

    public synchronized static SyncService getInstance(DataManager dataManager) {
        if (instance == null) {
            instance = new SyncService(dataManager);
        }
        return instance;
    }

    /**
     * Don't start a new sync if there is only one in progress
     */
    public synchronized boolean startAndListen(@NonNull Context appContext, @Nullable DataCallback dataCallback) {
        String appKey = dataManager.getAppKey();
        String userId = dataManager.getLastUserId();
        Log.d("sync", "startAndListen()");
        if (appKey.isEmpty() || userId.isEmpty()) {
            Log.d("sync", "first if");
            return false;
        }

        if (dataCallback != null)
            resultListeners.add(dataCallback);

        if (!syncInProgress) {
            syncInProgress = true;
            switch (dataManager.getSyncGroup()) {
                case GLOBAL:
                    Log.d("sync", "calling global sync");
                    new DatabaseSync(appContext, appKey, this).sync();
                    break;
                case USER:
                    Log.d("sync", "calling user sync");
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

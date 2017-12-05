package com.simprints.id.backgroundSync;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.id.Application;

public class GcmSyncService extends GcmTaskService {

    private SyncService syncService;

    @Override
    public void onCreate() {
        super.onCreate();
        Application app = ((Application) getApplication());
        syncService = app.getSyncService();
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        new SyncSetup(getApplicationContext()).initialize();
    }

    @SuppressLint("HardwareIds")
    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("GcmSyncService", "onRunTask Called");

        boolean result = syncService.startAndListen(getApplicationContext(), null);
        if (!result) {
            Log.d("GcmSyncService", "Missing api key / user id");
            return GcmNetworkManager.RESULT_FAILURE;
        } else {
            Log.d("GcmSyncService", "Background sync started");
            return GcmNetworkManager.RESULT_SUCCESS;
        }
    }
}

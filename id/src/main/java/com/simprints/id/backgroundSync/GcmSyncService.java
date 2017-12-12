package com.simprints.id.backgroundSync;

import android.annotation.SuppressLint;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.id.Application;
import com.simprints.id.exceptions.unsafe.InvalidSyncParametersError;

import timber.log.Timber;

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
        Timber.d("GcmSyncService: onRunTask Called");

        try{
            syncService.startAndListen(getApplicationContext(), null);
            Timber.d("GcmSyncService: Background sync started");
            return GcmNetworkManager.RESULT_SUCCESS;
        } catch (InvalidSyncParametersError e) {
            Timber.d("GcmSyncService: Missing api key / user id");
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }
}

package com.simprints.id.backgroundSync;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.id.services.SyncService;

public class GcmSyncService extends GcmTaskService {
    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        new SyncSetup(getApplicationContext()).initialize();
    }

    @SuppressLint("HardwareIds")
    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("GcmSyncService", "onRunTask Called");

        startService(new Intent(this, SyncService.class));
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}

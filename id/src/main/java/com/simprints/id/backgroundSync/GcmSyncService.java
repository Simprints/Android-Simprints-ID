package com.simprints.id.backgroundSync;

import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.Event;

public class GcmSyncService extends GcmTaskService implements DatabaseEventListener {
    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        new SyncSetup(getApplicationContext()).initialize();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("GcmSyncService", "onRunTask Called");

        DatabaseSync.sync(getApplicationContext(), this);

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @Override
    public void onDataEvent(Event event) {
        switch (event) {

            case SYNC_INTERRUPTED:
                Log.d("BACKGROUND SYNC", "SYNC_INTERRUPTED");
                break;
            case SYNC_SUCCESS:
                Log.d("BACKGROUND SYNC", "SYNC_SUCCESS");
                break;
            case SIGNED_IN:
                Log.d("BACKGROUND SYNC", "SIGNED_IN");
                break;

        }
    }
}

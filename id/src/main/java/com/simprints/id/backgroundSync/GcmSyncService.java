package com.simprints.id.backgroundSync;

import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libdata.models.M_ApiKey;

import java.util.List;

public class GcmSyncService extends GcmTaskService {

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        new SyncSetup(getApplicationContext()).initialize();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("GcmSyncService", "onRunTask Called");

        DatabaseContext.initActiveAndroid(getApplicationContext());
        List<M_ApiKey> keys = DatabaseContext.getSyncKeys();

        for (final M_ApiKey key : keys) {
            new DatabaseContext(key.asString(), getApplicationContext(), new DatabaseEventListener() {
                @Override
                public void onDataEvent(Event event) {
                    Log.d("GcmSyncService", event.toString());
                }
            }).sync();
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }
}

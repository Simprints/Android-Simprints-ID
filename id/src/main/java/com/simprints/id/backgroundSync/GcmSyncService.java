package com.simprints.id.backgroundSync;

import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.id.tools.SharedPrefHelper;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;

public class GcmSyncService extends GcmTaskService implements DatabaseEventListener {
    private DatabaseContext databaseContext;

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        new SyncSetup(getApplicationContext()).initialize();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("GcmSyncService", "onRunTask Called");

        String userId = new SharedPrefHelper(getApplicationContext()).getLastUserId();

        DatabaseContext.initDatabase(getApplicationContext(), userId);

        String key = DatabaseContext.signedInUserId();
        if (key != null) {
            databaseContext = new DatabaseContext(key, userId, getApplicationContext(), this);
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @Override
    public void onDataEvent(Event event) {
        switch (event) {

            case SYNC_INTERRUPTED:
                databaseContext.sync();
                break;
            case SYNC_SUCCESS:
                databaseContext.destroy();
                break;
            case SIGNED_IN:
                databaseContext.sync();
                break;

        }
    }
}

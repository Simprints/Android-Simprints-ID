package com.simprints.id.backgroundSync;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.id.tools.Analytics;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.Event;

public class GcmSyncService extends GcmTaskService implements DatabaseEventListener {
    private String deviceId;

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        new SyncSetup(getApplicationContext()).initialize();
    }

    @SuppressLint("HardwareIds")
    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("GcmSyncService", "onRunTask Called");

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        DatabaseSync.sync(getApplicationContext(), this, null);

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @Override
    public void onDataEvent(Event event) {
        switch (event) {

            case SYNC_INTERRUPTED:
                Log.d("BACKGROUND SYNC", "SYNC_INTERRUPTED");
                Analytics.getInstance(getApplicationContext()).setBackgroundSync(false, deviceId);
                break;
            case SYNC_SUCCESS:
                Log.d("BACKGROUND SYNC", "SYNC_SUCCESS");
                Analytics.getInstance(getApplicationContext()).setBackgroundSync(true, deviceId);
                break;

        }
    }
}

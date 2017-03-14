package com.simprints.id.backgroundSync;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.simprints.id.tools.Analytics;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.id.tools.SharedPref;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.ResultListener;
import com.simprints.libdata.tools.Constants;

public class GcmSyncService extends GcmTaskService {
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
        SharedPref sharedPref = new SharedPref(getApplicationContext());
        RemoteConfig.init();

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String appKey = sharedPref.getAppKeyString();

        if (appKey == null || appKey.isEmpty())
            return GcmNetworkManager.RESULT_FAILURE;

        Constants.GROUP syncGroup = sharedPref.getSyncGroup();

        ResultListener resultListener = new ResultListener() {
            @Override
            public void onSuccess() {
                Log.d("BACKGROUND SYNC", "SYNC_SUCCESS");
                Analytics.getInstance(getApplicationContext()).setBackgroundSync(true, deviceId);
            }

            @Override
            public void onFailure(DATA_ERROR data_error) {
                Log.d("BACKGROUND SYNC", "SYNC_INTERRUPTED");
                Analytics.getInstance(getApplicationContext()).setBackgroundSync(false, deviceId);
            }
        };

        switch (syncGroup) {
            case GLOBAL:
                new DatabaseSync(getApplicationContext(),
                        sharedPref.getAppKeyString(),
                        resultListener).sync();
                break;
            case USER:
                new DatabaseSync(getApplicationContext(),
                        sharedPref.getAppKeyString(),
                        resultListener,
                        sharedPref.getLastUserIdString()).sync();
                break;
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }
}

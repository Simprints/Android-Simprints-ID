package com.simprints.id.backgroundSync;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

public class SyncSetup {
    private static final int RC_PLAY_SERVICES = 123;
    private Context context;

    public SyncSetup(Context context) {
        this.context = context;
    }

    public void initialize() {
        if (checkPlayServicesAvailable()) {
            GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);

            Task task = new PeriodicTask.Builder()
                    .setService(GcmSyncService.class)
                    .setPeriod(60 * 60 * 5) // 5 hours
                    .setFlex(60 * 60) // 1 hours
                    .setPersisted(true)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setTag("periodic_task")
                    .setUpdateCurrent(true)
                    .build();

            mGcmNetworkManager.schedule(task);
        }
    }

    private boolean checkPlayServicesAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                // Show dialog to resolve the error if the calling context is an activity.
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing()) {
                        availability.getErrorDialog((Activity) context, resultCode, RC_PLAY_SERVICES).show();
                    }
                }
                return false;
            } else {
                // Unresolvable error
                Toast.makeText(context, "Google Play Services error", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}

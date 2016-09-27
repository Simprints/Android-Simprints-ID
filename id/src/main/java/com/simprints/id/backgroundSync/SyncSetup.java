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
                    .setPeriod(5)
                    .setFlex(1)
                    .setPersisted(true)
                    .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                    .setTag("periodic_task")
                    .setUpdateCurrent(true)
                    .build();

            mGcmNetworkManager.schedule(task);
        } else {
            new SchedulerReceiver().setAlarm(context);
        }
    }

    private boolean checkPlayServicesAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                // Show dialog to resolve the error.
                if(context instanceof Activity) {
                    availability.getErrorDialog((Activity) context, resultCode, RC_PLAY_SERVICES).show();
                }
                return true;
            } else {
                // Unresolvable error
                Toast.makeText(context, "Google Play Services error", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}

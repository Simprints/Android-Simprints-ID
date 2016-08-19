package com.simprints.id.tools;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.simprints.libdata.Data;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundSync extends IntentService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static boolean running = false;
    private Context context;

    public BackgroundSync() {
        super("BackgroundSync");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        context = getApplicationContext();
        startSync();
    }

    private void startSync() {
        if (!running) {
            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            if (isConnected()) {
                                Data.getInstance(context).syncAll();
                            } else {
                                enableReceiver();
                                stopSelf();
                            }
                        }
                    }, 0, 10, TimeUnit.SECONDS);

            running = true;
        }
    }

    private void enableReceiver() {
        ComponentName receiver = new ComponentName(context, SyncBroadcasts.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}

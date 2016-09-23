package com.simprints.id.tools;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libdata.models.M_ApiKey;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundSync extends IntentService {
    private static boolean running = false;
    private static Iterator<M_ApiKey> keys;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Context context;

    public BackgroundSync() {
        super("BackgroundSync");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        context = getApplicationContext();
        DatabaseContext.initActiveAndroid(context);
        startSync();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        return START_STICKY;
    }

    private void startSync() {
        if (!running) {
            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            if (isConnected()) {
                                keys = null;
                                keys = DatabaseContext.getSyncKeys().iterator();
                                if(keys.hasNext()){
                                    syncing(keys.next());
                                }
                            } else {
                                enableReceiver();
                                stopSelf();
                            }
                        }
                    }, 0, 60, TimeUnit.SECONDS);

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

    private void syncing(final M_ApiKey key){
        new DatabaseContext(key.asString(), context, new DatabaseEventListener() {
            @Override
            public void onDataEvent(Event event) {
                Toast.makeText(context, "Sync Called", Toast.LENGTH_SHORT).show();

                keys.remove();
                if (keys.hasNext()){
                    syncing(keys.next());
                }
            }
        }).sync();
    }
}

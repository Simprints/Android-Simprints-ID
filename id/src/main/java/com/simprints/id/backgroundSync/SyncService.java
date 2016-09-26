package com.simprints.id.backgroundSync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.Event;
import com.simprints.libdata.models.M_ApiKey;

import java.util.Iterator;

public class SyncService extends IntentService {
    private static Iterator<M_ApiKey> keys;
    private Context context;
    private Intent intent;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SyncService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        context = getApplicationContext();
        this.intent = intent;

        DatabaseContext.initActiveAndroid(context);
        keys = DatabaseContext.getSyncKeys().iterator();

        startSync();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startSync() {
        if (keys.hasNext()) {
            new DatabaseContext(keys.next().asString(), context, new DatabaseEventListener() {
                @Override
                public void onDataEvent(Event event) {
                    Toast.makeText(context, "Sync Called", Toast.LENGTH_SHORT).show();

                    keys.remove();

                    if (keys.hasNext()) {
                        syncing(keys.next());
                    }else {
                        SchedulerReceiver.completeWakefulIntent(intent);
                    }
                }
            }).sync();
        }
    }


    private void syncing(final M_ApiKey key) {

    }
}

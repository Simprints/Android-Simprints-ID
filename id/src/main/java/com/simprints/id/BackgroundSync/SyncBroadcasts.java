package com.simprints.id.backgroundSync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SyncBroadcasts extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            new SyncSetup(context).initialize();
        }
    }
}

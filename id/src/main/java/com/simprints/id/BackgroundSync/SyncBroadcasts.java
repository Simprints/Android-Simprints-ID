package com.simprints.id.backgroundSync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SyncBroadcasts extends BroadcastReceiver {
    SchedulerReceiver alarm = new SchedulerReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarm.setAlarm(context);
        }
    }
}

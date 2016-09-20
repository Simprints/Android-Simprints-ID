package com.simprints.id.tools;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class SyncBroadcasts extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Check if the connectivity is up
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {

            //start service
            Intent pushIntent = new Intent(context, BackgroundSync.class);
            context.startService(pushIntent);

            //Disable the broadcast receiver
            ComponentName receiver = new ComponentName(context, SyncBroadcasts.class);
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

        }
    }
}
package com.simprints.id.tools;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import static com.simprints.id.activities.FrontActivity.hasPermissions;

public class PermissionManager {

    public static void requestPermissions(Activity context) {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, InternalConstants.COMMCARE_PERMISSION};

        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(context, PERMISSIONS, PERMISSION_ALL);
        }
    }
    
}

package com.simprints.id.tools;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;


public class PermissionManager {

    @NonNull
    private static List<String> requiredPermissions(AppState appState) {
        List<String> requiredPermissions = new ArrayList<>();

        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (InternalConstants.COMMCARE_PACKAGE.equalsIgnoreCase(appState.getCallingPackage()))
            requiredPermissions.add(InternalConstants.COMMCARE_PERMISSION);

        return requiredPermissions;
    }

    public static boolean checkAllPermissions(@NonNull Activity activity, AppState appState) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            for (String permission : requiredPermissions(appState))
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;

        return true;
    }

    public static void requestAllPermissions(@NonNull Activity activity, AppState appState) {
        ActivityCompat.requestPermissions(
                activity,
                requiredPermissions(appState).toArray(new String[0]),
                InternalConstants.ALL_PERMISSIONS_REQUEST
        );
    }
}

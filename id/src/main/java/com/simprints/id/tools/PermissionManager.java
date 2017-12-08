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
    private static List<String> requiredPermissions(String callingPackage) {
        List<String> requiredPermissions = new ArrayList<>();

        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (InternalConstants.COMMCARE_PACKAGE.equalsIgnoreCase(callingPackage))
            requiredPermissions.add(InternalConstants.COMMCARE_PERMISSION);

        return requiredPermissions;
    }

    public static boolean checkAllPermissions(@NonNull Activity activity, String callingPackage) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            for (String permission : requiredPermissions(callingPackage))
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;

        return true;
    }

    public static void requestAllPermissions(@NonNull Activity activity, String callingPackage) {
        ActivityCompat.requestPermissions(
                activity,
                requiredPermissions(callingPackage).toArray(new String[0]),
                InternalConstants.ALL_PERMISSIONS_REQUEST
        );
    }
}

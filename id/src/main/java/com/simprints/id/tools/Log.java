package com.simprints.id.tools;

import android.app.Activity;

import java.util.Locale;

@SuppressWarnings("unused")
public class Log {

    public static void d(Activity activity, String s) {
        android.util.Log.d(activity.getPackageName(),
                String.format(Locale.UK, "%s: %s", activity.getClass().getSimpleName(), s));
    }

}

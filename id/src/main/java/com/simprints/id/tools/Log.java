package com.simprints.id.tools;

import java.util.Locale;

@SuppressWarnings("unused")
public class Log {

    public static void d(Object o, String s) {
        android.util.Log.d(o.getClass().getSimpleName(),
                String.format(Locale.UK, "%s: %s", o.getClass().getSimpleName(), s));
    }
}

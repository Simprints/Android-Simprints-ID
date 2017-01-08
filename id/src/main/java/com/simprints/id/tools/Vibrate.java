package com.simprints.id.tools;

import android.content.Context;
import android.os.Vibrator;

public class Vibrate {
    public static void vibrate(Context context, long duration) {
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(duration);
    }
}

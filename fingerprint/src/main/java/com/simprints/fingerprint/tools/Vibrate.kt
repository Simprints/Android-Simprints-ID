package com.simprints.fingerprint.tools

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object Vibrate {

    fun vibrate(context: Context) {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).let {
            if (Build.VERSION.SDK_INT >= 26) {
                newVibrate(it)
            } else {
                oldVibrate(it)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun oldVibrate(vibrator: Vibrator) {
        vibrator.vibrate(VIBRATE_PERIOD_MILLIS)
    }

    @TargetApi(26)
    private fun newVibrate(vibrator: Vibrator) {
        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_PERIOD_MILLIS, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private const val VIBRATE_PERIOD_MILLIS = 100L
}

package com.simprints.id.tools

import android.content.Context
import android.os.Vibrator

object Vibrate {
    fun vibrate(context: Context) {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(100.toLong())
    }
}

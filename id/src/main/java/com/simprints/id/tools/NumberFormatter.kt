package com.simprints.id.tools

import android.icu.text.NumberFormat
import android.os.Build
import java.util.*

class NumberFormatter(private val locale: Locale) {

    fun getFormattedIntegerString(number: Int): String =
        if (Build.VERSION.SDK_INT >= 24) {
            NumberFormat.getInstance(locale).format(number)
        } else {
            String.format("%,d", number)
        }
}

package com.simprints.id.tools

import android.icu.text.NumberFormat
import android.os.Build
import java.util.*

class NumberFormatter {

    fun getFormattedIntegerString(number: Int, locale: Locale): String =
        if (Build.VERSION.SDK_INT >= 24) {
            NumberFormat.getInstance(locale).format(number)
        } else {
            String.format("%,d", number)
        }
}

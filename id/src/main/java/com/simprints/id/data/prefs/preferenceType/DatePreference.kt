package com.simprints.id.data.prefs.preferenceType

import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.logging.Simber
import java.util.*
import kotlin.reflect.KProperty

class DatePreference(private val prefs: ImprovedSharedPreferences,
                     private val key: String,
                     private val defValue: Date?) {

    companion object {
        private const val NULL_DATE: Long = -1
    }

    private var value: Date? by lazyVar {
        Simber.d("DatePreference read $key from Shared Preferences")
        val longTime: Long = prefs.getPrimitive(key, defValue?.time
            ?: NULL_DATE)
        if (longTime > NULL_DATE) {
            Date(longTime)
        } else {
            null
        }
    }

    @Synchronized
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Date? {
        Simber.d("PrimitivePreference.getValue $key")
        return value
    }

    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Date?) {
        Simber.d("PrimitivePreference.setValue $key")
        this.value = value
        Simber.d("PrimitivePreference write $key to Shared Preferences")
        prefs.edit()
            .putPrimitive(key, value?.time ?: -1)
            .apply()
    }
}

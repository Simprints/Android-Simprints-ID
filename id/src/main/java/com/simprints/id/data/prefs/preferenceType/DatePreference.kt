package com.simprints.id.data.prefs.preferenceType

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.delegates.lazyVar
import timber.log.Timber
import java.util.*
import kotlin.reflect.KProperty

class DatePreference(private val prefs: ImprovedSharedPreferences,
                     private val key: String,
                     private val defValue: Date?) {

    companion object {
        private const val NULL_DATE: Long = -1
    }

    private var value: Date? by lazyVar {
        Timber.d("DatePreference read $key from Shared Preferences")
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
        Timber.d("PrimitivePreference.getValue $key")
        return value
    }

    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Date?) {
        Timber.d("PrimitivePreference.setValue $key")
        this.value = value
        Timber.d("PrimitivePreference write $key to Shared Preferences")
        prefs.edit()
            .putPrimitive(key, value?.time ?: -1)
            .apply()
    }
}

package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import timber.log.Timber
import java.util.*
import kotlin.reflect.KProperty

class DatePreference(private val preferences: ImprovedSharedPreferences,
                     private val key: String,
                     private val defValue: Date?) {

    private var value: Date? by lazyVar {
        Timber.d("DatePreference read $key from Shared Preferences")
        val longTime: Long = preferences.getPrimitive(key, defValue?.time ?: -1)
         if (longTime > 0) {
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
        preferences.edit()
            .putPrimitive(key, value?.time ?: -1)
            .apply()
    }
}

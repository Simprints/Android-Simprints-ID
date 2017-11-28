package com.simprints.id.tools.delegates

import com.simprints.id.tools.delegations.sharedPreferences.ExtSharedPreferences
import timber.log.Timber
import kotlin.reflect.KProperty

/**
 * Delegate for cached, lazy, thread-safe access to shared preferences
 * @author etienne@simprints.com
 */
class Preference<T: Any>(private val prefs: ExtSharedPreferences,
                         private val key: String,
                         private val defValue: T) {

    private var initialized: Boolean = false
    private lateinit var field: T

    @Synchronized
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        Timber.d("Preference.getValue ${property.name}")
        if (!initialized) {
            field = prefs.getAny(key, defValue)
            initialized = true
        }
        return field
    }

    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        Timber.d("Preference.setValue ${property.name}")
        field = value
        initialized = true
        prefs.edit()
                .putAny(key, value)
                .apply()
    }
}

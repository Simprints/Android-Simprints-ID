package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import timber.log.Timber
import java.security.InvalidParameterException
import kotlin.reflect.KProperty

/**
 * Delegate to read/write Boolean, Float, Int, Long, and String values to Shared Preferences.
 *
 * Thread-safety: a property delegated to PrimitivePreference can be access concurrently
 * from different threads.
 *
 * Lazy-initialization: reads to Shared Preferences are performed not on instantiation but on first
 * access.
 *
 * Caching: the value of the property is cached to reduce the number of reads and writes to the
 * Shared Preferences: at most one read on first access, and one write per set.
 *
 * @author etienne@simprints.com
 */
class PrimitivePreference<T: Any>(private val preferences: ImprovedSharedPreferences,
                                  private val key: String,
                                  private val defValue: T) {

    init {
        when (defValue) {
            is Boolean, is Float, is Int, is Long, is String -> {}
            else -> throw InvalidParameterException("${defValue::class.java.simpleName} is not a primitive type. Use ComplexPreference instead.")
        }
    }

    private var value: T by lazyVar {
        Timber.d("PrimitivePreference read $key from Shared Preferences")
        preferences.getAny(key, defValue)
    }

    @Synchronized
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        Timber.d("PrimitivePreference.getValue $key")
        return value
    }

    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        Timber.d("PrimitivePreference.setValue $key")
        this.value = value
        Timber.d("PrimitivePreference write $key to Shared Preferences")
        preferences.edit().putAny(key, value).apply()
    }
}

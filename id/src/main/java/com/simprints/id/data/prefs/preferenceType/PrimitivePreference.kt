package com.simprints.id.data.prefs.preferenceType

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.exceptions.unexpected.NonPrimitiveTypeException
import isPrimitive
import kotlin.reflect.KProperty

/**
 * Delegate to read/write values of primitive types ([Byte], [Short], [Int], [Long], [Float], [Double],
 * [String] or [Boolean]) from the Shared Preferences.
 */
open class PrimitivePreference<T : Any>(private val prefs: ImprovedSharedPreferences,
                                        private val key: String,
                                        private val defValue: T) {

    init {
        if (!defValue.isPrimitive()) {
            throw NonPrimitiveTypeException.forTypeOf(defValue)
        }
    }

    protected var value: T
        get() = prefs.getPrimitive(key, defValue)
        set(value) {
            prefs.edit()
                .putPrimitive(key, value)
                .apply()
        }

    @Synchronized
    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    @Synchronized
    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

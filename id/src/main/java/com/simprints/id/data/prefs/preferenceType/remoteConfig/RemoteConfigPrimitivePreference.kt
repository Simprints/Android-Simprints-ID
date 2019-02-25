package com.simprints.id.data.prefs.preferenceType.remoteConfig

import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.exceptions.unexpected.NonPrimitiveTypeException
import kotlin.reflect.KProperty

open class RemoteConfigPrimitivePreference<T : Any>(prefs: ImprovedSharedPreferences,
                                                    private val remoteConfigWrapper: RemoteConfigWrapper,
                                                    private val key: String,
                                                    private val defValue: T)
    : PrimitivePreference<T>(prefs, key, defValue) {

    init {
        remoteConfigWrapper.prepareDefaultValue(key, defValue)
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        try {
            value = getRemoteConfigValue()
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getRemoteConfigValue(): T =
        (when (defValue) {
            is Boolean -> remoteConfigWrapper.getBoolean(key)
            is Long -> remoteConfigWrapper.getLong(key)
            is Int -> remoteConfigWrapper.getLong(key)?.toInt()
            is Short -> remoteConfigWrapper.getLong(key)?.toShort()
            is Double -> remoteConfigWrapper.getDouble(key)
            is Float -> remoteConfigWrapper.getDouble(key)?.toFloat()
            is String -> remoteConfigWrapper.getString(key)
            else -> throw NonPrimitiveTypeException.forTypeOf(defValue)
        } ?: defValue) as T
}

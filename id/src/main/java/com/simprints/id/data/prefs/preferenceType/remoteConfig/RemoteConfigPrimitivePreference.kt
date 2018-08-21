package com.simprints.id.data.prefs.preferenceType.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.exceptions.unsafe.NonPrimitiveTypeError
import kotlin.reflect.KProperty

open class RemoteConfigPrimitivePreference<T : Any>(prefs: ImprovedSharedPreferences,
                                                    private val remoteConfig: FirebaseRemoteConfig,
                                                    remoteConfigDefaults: MutableMap<String, Any>,
                                                    private val key: String,
                                                    private val defValue: T)
    : PrimitivePreference<T>(prefs, key, defValue) {

    init {
        remoteConfigDefaults[key] = defValue
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
        when (defValue) {
            is Boolean -> remoteConfig.getBoolean(key)
            is Long -> remoteConfig.getLong(key)
            is Int -> remoteConfig.getLong(key).toInt()
            is Short -> remoteConfig.getLong(key).toShort()
            is Double -> remoteConfig.getDouble(key)
            is Float -> remoteConfig.getDouble(key).toFloat()
            is String -> remoteConfig.getString(key)
            is ByteArray -> remoteConfig.getByteArray(key)
            else -> throw NonPrimitiveTypeError.forTypeOf(defValue)
        } as T
}

package com.simprints.id.data.prefs.preferenceType.remoteConfig

import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.ComplexPreference
import com.simprints.id.tools.serializers.Serializer
import java.security.InvalidParameterException
import kotlin.reflect.KProperty

open class RemoteConfigComplexPreference<T : Any>(prefs: ImprovedSharedPreferences,
                                                  private val remoteConfigWrapper: RemoteConfigWrapper,
                                                  private val key: String,
                                                  private val defValue: T,
                                                  serializer: Serializer<T>)
    : ComplexPreference<T>(prefs, key, defValue, serializer) {

    init {
        remoteConfigWrapper.prepareDefaultValue(key, serializedDefValue)
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        setSerializedPrefValueToRemoteConfigValue()
        return getAndDeserializePrefValue(thisRef, property)
    }

    private fun setSerializedPrefValueToRemoteConfigValue() {
        serializedValue = remoteConfigWrapper.getString(key)?: serializedDefValue
    }

    protected fun getAndDeserializePrefValue(thisRef: Any?, property: KProperty<*>): T =
        try {
            super.getValue(thisRef, property)
        } catch (e: InvalidParameterException) {
            // InvalidParameterException can be thrown if the deserialization fails
            defValue
        }
}

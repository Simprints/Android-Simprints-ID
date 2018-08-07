package com.simprints.id.data.prefs.preferenceType.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.ComplexPreference
import com.simprints.id.tools.serializers.Serializer
import java.security.InvalidParameterException
import kotlin.reflect.KProperty

open class RemoteConfigComplexPreference<T : Any>(prefs: ImprovedSharedPreferences,
                                                  private val remoteConfig: FirebaseRemoteConfig,
                                                  remoteConfigDefaults: MutableMap<String, Any>,
                                                  private val key: String,
                                                  private val defValue: T,
                                                  serializer: Serializer<T>)
    : ComplexPreference<T>(prefs, key, defValue, serializer) {

    init {
        remoteConfigDefaults[key] = serializedDefValue
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        serializedValue = remoteConfig.getString(key)
        return getBackingPreferenceValue(thisRef, property)
    }

    protected fun getBackingPreferenceValue(thisRef: Any?, property: KProperty<*>): T =
        try {
            super.getValue(thisRef, property)
        } catch (e: InvalidParameterException) {
            // InvalidParameterException can be thrown if the deserialization fails
            defValue
        }
}

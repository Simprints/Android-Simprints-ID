package com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPreference.Companion.IS_OVERRIDDEN_DEFAULT_VALUE
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPreference.Companion.IS_OVERRIDDEN_KEY_SUFFIX
import com.simprints.id.tools.serializers.Serializer
import kotlin.reflect.KProperty

class OverridableRemoteConfigComplexPreference<T : Any>(prefs: ImprovedSharedPreferences,
                                                        remoteConfig: FirebaseRemoteConfig,
                                                        remoteConfigDefaults: MutableMap<String, Any>,
                                                        key: String,
                                                        defValue: T,
                                                        serializer: Serializer<T>)
    : RemoteConfigComplexPreference<T>(prefs, remoteConfig, remoteConfigDefaults, key, defValue, serializer),
    OverridableRemoteConfigPreference {

    override val isOverriddenFlagKey = key + IS_OVERRIDDEN_KEY_SUFFIX
    override var isOverriddenFlag: Boolean by PrimitivePreference(prefs, isOverriddenFlagKey, IS_OVERRIDDEN_DEFAULT_VALUE)

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        if (isOverriddenFlag) {
            super.getPrefsValue(thisRef, property)
        } else {
            super.getValue(thisRef, property)
        }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        isOverriddenFlag = true
        super.setValue(thisRef, property, value)
    }
}

package com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable

import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigPrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPreference.Companion.IS_OVERRIDDEN_DEFAULT_VALUE
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPreference.Companion.IS_OVERRIDDEN_KEY_SUFFIX
import kotlin.reflect.KProperty

class OverridableRemoteConfigPrimitivePreference<T : Any>(prefs: ImprovedSharedPreferences,
                                                          remoteConfigWrapper: RemoteConfigWrapper,
                                                          key: String,
                                                          defValue: T)
    : RemoteConfigPrimitivePreference<T>(prefs, remoteConfigWrapper, key, defValue),
    OverridableRemoteConfigPreference {

    override val isOverriddenFlagKey = key + IS_OVERRIDDEN_KEY_SUFFIX
    override var isOverriddenFlag: Boolean by PrimitivePreference(prefs, isOverriddenFlagKey, IS_OVERRIDDEN_DEFAULT_VALUE)

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        if (isOverriddenFlag) {
            getPrefsValue()
        } else {
            getRemoteConfigValue()
        }

    private fun getPrefsValue() = value

    /**
     * Current override behaviour: if the user sets this preference, it becomes overridden
     */
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        isOverriddenFlag = true
        super.setValue(thisRef, property, value)
    }
}

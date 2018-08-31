package com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable

interface OverridableRemoteConfigPreference {

    val isOverriddenFlagKey: String
    var isOverriddenFlag: Boolean

    companion object {
        const val IS_OVERRIDDEN_KEY_SUFFIX = "_isOverridden"
        const val IS_OVERRIDDEN_DEFAULT_VALUE = false
    }
}

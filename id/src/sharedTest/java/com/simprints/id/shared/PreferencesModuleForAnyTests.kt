package com.simprints.id.shared

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.PreferencesModule
import com.simprints.id.domain.Constants
import com.simprints.id.shared.DependencyRule.RealRule
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier

open class PreferencesModuleForAnyTests(open var remoteConfigRule: DependencyRule = RealRule,
                                        open var settingsPreferencesManagerRule: DependencyRule = RealRule)
    : PreferencesModule() {

    override fun provideRemoteConfig(): FirebaseRemoteConfig =
        remoteConfigRule.resolveDependency { super.provideRemoteConfig() }

    override fun provideSettingsPreferencesManager(prefs: ImprovedSharedPreferences,
                                                   remoteConfig: FirebaseRemoteConfig,
                                                   fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                                                   groupSerializer: Serializer<Constants.GROUP>): SettingsPreferencesManager =
        settingsPreferencesManagerRule.resolveDependency { super.provideSettingsPreferencesManager(prefs, remoteConfig, fingerIdToBooleanSerializer, groupSerializer) }
}

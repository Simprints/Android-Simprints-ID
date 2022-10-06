package com.simprints.id.testtools.di

import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.PreferencesModule
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestPreferencesModule(
    var settingsPreferencesManagerRule: DependencyRule = RealRule
) : PreferencesModule() {

    override fun provideSettingsPreferencesManager(prefs: ImprovedSharedPreferences): SettingsPreferencesManager =
        settingsPreferencesManagerRule.resolveDependency {
            super.provideSettingsPreferencesManager(prefs)
        }

}

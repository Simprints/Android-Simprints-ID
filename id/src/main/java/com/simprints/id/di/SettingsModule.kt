package com.simprints.id.di

import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutViewModelFactory
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.secure.SignerManager
import dagger.Module
import dagger.Provides

@Module
open class SettingsModule {

    @Provides
    open fun provideSettingsAboutViewModelFactory(
        preferencesManager: PreferencesManager,
        signerManager: SignerManager,
        recentEventsManager: RecentEventsPreferencesManager
    ): SettingsAboutViewModelFactory {
        return SettingsAboutViewModelFactory(preferencesManager, signerManager, recentEventsManager)
    }

    @Provides
    open fun provideSettingsPreferenceViewModelFactory(
        preferencesManager: PreferencesManager,
        crashReportManager: CrashReportManager
    ): SettingsPreferenceViewModelFactory {
        return SettingsPreferenceViewModelFactory(preferencesManager, crashReportManager)
    }
}

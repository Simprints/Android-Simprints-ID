package com.simprints.id.di

import android.content.SharedPreferences
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.sharedpreferences.RecentEventsPreferencesManager
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_FILE_NAME
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_MODE
import com.simprints.id.Application
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.IdPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

// TODO: Remove after hilt migration
@DisableInstallInCheck
@Module
@JvmSuppressWildcards(false)
open class PreferencesModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        app: Application
    ): SharedPreferences = app.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    @Provides
    @Singleton
    fun provideImprovedSharedPreferences(
        basePrefs: SharedPreferences
    ): ImprovedSharedPreferences = ImprovedSharedPreferencesImpl(basePrefs)

    @Provides
    @Singleton
    fun provideScannerAttributesPreferencesManager(
        prefs: ImprovedSharedPreferences
    ): ScannerAttributesPreferencesManager = ScannerAttributesPreferencesManagerImpl(prefs)

    @Provides
    @Singleton
    open fun provideSettingsPreferencesManager(prefs: ImprovedSharedPreferences): SettingsPreferencesManager =
        SettingsPreferencesManagerImpl(prefs)

    @Provides
    @Singleton
    fun providePreferencesManager(
        settingsPreferencesManager: SettingsPreferencesManager,
        lastEventsPreferencesManager: RecentEventsPreferencesManager,
        app: Application
    ): PreferencesManager =
        IdPreferencesManagerImpl(settingsPreferencesManager, lastEventsPreferencesManager, app)

    @Provides
    @Singleton
    open fun provideIdPreferencesManager(
        settingsPreferencesManager: SettingsPreferencesManager,
        lastEventsPreferencesManager: RecentEventsPreferencesManager,
        app: Application
    ): IdPreferencesManager =
        IdPreferencesManagerImpl(settingsPreferencesManager, lastEventsPreferencesManager, app)

}

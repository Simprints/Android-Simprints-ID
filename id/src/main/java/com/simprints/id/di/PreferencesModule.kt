package com.simprints.id.di

import android.content.SharedPreferences
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_FILE_NAME
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_MODE
import com.simprints.id.Application
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.IdPreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
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
    fun providePreferencesManager(app: Application): PreferencesManager =
        IdPreferencesManagerImpl(app)

    @Provides
    @Singleton
    open fun provideIdPreferencesManager(app: Application): IdPreferencesManager =
        IdPreferencesManagerImpl(app)

}

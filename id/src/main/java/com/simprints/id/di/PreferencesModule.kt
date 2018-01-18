package com.simprints.id.di

import android.content.SharedPreferences
import com.simprints.id.Application
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManager
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManagerImpl
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManagerImpl
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManagerImpl
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.Location
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by fabiotuzza on 17/01/2018.
 */
@Module
@JvmSuppressWildcards(false)
open class PreferencesModule {

    @Provides @Singleton fun provideSharedPreferences(app: Application): SharedPreferences = app.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE)

    @Provides @Singleton fun provideImprovedSharedPreferences(basePrefs: SharedPreferences): ImprovedSharedPreferences = ImprovedSharedPreferencesImpl(basePrefs)

    @Provides @Singleton fun provideScannerAttributesPreferencesManager(prefs: ImprovedSharedPreferences): ScannerAttributesPreferencesManager = ScannerAttributesPreferencesManagerImpl(prefs)

    @Provides @Singleton open fun provideSessionParametersPreferencesManager(prefs: ImprovedSharedPreferences,
                                                                             @Named("CalloutActionSerializer") calloutActionSerializer: Serializer<CalloutAction>): SessionParametersPreferencesManager = SessionParametersPreferencesManagerImpl(prefs, calloutActionSerializer)

    @Provides @Singleton fun provideSessionTimestampsPreferencesManager(prefs: ImprovedSharedPreferences): SessionTimestampsPreferencesManager = SessionTimestampsPreferencesManagerImpl(prefs)

    @Provides @Singleton fun provideSessionStatePreferencesManager(prefs: ImprovedSharedPreferences,
                                                                   scannerAttributesPreferencesManager: ScannerAttributesPreferencesManager,
                                                                   sessionParametersPreferencesManager: SessionParametersPreferencesManager,
                                                                   sessionTimestampsPreferencesManager: SessionTimestampsPreferencesManager,
                                                                   @Named("LocationSerializer") locationSerializer: Serializer<Location>): SessionStatePreferencesManager =

                                                                        SessionStatePreferencesManagerImpl(prefs,
                                                                            scannerAttributesPreferencesManager,
                                                                            sessionParametersPreferencesManager,
                                                                            sessionTimestampsPreferencesManager,
                                                                            locationSerializer)

    @Provides @Singleton fun provideSettingsPreferencesManager(prefs: ImprovedSharedPreferences,
                                                               @Named("FingerIdToBooleanSerializer") fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                                                               @Named("GroupSerializer") groupSerializer: Serializer<Constants.GROUP>): SettingsPreferencesManager = SettingsPreferencesManagerImpl(prefs, fingerIdToBooleanSerializer, groupSerializer)


    @Provides @Singleton fun providePreferencesManager(sessionStatePreferencesManager: SessionStatePreferencesManager,
                                                       settingsPreferencesManager: SettingsPreferencesManager): PreferencesManager =
        PreferencesManagerImpl(sessionStatePreferencesManager, settingsPreferencesManager)

}

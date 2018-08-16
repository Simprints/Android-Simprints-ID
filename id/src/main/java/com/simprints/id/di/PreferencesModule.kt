package com.simprints.id.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.Application
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
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
import com.simprints.id.data.prefs.sync.SyncPreferencesManager
import com.simprints.id.data.prefs.sync.SyncPreferencesManagerImpl
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Location
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


@Module
@JvmSuppressWildcards(false)
open class PreferencesModule {

    @Provides @Singleton open fun provideRemoteConfig(): FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    @Provides @Singleton fun provideRemoteConfigFetcher(remoteConfig: FirebaseRemoteConfig): RemoteConfigFetcher = RemoteConfigFetcher(remoteConfig)

    @Provides @Singleton fun provideSharedPreferences(app: Application): SharedPreferences = app.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE)

    @Provides @Singleton fun provideImprovedSharedPreferences(basePrefs: SharedPreferences): ImprovedSharedPreferences = ImprovedSharedPreferencesImpl(basePrefs)

    @Provides @Singleton fun provideScannerAttributesPreferencesManager(prefs: ImprovedSharedPreferences): ScannerAttributesPreferencesManager = ScannerAttributesPreferencesManagerImpl(prefs)

    @Provides @Singleton fun provideSessionParametersPreferencesManager(prefs: ImprovedSharedPreferences,
                                                                             @Named("CalloutActionSerializer") calloutActionSerializer: Serializer<CalloutAction>): SessionParametersPreferencesManager = SessionParametersPreferencesManagerImpl(prefs, calloutActionSerializer)

    @Provides @Singleton fun provideSessionTimestampsPreferencesManager(prefs: ImprovedSharedPreferences): SessionTimestampsPreferencesManager = SessionTimestampsPreferencesManagerImpl(prefs)

    @Provides @Singleton fun provideSessionStatePreferencesManager(ctx: Context,
                                                                   prefs: ImprovedSharedPreferences,
                                                                   scannerAttributesPreferencesManager: ScannerAttributesPreferencesManager,
                                                                   sessionParametersPreferencesManager: SessionParametersPreferencesManager,
                                                                   sessionTimestampsPreferencesManager: SessionTimestampsPreferencesManager,
                                                                   @Named("LocationSerializer") locationSerializer: Serializer<Location>): SessionStatePreferencesManager =

                                                                        SessionStatePreferencesManagerImpl(
                                                                            ctx,
                                                                            prefs,
                                                                            scannerAttributesPreferencesManager,
                                                                            sessionParametersPreferencesManager,
                                                                            sessionTimestampsPreferencesManager,
                                                                            locationSerializer)

    @Provides @Singleton open fun provideSettingsPreferencesManager(prefs: ImprovedSharedPreferences,
                                                               remoteConfig: FirebaseRemoteConfig,
                                                               @Named("FingerIdToBooleanSerializer") fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                                                               @Named("GroupSerializer") groupSerializer: Serializer<Constants.GROUP>): SettingsPreferencesManager = SettingsPreferencesManagerImpl(prefs, remoteConfig, fingerIdToBooleanSerializer, groupSerializer)

    @Provides @Singleton fun provideSyncPreferencesManager(prefs: ImprovedSharedPreferences): SyncPreferencesManager = SyncPreferencesManagerImpl(prefs)

    @Provides @Singleton fun providePreferencesManager(sessionStatePreferencesManager: SessionStatePreferencesManager,
                                                       settingsPreferencesManager: SettingsPreferencesManager,
                                                       lastEventsPreferencesManager: RecentEventsPreferencesManager,
                                                       syncPreferencesManager: SyncPreferencesManager,
                                                       app: Application): PreferencesManager =
        PreferencesManagerImpl(sessionStatePreferencesManager, settingsPreferencesManager, lastEventsPreferencesManager, syncPreferencesManager, app)
}

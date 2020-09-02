package com.simprints.id.di

import android.content.SharedPreferences
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_FILE_NAME
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_MODE
import com.simprints.id.Application
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.RemoteConfigFetcher
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting
import com.simprints.id.tools.serializers.Serializer
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
@JvmSuppressWildcards(false)
open class PreferencesModule {

    @Provides
    @Singleton
    open fun provideRemoteConfig(): FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    @Provides
    @Singleton
    fun provideRemoteConfigFetcher(
        remoteConfig: FirebaseRemoteConfig
    ): RemoteConfigFetcher = RemoteConfigFetcher(remoteConfig)

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
    fun provideRemoteConfigWrapper(
        remoteConfig: FirebaseRemoteConfig, prefs: ImprovedSharedPreferences
    ): RemoteConfigWrapper = RemoteConfigWrapper(remoteConfig, prefs)

    @Provides
    @Singleton
    fun provideScannerAttributesPreferencesManager(
        prefs: ImprovedSharedPreferences
    ): ScannerAttributesPreferencesManager = ScannerAttributesPreferencesManagerImpl(prefs)

    @Provides
    @Singleton
    open fun provideSettingsPreferencesManager(
        prefs: ImprovedSharedPreferences,
        remoteConfigWrapper: RemoteConfigWrapper,
        @Named("GroupSerializer") groupSerializer: Serializer<GROUP>,
        @Named("LanguagesStringArraySerializer") languagesStringArraySerializer: Serializer<Array<String>>,
        @Named("ModuleIdOptionsStringSetSerializer") moduleIdOptionsStringSetSerializer: Serializer<Set<String>>,
        @Named("PeopleDownSyncSettingSerializer") eventDownSyncSettingSerializer: Serializer<EventDownSyncSetting>,
        @Named("ModalitiesSerializer") modalitiesSerializer: Serializer<List<Modality>>,
        @Named("CaptureFingerprintStrategySerializer") captureFingerprintStrategySerializer: Serializer<CaptureFingerprintStrategy>,
        @Named("SaveFingerprintImagesStrategySerializer") saveFingerprintImagesStrategySerializer: Serializer<SaveFingerprintImagesStrategy>,
        @Named("ScannerGenerationsSerializer") scannerGenerationsSerializer: Serializer<List<ScannerGeneration>>,
        @Named("FingerprintsToCollectSerializer") fingerprintsToCollectSerializer: Serializer<List<FingerIdentifier>>
    ): SettingsPreferencesManager = SettingsPreferencesManagerImpl(
        prefs,
        remoteConfigWrapper,
        groupSerializer,
        modalitiesSerializer,
        languagesStringArraySerializer,
        moduleIdOptionsStringSetSerializer,
        eventDownSyncSettingSerializer,
        captureFingerprintStrategySerializer,
        saveFingerprintImagesStrategySerializer,
        scannerGenerationsSerializer,
        fingerprintsToCollectSerializer
    )

    @Provides
    @Singleton
    fun providePreferencesManager(settingsPreferencesManager: SettingsPreferencesManager,
                                  lastEventsPreferencesManager: RecentEventsPreferencesManager,
                                  app: Application): PreferencesManager =
        PreferencesManagerImpl(settingsPreferencesManager, lastEventsPreferencesManager, app)

}

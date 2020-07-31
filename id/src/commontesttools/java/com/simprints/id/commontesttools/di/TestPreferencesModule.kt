package com.simprints.id.commontesttools.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.data.prefs.settings.fingerprint.serializers.FingerprintsToCollectSerializer
import com.simprints.id.di.PreferencesModule
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting
import com.simprints.id.tools.serializers.Serializer
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestPreferencesModule(
    var remoteConfigRule: DependencyRule = RealRule,
    var settingsPreferencesManagerRule: DependencyRule = RealRule
) : PreferencesModule() {

    override fun provideRemoteConfig(): FirebaseRemoteConfig = remoteConfigRule.resolveDependency {
        super.provideRemoteConfig()
    }

    override fun provideSettingsPreferencesManager(
        prefs: ImprovedSharedPreferences,
        remoteConfigWrapper: RemoteConfigWrapper,
        fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
        groupSerializer: Serializer<GROUP>,
        languagesStringArraySerializer: Serializer<Array<String>>,
        moduleIdOptionsStringSetSerializer: Serializer<Set<String>>,
        subjectsDownSyncSettingSerializer: Serializer<SubjectsDownSyncSetting>,
        modalitiesSerializer: Serializer<List<Modality>>,
        captureFingerprintStrategySerializer: Serializer<CaptureFingerprintStrategy>,
        saveFingerprintImagesStrategySerializer: Serializer<SaveFingerprintImagesStrategy>,
        scannerGenerationsSerializer: Serializer<List<ScannerGeneration>>,
        fingerprintsToCollectSerializer: Serializer<List<FingerIdentifier>>
    ): SettingsPreferencesManager = settingsPreferencesManagerRule.resolveDependency {
        super.provideSettingsPreferencesManager(
            prefs,
            remoteConfigWrapper,
            fingerIdToBooleanSerializer,
            groupSerializer,
            languagesStringArraySerializer,
            moduleIdOptionsStringSetSerializer,
            subjectsDownSyncSettingSerializer,
            modalitiesSerializer,
            captureFingerprintStrategySerializer,
            saveFingerprintImagesStrategySerializer,
            scannerGenerationsSerializer,
            fingerprintsToCollectSerializer
        )
    }

}

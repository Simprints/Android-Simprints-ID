package com.simprints.id.commontesttools.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.di.PreferencesModule
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting
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
        groupSerializer: Serializer<GROUP>,
        languagesStringArraySerializer: Serializer<Array<String>>,
        moduleIdOptionsStringSetSerializer: Serializer<Set<String>>,
        eventDownSyncSettingSerializer: Serializer<EventDownSyncSetting>,
        syncDestinationSettingSerializer: Serializer<List<SyncDestinationSetting>>,
        modalitiesSerializer: Serializer<List<Modality>>,
        captureFingerprintStrategySerializer: Serializer<CaptureFingerprintStrategy>,
        saveFingerprintImagesStrategySerializer: Serializer<SaveFingerprintImagesStrategy>,
        scannerGenerationsSerializer: Serializer<List<ScannerGeneration>>,
        fingerprintsToCollectSerializer: Serializer<List<FingerIdentifier>>,
        fingerprintConfidenceThresholdsSerializer: Serializer<Map<FingerprintConfidenceThresholds, Int>>,
        faceConfidenceThresholdsSerializer: Serializer<Map<FaceConfidenceThresholds, Int>>
    ): SettingsPreferencesManager = settingsPreferencesManagerRule.resolveDependency {
        super.provideSettingsPreferencesManager(
            prefs,
            remoteConfigWrapper,
            groupSerializer,
            languagesStringArraySerializer,
            moduleIdOptionsStringSetSerializer,
            eventDownSyncSettingSerializer,
            syncDestinationSettingSerializer,
            modalitiesSerializer,
            captureFingerprintStrategySerializer,
            saveFingerprintImagesStrategySerializer,
            scannerGenerationsSerializer,
            fingerprintsToCollectSerializer,
            fingerprintConfidenceThresholdsSerializer,
            faceConfidenceThresholdsSerializer
        )
    }

}

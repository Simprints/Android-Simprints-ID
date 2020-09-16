package com.simprints.id.di

import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.data.prefs.settings.fingerprint.serializers.FingerprintsToCollectSerializer
import com.simprints.id.data.prefs.settings.fingerprint.serializers.ScannerGenerationsSerializer
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting
import com.simprints.id.tools.json.SimJsonHelper
import com.simprints.id.tools.serializers.*
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


@Module
@JvmSuppressWildcards(false)
class SerializerModule {

    @Provides
    @Singleton
    @Named("BooleanSerializer")
    fun provideBooleanSerializer(): Serializer<Boolean> = BooleanSerializer()

    @Provides
    @Singleton
    @Named("IntSerializer")
    fun provideIntegerSerializer(): Serializer<Int> = IntegerSerializer()

    @Provides
    @Singleton
    @Named("FingerIdentifierSerializer")
    fun provideFingerIdentifierSerializer(): Serializer<FingerIdentifier> = EnumSerializer(FingerIdentifier::class.java)

    @Provides
    @Singleton
    @Named("GroupSerializer")
    fun provideGroupSerializer(): Serializer<GROUP> = EnumSerializer(GROUP::class.java)

    @Provides
    @Singleton
    @Named("PeopleDownSyncSettingSerializer")
    fun providePeopleDownSyncSettingSerializer(): Serializer<EventDownSyncSetting> = EnumSerializer(EventDownSyncSetting::class.java)

    @Provides
    @Singleton
    @Named("FingerprintConfidenceSerializer")
    fun provideFingerprintConfidenceSerializer(): Serializer<FingerprintConfidenceThresholds> =
        EnumSerializer(FingerprintConfidenceThresholds::class.java)

    @Provides
    @Singleton
    @Named("FaceConfidenceSerializer")
    fun provideFaceConfidenceSerializer(): Serializer<FaceConfidenceThresholds> =
        EnumSerializer(FaceConfidenceThresholds::class.java)

    @Provides
    @Singleton
    @Named("ModalitiesSerializer")
    fun provideModalSerializer(): Serializer<List<Modality>> = ModalitiesListSerializer()

    @Provides
    @Singleton
    fun provideJsonHelper(): JsonHelper = JsonHelper()

    @Provides
    @Singleton
    @Named("FingerprintConfidenceThresholdsSerializer")
    fun provideFingerprintConfidenceThresholdsSerializer(
        @Named("FingerprintConfidenceSerializer") fingerprintConfidenceSerializer: Serializer<FingerprintConfidenceThresholds>,
        @Named("IntSerializer") intSerializer: Serializer<Int>,
        gson: Gson
    ): Serializer<Map<FingerprintConfidenceThresholds, Int>> = MapSerializer(fingerprintConfidenceSerializer, intSerializer, gson)

    @Provides
    @Singleton
    @Named("FaceConfidenceThresholdsSerializer")
    fun provideFaceConfidenceThresholdsSerializer(
        @Named("FaceConfidenceSerializer") faceConfidenceThresholdsSerializer: Serializer<FaceConfidenceThresholds>,
        @Named("IntSerializer") intSerializer: Serializer<Int>,
        gson: Gson
    ): Serializer<Map<FaceConfidenceThresholds, Int>> = MapSerializer(faceConfidenceThresholdsSerializer, intSerializer, gson)

    @Provides
    @Singleton
    @Named("LanguagesStringArraySerializer")
    fun provideLanguagesStringArraySerializer(): Serializer<Array<String>> = LanguagesStringArraySerializer()

    @Provides
    @Singleton
    @Named("ModuleIdOptionsStringSetSerializer")
    fun provideModuleIdOptionsStringSetSerializer(): Serializer<Set<String>> = ModuleIdOptionsStringSetSerializer()

    @Provides
    @Singleton
    @Named("CaptureFingerprintStrategySerializer")
    fun provideCaptureFingerprintStrategySerializer(): Serializer<CaptureFingerprintStrategy> = EnumSerializer(CaptureFingerprintStrategy::class.java)

    @Provides
    @Singleton
    @Named("SaveFingerprintImagesStrategySerializer")
    fun provideSaveFingerprintImagesStrategySerializer(): Serializer<SaveFingerprintImagesStrategy> = EnumSerializer(SaveFingerprintImagesStrategy::class.java)

    @Provides
    @Singleton
    @Named("ScannerGenerationsSerializer")
    fun provideScannerGenerationsSerializer(): Serializer<List<ScannerGeneration>> = ScannerGenerationsSerializer()

    @Provides
    @Singleton
    @Named("FingerprintsToCollectSerializer")
    fun provideFingerprintsToCollectSerializer(): Serializer<List<FingerIdentifier>> = FingerprintsToCollectSerializer()
}

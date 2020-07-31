package com.simprints.id.di

import com.google.gson.Gson
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.data.prefs.settings.fingerprint.serializers.FingerprintsToCollectSerializer
import com.simprints.id.data.prefs.settings.fingerprint.serializers.ScannerGenerationsSerializer
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
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
    @Named("FingerIdentifierSerializer")
    fun provideFingerIdentifierSerializer(): Serializer<FingerIdentifier> = EnumSerializer(FingerIdentifier::class.java)

    @Provides
    @Singleton
    @Named("GroupSerializer")
    fun provideGroupSerializer(): Serializer<GROUP> = EnumSerializer(GROUP::class.java)

    @Provides
    @Singleton
    @Named("PeopleDownSyncSettingSerializer")
    fun providePeopleDownSyncSettingSerializer(): Serializer<SubjectsDownSyncSetting> = EnumSerializer(SubjectsDownSyncSetting::class.java)

    @Provides
    @Singleton
    @Named("ModalitiesSerializer")
    fun provideModalSerializer(): Serializer<List<Modality>> = ModalitiesListSerializer()

    @Provides
    @Singleton
    fun provideGson(): Gson = SimJsonHelper.gson

    @Provides
    @Singleton
    @Named("FingerIdToBooleanSerializer")
    fun provideFingerIdToBooleanSerializer(
        @Named("FingerIdentifierSerializer") fingerIdentifierSerializer: Serializer<FingerIdentifier>,
        @Named("BooleanSerializer") booleanSerializer: Serializer<Boolean>,
        gson: Gson
    ): Serializer<Map<FingerIdentifier, Boolean>> = MapSerializer(fingerIdentifierSerializer, booleanSerializer, gson)

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

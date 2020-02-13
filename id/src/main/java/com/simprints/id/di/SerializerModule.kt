package com.simprints.id.di

import com.google.gson.Gson
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncTrigger
import com.simprints.id.tools.serializers.*
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


@Module
@JvmSuppressWildcards(false)
class SerializerModule {

    @Provides @Singleton @Named("BooleanSerializer") fun provideBooleanSerializer(): Serializer<Boolean> = BooleanSerializer()
    @Provides @Singleton @Named("FingerIdentifierSerializer") fun provideFingerIdentifierSerializer(): Serializer<FingerIdentifier> = EnumSerializer(FingerIdentifier::class.java)
    @Provides @Singleton @Named("GroupSerializer") fun provideGroupSerializer(): Serializer<GROUP> = EnumSerializer(GROUP::class.java)
    @Provides @Singleton @Named("ModalitiesSerializer") fun provideModalSerializer(): Serializer<List<Modality>> = ModalitiesListSerializer()
    @Provides @Singleton @Named("PeopleDownSyncTriggerSerializer") fun providePeopleDownSyncTriggerSerializer(): Serializer<PeopleDownSyncTrigger> = EnumSerializer(PeopleDownSyncTrigger::class.java)
    @Provides @Singleton fun provideGson(): Gson = Gson()

    @Provides @Singleton @Named("FingerIdToBooleanSerializer") fun provideFingerIdToBooleanSerializer(@Named("FingerIdentifierSerializer") fingerIdentifierSerializer: Serializer<FingerIdentifier>,
                                                                                                      @Named("BooleanSerializer") booleanSerializer: Serializer<Boolean>,
                                                                                                      gson: Gson): Serializer<Map<FingerIdentifier, Boolean>> = MapSerializer(fingerIdentifierSerializer, booleanSerializer, gson)
    @Provides @Singleton @Named("PeopleDownSyncTriggerToBooleanSerializer") fun providePeopleDownSyncTriggerToBooleanSerializer(
        @Named("PeopleDownSyncTriggerSerializer") peopleDownSyncSyncTriggerSerializer: Serializer<PeopleDownSyncTrigger>,
        @Named("BooleanSerializer") booleanSerializer: Serializer<Boolean>,
        gson: Gson): Serializer<Map<PeopleDownSyncTrigger, Boolean>> = MapSerializer(peopleDownSyncSyncTriggerSerializer, booleanSerializer, gson)

    @Provides @Singleton @Named("LanguagesStringArraySerializer") fun provideLanguagesStringArraySerializer(): Serializer<Array<String>> = LanguagesStringArraySerializer()
    @Provides @Singleton @Named("ModuleIdOptionsStringSetSerializer") fun provideModuleIdOptionsStringSetSerializer(): Serializer<Set<String>> = ModuleIdOptionsStringSetSerializer()
}

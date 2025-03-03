package com.simprints.infra.enrolment.records.realm.store

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RealmModule {
    @Binds
    internal abstract fun bindRealmWrapper(impl: RealmWrapperImpl): RealmWrapper
}

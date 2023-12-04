package com.simprints.infra.enrolment.records.store

import com.simprints.infra.enrolment.records.store.local.EnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.store.local.EnrolmentRecordLocalDataSourceImpl
import com.simprints.infra.enrolment.records.store.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.enrolment.records.store.remote.EnrolmentRecordRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EnrolmentRecordsStoreModule {

    @Binds
    internal abstract fun bindEnrolmentRecordRepository(impl: EnrolmentRecordRepositoryImpl): EnrolmentRecordRepository

    @Binds
    internal abstract fun bindEnrolmentRecordLocalDataSource(impl: EnrolmentRecordLocalDataSourceImpl): EnrolmentRecordLocalDataSource

    @Binds
    internal abstract fun bindEnrolmentRecordRemoteDataSource(impl: EnrolmentRecordRemoteDataSourceImpl): EnrolmentRecordRemoteDataSource
}

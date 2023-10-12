package com.simprints.infra.enrolment.records.store

import com.simprints.infra.enrolment.records.store.local.SubjectLocalDataSource
import com.simprints.infra.enrolment.records.store.local.SubjectLocalDataSourceImpl
import com.simprints.infra.enrolment.records.store.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.enrolment.records.store.remote.EnrolmentRecordRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EnrolmentRecordsModule {

    @Binds
    internal abstract fun bindEnrolmentRecordRepository(impl: EnrolmentRecordRepositoryImpl): EnrolmentRecordRepository

    @Binds
    internal abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository

    @Binds
    internal abstract fun bindSubjectLocalDataSource(impl: SubjectLocalDataSourceImpl): SubjectLocalDataSource

    @Binds
    internal abstract fun bindEnrolmentRecordRemoteDataSource(impl: EnrolmentRecordRemoteDataSourceImpl): EnrolmentRecordRemoteDataSource
}

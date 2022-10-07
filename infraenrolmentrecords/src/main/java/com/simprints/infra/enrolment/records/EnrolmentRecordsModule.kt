package com.simprints.infra.enrolment.records

import com.simprints.infra.enrolment.records.domain.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.domain.EnrolmentRecordRepositoryImpl
import com.simprints.infra.enrolment.records.domain.SubjectRepository
import com.simprints.infra.enrolment.records.domain.SubjectRepositoryImpl
import com.simprints.infra.enrolment.records.local.SubjectLocalDataSource
import com.simprints.infra.enrolment.records.local.SubjectLocalDataSourceImpl
import com.simprints.infra.enrolment.records.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.enrolment.records.remote.EnrolmentRecordRemoteDataSourceImpl
import com.simprints.infra.enrolment.records.worker.EnrolmentRecordScheduler
import com.simprints.infra.enrolment.records.worker.EnrolmentRecordSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class EnrolmentRecordsModule {

    @Binds
    internal abstract fun bindEnrolmentRecordManager(impl: EnrolmentRecordManagerImpl): EnrolmentRecordManager

    @Binds
    internal abstract fun bindEnrolmentRecordRepository(impl: EnrolmentRecordRepositoryImpl): EnrolmentRecordRepository

    @Binds
    internal abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository

    @Binds
    internal abstract fun bindSubjectLocalDataSource(impl: SubjectLocalDataSourceImpl): SubjectLocalDataSource

    @Binds
    internal abstract fun bindEnrolmentRecordRemoteDataSource(impl: EnrolmentRecordRemoteDataSourceImpl): EnrolmentRecordRemoteDataSource

    @Binds
    internal abstract fun bindEnrolmentRecordScheduler(impl: EnrolmentRecordSchedulerImpl): EnrolmentRecordScheduler
}

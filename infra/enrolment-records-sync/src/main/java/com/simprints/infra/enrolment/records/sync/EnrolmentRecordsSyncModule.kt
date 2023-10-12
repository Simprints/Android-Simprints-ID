package com.simprints.infra.enrolment.records

import com.simprints.infra.enrolment.records.worker.EnrolmentRecordScheduler
import com.simprints.infra.enrolment.records.worker.EnrolmentRecordSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EnrolmentRecordsSyncModule {

    @Binds
    internal abstract fun bindEnrolmentRecordManager(impl: EnrolmentRecordManagerImpl): EnrolmentRecordManager

    @Binds
    internal abstract fun bindEnrolmentRecordScheduler(impl: EnrolmentRecordSchedulerImpl): EnrolmentRecordScheduler
}

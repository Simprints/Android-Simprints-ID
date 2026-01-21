package com.simprints.infra.enrolment.records.repository

import android.content.Context
import com.simprints.core.AvailableProcessors
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.ExtractCommCareCaseIdUseCase
import com.simprints.infra.enrolment.records.repository.commcare.CommCareCandidateRecordDataSource
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSourceImpl
import com.simprints.infra.enrolment.records.repository.usecases.CompareImplicitTokenizedStringsUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier

@Module(
    includes = [
        IdentityDataSourceModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
abstract class EnrolmentRecordsStoreModule {
    @Binds
    internal abstract fun bindEnrolmentRecordRepository(impl: EnrolmentRecordRepositoryImpl): EnrolmentRecordRepository

    @Binds
    internal abstract fun bindEnrolmentRecordRemoteDataSource(impl: EnrolmentRecordRemoteDataSourceImpl): EnrolmentRecordRemoteDataSource
}

@Module
@InstallIn(SingletonComponent::class)
class IdentityDataSourceModule {
    @CommCareDataSource
    @Provides
    fun provideCommCareIdentityDataSource(
        timeHelper: TimeHelper,
        encoder: EncodingUtils,
        compareImplicitTokenizedStringsUseCase: CompareImplicitTokenizedStringsUseCase,
        extractCommCareCaseIdUseCase: ExtractCommCareCaseIdUseCase,
        @AvailableProcessors availableProcessors: Int,
        @ApplicationContext context: Context,
        @DispatcherIO dispatcher: CoroutineDispatcher,
    ): CandidateRecordDataSource = CommCareCandidateRecordDataSource(
        timeHelper = timeHelper,
        encoder = encoder,
        compareImplicitTokenizedStringsUseCase = compareImplicitTokenizedStringsUseCase,
        extractCommCareCaseId = extractCommCareCaseIdUseCase,
        availableProcessors = availableProcessors,
        context = context,
        dispatcher = dispatcher,
    )

    @EnrolmentBatchSize
    @Provides
    fun provideBatchSize(): Int = BATCH_SIZE

    companion object {
        const val BATCH_SIZE = 80
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommCareDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EnrolmentBatchSize

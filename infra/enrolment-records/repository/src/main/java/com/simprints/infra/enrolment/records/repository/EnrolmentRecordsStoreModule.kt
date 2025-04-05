package com.simprints.infra.enrolment.records.repository

import android.content.Context
import com.simprints.core.AvailableProcessors
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepositoryImpl.Companion.BATCH_SIZE
import com.simprints.infra.enrolment.records.repository.commcare.CommCareIdentityDataSource
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
        encoder: EncodingUtils,
        jsonHelper: JsonHelper,
        compareImplicitTokenizedStringsUseCase: CompareImplicitTokenizedStringsUseCase,
        @AvailableProcessors availableProcessors: Int,
        @ApplicationContext context: Context,
        @DispatcherIO dispatcher: CoroutineDispatcher,
    ): IdentityDataSource = CommCareIdentityDataSource(
        encoder = encoder,
        jsonHelper = jsonHelper,
        compareImplicitTokenizedStringsUseCase = compareImplicitTokenizedStringsUseCase,
        availableProcessors = availableProcessors,
        context = context,
        dispatcher = dispatcher,
    )

    @Provides
    fun provideBatchSize(): Int = BATCH_SIZE
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommCareDataSource

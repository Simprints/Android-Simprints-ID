package com.simprints.infra.enrolment.records.repository

import android.content.Context
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.commcare.CommCareIdentityDataSource
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.repository.local.EnrolmentRecordLocalDataSourceImpl
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
    internal abstract fun bindEnrolmentRecordLocalDataSource(impl: EnrolmentRecordLocalDataSourceImpl): EnrolmentRecordLocalDataSource

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
        @ApplicationContext context: Context,
        @DispatcherIO dispatcher: CoroutineDispatcher,
    ): IdentityDataSource = CommCareIdentityDataSource(
        encoder = encoder,
        jsonHelper = jsonHelper,
        compareImplicitTokenizedStringsUseCase = compareImplicitTokenizedStringsUseCase,
        context = context,
        dispatcher = dispatcher,
    )
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommCareDataSource

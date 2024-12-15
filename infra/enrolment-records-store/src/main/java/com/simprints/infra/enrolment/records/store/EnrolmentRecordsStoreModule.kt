package com.simprints.infra.enrolment.records.store

import android.content.Context
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.store.commcare.CommCareIdentityDataSource
import com.simprints.infra.enrolment.records.store.local.EnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.store.local.EnrolmentRecordLocalDataSourceImpl
import com.simprints.infra.enrolment.records.store.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.enrolment.records.store.remote.EnrolmentRecordRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        @ApplicationContext context: Context,
    ): IdentityDataSource = CommCareIdentityDataSource(
        encoder = encoder,
        jsonHelper = jsonHelper,
        context = context,
    )
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommCareDataSource

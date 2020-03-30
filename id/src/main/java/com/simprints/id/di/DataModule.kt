package com.simprints.id.di

import android.content.Context
import com.simprints.core.images.repository.ImageRepository
import com.simprints.core.images.repository.ImageRepositoryImpl
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.PersonRepositoryImpl
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSourceImpl
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSourceImpl
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.ProjectRepositoryImpl
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSourceImpl
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSourceImpl
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.secure.BaseUrlProvider
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    @Singleton
    open fun providePersonRemoteDataSource(
        remoteDbManager: RemoteDbManager,
        simApiClientFactory: SimApiClientFactory,
        baseUrlProvider: BaseUrlProvider
    ): PersonRemoteDataSource = PersonRemoteDataSourceImpl(
        remoteDbManager,
        simApiClientFactory,
        baseUrlProvider
    )

    @Provides
    @FlowPreview
    open fun provideProjectLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager
    ): ProjectLocalDataSource = ProjectLocalDataSourceImpl(
        ctx,
        secureLocalDbKeyProvider,
        loginInfoManager
    )

    @Provides
    @Singleton
    open fun provideProjectRemoteDataSource(
        remoteDbManager: RemoteDbManager,
        simApiClientFactory: SimApiClientFactory,
        baseUrlProvider: BaseUrlProvider
    ): ProjectRemoteDataSource = ProjectRemoteDataSourceImpl(
        remoteDbManager,
        simApiClientFactory,
        baseUrlProvider
    )

    @Provides
    open fun provideProjectRepository(
        projectLocalDataSource: ProjectLocalDataSource,
        projectRemoteDataSource: ProjectRemoteDataSource,
        baseUrlProvider: BaseUrlProvider
    ): ProjectRepository = ProjectRepositoryImpl(
        projectLocalDataSource,
        projectRemoteDataSource,
        baseUrlProvider
    )

    @Provides
    open fun providePersonRepository(
        personRemoteDataSource: PersonRemoteDataSource,
        personLocalDataSource: PersonLocalDataSource,
        peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
        peopleUpSyncExecutor: PeopleUpSyncExecutor
    ): PersonRepository = PersonRepositoryImpl(
        personRemoteDataSource,
        personLocalDataSource,
        peopleDownSyncScopeRepository,
        peopleUpSyncExecutor
    )

    @Provides
    @Singleton
    @FlowPreview
    open fun providePersonLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager
    ): PersonLocalDataSource = PersonLocalDataSourceImpl(
        ctx,
        secureLocalDbKeyProvider,
        loginInfoManager
    )

    @Provides
    open fun provideFingerprintRecordLocalDataSource(
        personLocalDataSource: PersonLocalDataSource
    ): FingerprintIdentityLocalDataSource = personLocalDataSource

    @Provides
    open fun provideImageRepository(
        context: Context
    ): ImageRepository = ImageRepositoryImpl(context)

}

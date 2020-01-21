package com.simprints.id.di

import android.content.Context
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.image.local.ImageLocalDataSource
import com.simprints.id.data.db.image.local.ImageLocalDataSourceImpl
import com.simprints.id.data.db.image.remote.ImageRemoteDataSource
import com.simprints.id.data.db.image.remote.ImageRemoteDataSourceImpl
import com.simprints.id.data.db.image.repository.ImageRepository
import com.simprints.id.data.db.image.repository.ImageRepositoryImpl
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
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    @Singleton
    open fun providePersonRemoteDataSource(remoteDbManager: RemoteDbManager): PersonRemoteDataSource = PersonRemoteDataSourceImpl(remoteDbManager)

    @Provides
    open fun provideProjectLocalDataSource(ctx: Context,
                                           secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
                                           loginInfoManager: LoginInfoManager): ProjectLocalDataSource =
        ProjectLocalDataSourceImpl(ctx, secureLocalDbKeyProvider, loginInfoManager)

    @Provides
    @Singleton
    open fun provideProjectRemoteDataSource(remoteDbManager: RemoteDbManager): ProjectRemoteDataSource =
        ProjectRemoteDataSourceImpl(remoteDbManager)

    @Provides
    open fun provideProjectRepository(projectLocalDataSource: ProjectLocalDataSource,
                                      projectRemoteDataSource: ProjectRemoteDataSource): ProjectRepository =
        ProjectRepositoryImpl(projectLocalDataSource, projectRemoteDataSource)

    @Provides
    open fun providePersonRepository(personLocalDataSource: PersonLocalDataSource,
                                     personRemoteDataSource: PersonRemoteDataSource,
                                     peopleUpSyncExecutor: PeopleUpSyncExecutor,
                                     downSyncScopeRepository: PeopleDownSyncScopeRepository): PersonRepository =
        PersonRepositoryImpl(personRemoteDataSource, personLocalDataSource, downSyncScopeRepository, peopleUpSyncExecutor)


    @Provides
    @Singleton
    open fun providePersonLocalDataSource(ctx: Context,
                                          secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
                                          loginInfoManager: LoginInfoManager): PersonLocalDataSource =
        PersonLocalDataSourceImpl(ctx, secureLocalDbKeyProvider, loginInfoManager)

    @Provides
    open fun provideFingerprintRecordLocalDataSource(personLocalDataSource: PersonLocalDataSource): FingerprintIdentityLocalDataSource =
        personLocalDataSource

    @Provides
    open fun provideImageLocalDataSource(
        context: Context
    ): ImageLocalDataSource = ImageLocalDataSourceImpl(context)

    @Provides
    open fun provideImageRemoteDataSource(): ImageRemoteDataSource = ImageRemoteDataSourceImpl()

    @Provides
    @Singleton
    open fun provideImageRepository(
        localDataSource: ImageLocalDataSource,
        remoteDataSource: ImageRemoteDataSource
    ): ImageRepository = ImageRepositoryImpl(localDataSource, remoteDataSource)

}

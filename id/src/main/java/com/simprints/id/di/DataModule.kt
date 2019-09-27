package com.simprints.id.di

import android.content.Context
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.PersonRepositoryImpl
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
import com.simprints.id.data.db.syncinfo.local.SyncInfoLocalDataSource
import com.simprints.id.data.db.syncinfo.local.SyncInfoLocalDataSourceImpl
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    @Singleton
    open fun providePersonRemoteDataSource(remoteDbManager: RemoteDbManager): PersonRemoteDataSource = PersonRemoteDataSourceImpl(remoteDbManager)


    @Provides
    open fun provideSyncInfoLocalDataSource(ctx: Context,
                                            secureDataManager: SecureDataManager,
                                            loginInfoManager: LoginInfoManager): SyncInfoLocalDataSource =
        SyncInfoLocalDataSourceImpl(ctx, secureDataManager, loginInfoManager)

    @Provides
    open fun provideProjectLocalDataSource(ctx: Context,
                                           secureDataManager: SecureDataManager,
                                           loginInfoManager: LoginInfoManager): ProjectLocalDataSource =
        ProjectLocalDataSourceImpl(ctx, secureDataManager, loginInfoManager)

    @Provides
    open fun provideProjectRemoteDataSource(remoteDbManager: RemoteDbManager): ProjectRemoteDataSource =
        ProjectRemoteDataSourceImpl(remoteDbManager)

    @Provides
    open fun provideProjectRepository(projectLocalDataSource: ProjectLocalDataSource,
                                      projectRemoteDataSource: ProjectRemoteDataSource): ProjectRepository =
        ProjectRepositoryImpl(projectLocalDataSource, projectRemoteDataSource)

    @Provides
    open fun providePersonRepository(personLocalDataSource: PersonLocalDataSource,
                                     personRemoteDataSource: PersonRemoteDataSource,
                                     peopleUpSyncMaster: PeopleUpSyncMaster): PersonRepository =
        PersonRepositoryImpl(personRemoteDataSource, personLocalDataSource, peopleUpSyncMaster)


    @Provides
    open fun providePersonLocalDataSource(ctx: Context,
                                          secureDataManager: SecureDataManager,
                                          loginInfoManager: LoginInfoManager): PersonLocalDataSource =
        PersonLocalDataSourceImpl(ctx, secureDataManager, loginInfoManager)
}

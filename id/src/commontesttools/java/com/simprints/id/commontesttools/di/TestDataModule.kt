package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.core.images.repository.ImageRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.di.DataModule
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import com.simprints.testtools.common.di.DependencyRule
import kotlinx.coroutines.FlowPreview

class TestDataModule(
    private val projectLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRemoteDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val personRemoteDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val personLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val imageRepositoryRule: DependencyRule = DependencyRule.RealRule
) : DataModule() {

    @FlowPreview
    override fun provideProjectLocalDataSource(ctx: Context,
                                               secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
                                               loginInfoManager: LoginInfoManager): ProjectLocalDataSource =
        projectLocalDataSourceRule.resolveDependency { super.provideProjectLocalDataSource(ctx, secureLocalDbKeyProvider, loginInfoManager) }

    override fun provideProjectRemoteDataSource(
        remoteDbManager: RemoteDbManager
    ): ProjectRemoteDataSource = projectRemoteDataSourceRule.resolveDependency {
        super.provideProjectRemoteDataSource(remoteDbManager)
    }

    override fun provideProjectRepository(
        projectLocalDataSource: ProjectLocalDataSource,
        projectRemoteDataSource: ProjectRemoteDataSource
    ): ProjectRepository = projectRepositoryRule.resolveDependency {
        super.provideProjectRepository(projectLocalDataSource, projectRemoteDataSource)
    }

    override fun providePersonRepository(
        personRemoteDataSource: PersonRemoteDataSource,
        personLocalDataSource: PersonLocalDataSource,
        peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
        peopleUpSyncExecutor: PeopleUpSyncExecutor
    ): PersonRepository = personRepositoryRule.resolveDependency {
        super.providePersonRepository(
            personRemoteDataSource,
            personLocalDataSource,
            peopleDownSyncScopeRepository,
            peopleUpSyncExecutor
        )
    }

    override fun provideImageRepository(
        context: Context
    ): ImageRepository = imageRepositoryRule.resolveDependency {
        super.provideImageRepository(context)
    }

    override fun providePersonRemoteDataSource(remoteDbManager: RemoteDbManager): PersonRemoteDataSource =
        personRemoteDataSourceRule.resolveDependency { super.providePersonRemoteDataSource(remoteDbManager) }

    @FlowPreview
    override fun providePersonLocalDataSource(ctx: Context,
                                              secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
                                              loginInfoManager: LoginInfoManager): PersonLocalDataSource =
        personLocalDataSourceRule.resolveDependency { super.providePersonLocalDataSource(ctx, secureLocalDbKeyProvider, loginInfoManager) }

}

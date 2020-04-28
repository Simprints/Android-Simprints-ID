package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.core.images.repository.ImageRepository
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.PersonRepositoryDownSyncHelper
import com.simprints.id.data.db.person.PersonRepositoryUpSyncHelper
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.di.DataModule
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule
import kotlinx.coroutines.FlowPreview

class TestDataModule(
    private val projectLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRemoteDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val personLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryUpSyncHelperRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryDownSyncHelperRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val imageRepositoryRule: DependencyRule = DependencyRule.RealRule
) : DataModule() {

    @FlowPreview
    override fun provideProjectLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager
    ): ProjectLocalDataSource =
        projectLocalDataSourceRule.resolveDependency {
            super.provideProjectLocalDataSource(
                ctx,
                secureLocalDbKeyProvider,
                loginInfoManager
            )
        }

    override fun provideProjectRemoteDataSource(
        remoteDbManager: RemoteDbManager,
        simApiClientFactory: SimApiClientFactory
    ): ProjectRemoteDataSource = projectRemoteDataSourceRule.resolveDependency {
        super.provideProjectRemoteDataSource(remoteDbManager, simApiClientFactory)
    }

    override fun provideProjectRepository(
        projectLocalDataSource: ProjectLocalDataSource,
        projectRemoteDataSource: ProjectRemoteDataSource
    ): ProjectRepository = projectRepositoryRule.resolveDependency {
        super.provideProjectRepository(
            projectLocalDataSource,
            projectRemoteDataSource
        )
    }

    override fun providePersonRepositoryUpSyncHelper(
        loginInfoManager: LoginInfoManager,
        personLocalDataSource: PersonLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
        preferencesManager: PreferencesManager,
        peopleSyncCache: PeopleSyncCache
    ): PersonRepositoryUpSyncHelper =
        personRepositoryUpSyncHelperRule.resolveDependency {
            super.providePersonRepositoryUpSyncHelper(
                loginInfoManager, personLocalDataSource, eventRemoteDataSource,
                peopleUpSyncScopeRepository, preferencesManager, peopleSyncCache
            )
    }

    override fun providePersonRepositoryDownSyncHelper(personLocalDataSource: PersonLocalDataSource,
                                                       eventRemoteDataSource: EventRemoteDataSource,
                                                       downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                                       timeHelper: TimeHelper): PersonRepositoryDownSyncHelper =
        personRepositoryDownSyncHelperRule.resolveDependency {
            super.providePersonRepositoryDownSyncHelper(personLocalDataSource, eventRemoteDataSource,
                downSyncScopeRepository, timeHelper)
        }


    override fun providePersonRepository(
        personLocalDataSource: PersonLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
        peopleUpSyncExecutor: PeopleUpSyncExecutor,
        personRepositoryUpSyncHelper: PersonRepositoryUpSyncHelper,
        personRepositoryDownSyncHelper: PersonRepositoryDownSyncHelper
    ): PersonRepository = personRepositoryRule.resolveDependency {
        super.providePersonRepository(
            personLocalDataSource,
            eventRemoteDataSource,
            peopleDownSyncScopeRepository,
            peopleUpSyncExecutor,
            personRepositoryUpSyncHelper,
            personRepositoryDownSyncHelper
        )
    }

    override fun provideImageRepository(
        context: Context,
        baseUrlProvider: BaseUrlProvider
    ): ImageRepository = imageRepositoryRule.resolveDependency {
        super.provideImageRepository(context, baseUrlProvider)
    }

    override fun provideLongConsentLocalDataSource(context: Context, loginInfoManager: LoginInfoManager): LongConsentLocalDataSource =
        longConsentLocalDataSourceRule.resolveDependency { super.provideLongConsentLocalDataSource(context, loginInfoManager) }

    override fun provideLongConsentRepository(longConsentLocalDataSource: LongConsentLocalDataSource, loginInfoManager: LoginInfoManager,
                                              crashReportManager: CrashReportManager): LongConsentRepository =
        longConsentRepositoryRule.resolveDependency { super.provideLongConsentRepository(longConsentLocalDataSource, loginInfoManager, crashReportManager) }

    @FlowPreview
    override fun providePersonLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager
    ): PersonLocalDataSource =
        personLocalDataSourceRule.resolveDependency {
            super.providePersonLocalDataSource(
                ctx,
                secureLocalDbKeyProvider,
                loginInfoManager
            )
        }

}

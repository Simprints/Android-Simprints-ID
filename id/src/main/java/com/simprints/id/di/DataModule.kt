package com.simprints.id.di

import android.content.Context
import com.simprints.core.images.repository.ImageRepository
import com.simprints.core.images.repository.ImageRepositoryImpl
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSourceImpl
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.consent.longconsent.LongConsentRepositoryImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.person.*
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.local.PersonLocalDataSourceImpl
import com.simprints.id.data.db.person.remote.EventRemoteDataSource
import com.simprints.id.data.db.person.remote.EventRemoteDataSourceImpl
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.ProjectRepositoryImpl
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSourceImpl
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSourceImpl
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncExecutor
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    @Singleton
    open fun provideEventRemoteDataSource(
        remoteDbManager: RemoteDbManager,
        simApiClientFactory: SimApiClientFactory
    ): EventRemoteDataSource = EventRemoteDataSourceImpl(remoteDbManager, simApiClientFactory)

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
        simApiClientFactory: SimApiClientFactory
    ): ProjectRemoteDataSource = ProjectRemoteDataSourceImpl(
        remoteDbManager,
        simApiClientFactory
    )

    @Provides
    open fun provideProjectRepository(
        projectLocalDataSource: ProjectLocalDataSource,
        projectRemoteDataSource: ProjectRemoteDataSource
    ): ProjectRepository = ProjectRepositoryImpl(
        projectLocalDataSource,
        projectRemoteDataSource
    )

    @Provides
    open fun providePersonRepositoryUpSyncHelper(
        loginInfoManager: LoginInfoManager,
        personLocalDataSource: PersonLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
        preferencesManager: PreferencesManager,
        peopleSyncCache: PeopleSyncCache) : PersonRepositoryUpSyncHelper =
        PersonRepositoryUpSyncHelperImpl(loginInfoManager, personLocalDataSource, eventRemoteDataSource,
                peopleUpSyncScopeRepository, preferencesManager.modalities)

    @Provides
    open fun providePersonRepositoryDownSyncHelper(personLocalDataSource: PersonLocalDataSource,
                                                   eventRemoteDataSource: EventRemoteDataSource,
                                                   downSyncScopeRepository: PeopleDownSyncScopeRepository,
                                                   timeHelper: TimeHelper): PersonRepositoryDownSyncHelper =
        PersonRepositoryDownSyncHelperImpl(personLocalDataSource, eventRemoteDataSource, downSyncScopeRepository, timeHelper)

    @Provides
    open fun providePersonRepository(
        personLocalDataSource: PersonLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
        peopleUpSyncExecutor: PeopleUpSyncExecutor,
        personRepositoryUpSyncHelper: PersonRepositoryUpSyncHelper,
        personRepositoryDownSyncHelper: PersonRepositoryDownSyncHelper
    ): PersonRepository = PersonRepositoryImpl(
        eventRemoteDataSource,
        personLocalDataSource,
        peopleDownSyncScopeRepository,
        peopleUpSyncExecutor,
        personRepositoryUpSyncHelper,
        personRepositoryDownSyncHelper
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
        context: Context,
        baseUrlProvider: BaseUrlProvider
    ): ImageRepository = ImageRepositoryImpl(context, baseUrlProvider)

    @Provides
    open fun provideLongConsentLocalDataSource(
        context: Context,
        loginInfoManager: LoginInfoManager
    ): LongConsentLocalDataSource =
        LongConsentLocalDataSourceImpl(context.filesDir.absolutePath, loginInfoManager)

    @Provides
    open fun provideLongConsentRepository(
        longConsentLocalDataSource: LongConsentLocalDataSource,
        loginInfoManager: LoginInfoManager,
        crashReportManager: CrashReportManager
    ): LongConsentRepository = LongConsentRepositoryImpl(longConsentLocalDataSource,
        loginInfoManager, crashReportManager)

}

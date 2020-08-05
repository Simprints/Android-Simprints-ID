package com.simprints.id.di

import android.content.Context
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSourceImpl
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.consent.longconsent.LongConsentRepositoryImpl
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.event.remote.EventRemoteDataSourceImpl
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.ProjectRepositoryImpl
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSourceImpl
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSourceImpl
import com.simprints.id.data.db.subject.*
import com.simprints.id.data.db.subject.local.FaceIdentityLocalDataSource
import com.simprints.id.data.db.subject.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectLocalDataSourceImpl
import com.simprints.id.data.db.subjects_sync.down.SubjectRepositoryDownSyncHelper
import com.simprints.id.data.db.subjects_sync.down.SubjectRepositoryDownSyncHelperImpl
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.up.SubjectRepositoryUpSyncHelper
import com.simprints.id.data.db.subjects_sync.up.SubjectRepositoryUpSyncHelperImpl
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.images.repository.ImageRepositoryImpl
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.services.sync.subjects.master.internal.SubjectsSyncCache
import com.simprints.id.services.sync.subjects.up.controllers.SubjectsUpSyncExecutor
import com.simprints.id.tools.TimeHelper
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    open fun provideEventRemoteDataSource(
        simApiClientFactory: SimApiClientFactory
    ): EventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory)

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
        simApiClientFactory: SimApiClientFactory
    ): ProjectRemoteDataSource = ProjectRemoteDataSourceImpl(
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
        subjectLocalDataSource: SubjectLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository,
        preferencesManager: PreferencesManager,
        subjectsSyncCache: SubjectsSyncCache): SubjectRepositoryUpSyncHelper =
        SubjectRepositoryUpSyncHelperImpl(loginInfoManager, subjectLocalDataSource, eventRemoteDataSource,
            subjectsUpSyncScopeRepository, preferencesManager.modalities)

    @Provides
    open fun providePersonRepositoryDownSyncHelper(subjectLocalDataSource: SubjectLocalDataSource,
                                                   eventRemoteDataSource: EventRemoteDataSource,
                                                   downSyncScopeRepository: SubjectsDownSyncScopeRepository,
                                                   timeHelper: TimeHelper): SubjectRepositoryDownSyncHelper =
        SubjectRepositoryDownSyncHelperImpl(subjectLocalDataSource, eventRemoteDataSource, downSyncScopeRepository, timeHelper)

    @Provides
    open fun providePersonRepository(
        subjectLocalDataSource: SubjectLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource,
        subjectsUpSyncExecutor: SubjectsUpSyncExecutor
    ): SubjectRepository = SubjectRepositoryImpl(
        eventRemoteDataSource,
        subjectLocalDataSource,
        subjectsUpSyncExecutor
    )

    @Provides
    @Singleton
    @FlowPreview
    open fun providePersonLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager
    ): SubjectLocalDataSource = SubjectLocalDataSourceImpl(
        ctx,
        secureLocalDbKeyProvider,
        loginInfoManager
    )

    @Provides
    open fun provideFingerprintRecordLocalDataSource(
        subjectLocalDataSource: SubjectLocalDataSource
    ): FingerprintIdentityLocalDataSource = subjectLocalDataSource

    @Provides
    open fun provideFaceIdentityLocalDataSource(
        subjectLocalDataSource: SubjectLocalDataSource
    ): FaceIdentityLocalDataSource = subjectLocalDataSource

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

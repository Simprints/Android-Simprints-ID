package com.simprints.id.di

import android.content.Context
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.event.remote.EventRemoteDataSourceImpl
import com.simprints.eventsystem.events_sync.EventSyncStatusDatabase
import com.simprints.id.data.consent.longconsent.*
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.ProjectRepositoryImpl
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSourceImpl
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSourceImpl
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.SubjectRepositoryImpl
import com.simprints.id.data.db.subject.local.FaceIdentityLocalDataSource
import com.simprints.id.data.db.subject.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subject.local.SubjectLocalDataSourceImpl
import com.simprints.id.data.db.subject.migration.SubjectToEventDbMigrationManagerImpl
import com.simprints.id.data.db.subject.migration.SubjectToEventMigrationManager
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.images.repository.ImageRepositoryImpl
import com.simprints.id.data.license.local.LicenseLocalDataSource
import com.simprints.id.data.license.local.LicenseLocalDataSourceImpl
import com.simprints.id.data.license.remote.LicenseRemoteDataSource
import com.simprints.id.data.license.remote.LicenseRemoteDataSourceImpl
import com.simprints.id.data.license.repository.LicenseRepository
import com.simprints.id.data.license.repository.LicenseRepositoryImpl
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.network.BaseUrlProvider
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    open fun provideEventRemoteDataSource(
        simApiClientFactory: SimApiClientFactory,
        jsonHelper: JsonHelper
    ): EventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory, jsonHelper)

    @Provides
    @FlowPreview
    open fun provideProjectLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager,
        dispatcher: DispatcherProvider
    ): ProjectLocalDataSource = ProjectLocalDataSourceImpl(
        ctx,
        secureLocalDbKeyProvider,
        loginInfoManager,
        dispatcher
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
        projectRemoteDataSource: ProjectRemoteDataSource,
        remoteConfigWrapper: RemoteConfigWrapper
    ): ProjectRepository = ProjectRepositoryImpl(
        projectLocalDataSource,
        projectRemoteDataSource,
        remoteConfigWrapper
    )

    @Provides
    open fun provideSubjectRepository(
        subjectLocalDataSource: SubjectLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource
    ): SubjectRepository = SubjectRepositoryImpl(
        subjectLocalDataSource
    )

    @Provides
    @Singleton
    @FlowPreview
    open fun providePersonLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager,
        dispatcher: DispatcherProvider
    ): SubjectLocalDataSource = SubjectLocalDataSourceImpl(
        ctx,
        secureLocalDbKeyProvider,
        loginInfoManager,
        dispatcher
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
        baseUrlProvider: BaseUrlProvider,
        remoteDbManager: RemoteDbManager
    ): ImageRepository = ImageRepositoryImpl(context, baseUrlProvider, remoteDbManager)

    @Provides
    open fun provideLongConsentLocalDataSource(
        context: Context,
        loginInfoManager: LoginInfoManager
    ): LongConsentLocalDataSource =
        LongConsentLocalDataSourceImpl(context.filesDir.absolutePath, loginInfoManager)

    @Provides
    open fun provideLongConsentRemoteDataSource(
        loginInfoManager: LoginInfoManager,
        remoteDbManager: RemoteDbManager
    ): LongConsentRemoteDataSource =
        LongConsentRemoteDataSourceImpl(loginInfoManager, remoteDbManager)

    @Provides
    open fun provideLongConsentRepository(
        longConsentLocalDataSource: LongConsentLocalDataSource,
        longConsentRemoteDataSource: LongConsentRemoteDataSource
    ): LongConsentRepository = LongConsentRepositoryImpl(
        longConsentLocalDataSource,
        longConsentRemoteDataSource
    )

    @Provides
    @Singleton
    open fun provideEventsSyncStatusDatabase(ctx: Context): EventSyncStatusDatabase =
        EventSyncStatusDatabase.getDatabase(ctx)

    @Provides
    open fun provideSubjectToEventMigrationManager(
        loginInfoManager: LoginInfoManager,
        eventLocal: EventLocalDataSource,
        timeHelper: TimeHelper,
        preferencesManager: IdPreferencesManager,
        subjectLocal: SubjectLocalDataSource,
        encoder: EncodingUtils
    ): SubjectToEventMigrationManager =
        SubjectToEventDbMigrationManagerImpl(
            loginInfoManager,
            eventLocal,
            timeHelper,
            preferencesManager,
            subjectLocal,
            encoder
        )

    @Provides
    open fun provideLicenseLocalDataSource(context: Context): LicenseLocalDataSource =
        LicenseLocalDataSourceImpl(context)

    @Provides
    open fun provideLicenseRemoteDataSource(
        simApiClientFactory: SimApiClientFactory,
        jsonHelper: JsonHelper
    ): LicenseRemoteDataSource = LicenseRemoteDataSourceImpl(simApiClientFactory, jsonHelper)

    @Provides
    open fun provideLicenseRepository(
        licenseLocalDataSource: LicenseLocalDataSource,
        licenseRemoteDataSource: LicenseRemoteDataSource
    ): LicenseRepository = LicenseRepositoryImpl(licenseLocalDataSource, licenseRemoteDataSource)
}

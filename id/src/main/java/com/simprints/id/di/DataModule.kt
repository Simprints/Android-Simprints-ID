package com.simprints.id.di

import android.content.Context
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClientFactory
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.event.remote.EventRemoteDataSourceImpl
import com.simprints.eventsystem.events_sync.EventSyncStatusDatabase
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.consent.longconsent.LongConsentRepositoryImpl
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSourceImpl
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSourceImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.ProjectRepositoryImpl
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSourceImpl
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSourceImpl
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.SubjectRepositoryImpl
import com.simprints.id.data.db.subject.local.*
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.images.repository.ImageRepositoryImpl
import com.simprints.id.data.license.local.LicenseLocalDataSource
import com.simprints.id.data.license.local.LicenseLocalDataSourceImpl
import com.simprints.id.data.license.remote.LicenseRemoteDataSource
import com.simprints.id.data.license.remote.LicenseRemoteDataSourceImpl
import com.simprints.id.data.license.repository.LicenseRepository
import com.simprints.id.data.license.repository.LicenseRepositoryImpl
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.network.ImageUrlProvider
import dagger.Module
import dagger.Provides
import java.net.URL
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    open fun provideEventRemoteDataSource(
        simApiClientFactory: SimApiClientFactory,
        jsonHelper: JsonHelper
    ): EventRemoteDataSource = EventRemoteDataSourceImpl(simApiClientFactory, jsonHelper)

    @Provides
    open fun provideProjectLocalDataSource(
        realmWrapper: RealmWrapper
    ): ProjectLocalDataSource = ProjectLocalDataSourceImpl(
        realmWrapper
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
    open fun provideRealmWrapper(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager,
        dispatcher: DispatcherProvider,
    ): RealmWrapper = RealmWrapperImpl(
        ctx,
        secureLocalDbKeyProvider,
        loginInfoManager,
        dispatcher
    )

    @Provides
    @Singleton
    open fun providePersonLocalDataSource(
        realmWrapper: RealmWrapper
    ): SubjectLocalDataSource = SubjectLocalDataSourceImpl(
        realmWrapper
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
        imageUrlProvider: ImageUrlProvider,
        remoteDbManager: RemoteDbManager
    ): ImageRepository = ImageRepositoryImpl(context, imageUrlProvider, remoteDbManager)

    @Provides
    open fun provideLongConsentLocalDataSource(
        context: Context,
        loginInfoManager: LoginInfoManager
    ): LongConsentLocalDataSource =
        LongConsentLocalDataSourceImpl(context.filesDir.absolutePath, loginInfoManager)

    @Provides
    open fun provideLongConsentRemoteDataSource(
        loginInfoManager: LoginInfoManager,
        simApiClientFactory: SimApiClientFactory
    ): LongConsentRemoteDataSource =
        LongConsentRemoteDataSourceImpl(
            loginInfoManager,
            simApiClientFactory,
            consentDownloader = { fileUrl -> URL(fileUrl.url).readBytes() }
        )

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

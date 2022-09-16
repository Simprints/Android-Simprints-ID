package com.simprints.id.di

import android.content.Context
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
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.enrolmentrecords.remote.EnrolmentRecordRemoteDataSource
import com.simprints.id.enrolmentrecords.remote.EnrolmentRecordRemoteDataSourceImpl
import com.simprints.infra.login.LoginManager
import com.simprints.infra.realm.RealmWrapper
import dagger.Module
import dagger.Provides
import java.net.URL
import javax.inject.Singleton

@Module
open class DataModule {

    @Provides
    open fun provideEventRemoteDataSource(
        loginManager: LoginManager,
        jsonHelper: JsonHelper
    ): EventRemoteDataSource = EventRemoteDataSourceImpl(loginManager, jsonHelper)

    @Provides
    open fun provideProjectLocalDataSource(
        realmWrapper: RealmWrapper
    ): ProjectLocalDataSource = ProjectLocalDataSourceImpl(
        realmWrapper
    )

    @Provides
    @Singleton
    open fun provideProjectRemoteDataSource(
        loginManager: LoginManager,
    ): ProjectRemoteDataSource = ProjectRemoteDataSourceImpl(
        loginManager
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
    open fun provideLongConsentLocalDataSource(
        context: Context,
        loginManager: LoginManager
    ): LongConsentLocalDataSource =
        LongConsentLocalDataSourceImpl(context.filesDir.absolutePath, loginManager)

    @Provides
    open fun provideLongConsentRemoteDataSource(
        loginManager: LoginManager,
    ): LongConsentRemoteDataSource =
        LongConsentRemoteDataSourceImpl(
            loginManager,
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
    fun provideEnrolmentRecordRemoteDataSource(loginManager: LoginManager): EnrolmentRecordRemoteDataSource =
        EnrolmentRecordRemoteDataSourceImpl(loginManager)
}

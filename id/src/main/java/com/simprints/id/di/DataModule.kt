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
import com.simprints.infra.login.LoginManager
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import java.net.URL
import javax.inject.Singleton

// TODO: Remove after hilt migration
@DisableInstallInCheck
@Module
open class DataModule {

    @Provides
    open fun provideEventRemoteDataSource(
        loginManager: LoginManager,
        jsonHelper: JsonHelper
    ): EventRemoteDataSource = EventRemoteDataSourceImpl(loginManager, jsonHelper)

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
}

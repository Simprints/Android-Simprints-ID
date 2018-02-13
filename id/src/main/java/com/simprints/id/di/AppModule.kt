package com.simprints.id.di

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.controllers.Setup
import com.simprints.id.data.DataManager
import com.simprints.id.data.DataManagerImpl
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.network.ApiManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.tools.AppState
import com.simprints.id.tools.NotificationFactory
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import javax.inject.Singleton

/**
 * Created by fabiotuzza on 16/01/2018.
 */
@Module
open class AppModule(val app: Application) {

    @Provides @Singleton fun provideApplication(): Application = app
    @Provides @Singleton fun provideContext(): Context = app

    @Provides @Singleton fun provideLocalDbManager(ctx: Context): LocalDbManager = RealmDbManager(ctx)
    @Provides @Singleton fun provideRemoteDbManager(ctx: Context): RemoteDbManager = FirebaseManager(ctx)
    @Provides @Singleton fun provideDbManager(localDbManager: LocalDbManager, remoteDbManager: RemoteDbManager): DbManager = DbManagerImpl(localDbManager, remoteDbManager)

    @Provides @Singleton fun provideApiManager(): ApiManager = ApiManagerImpl()
    @Provides @Singleton fun provideFabric(app: Application): Fabric = Fabric.Builder(app).kits(Crashlytics()).debuggable(BuildConfig.DEBUG).build()
    @Provides @Singleton fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(app).apply {
            setAnalyticsCollectionEnabled(true)
            setMinimumSessionDuration(0)
        }

    @Provides @Singleton fun provideAnalyticsManager(firebaseAnalytics: FirebaseAnalytics): AnalyticsManager = FirebaseAnalyticsManager(firebaseAnalytics)
    @Provides @Singleton fun provideSecureDataManager(improvedSharedPreferences: ImprovedSharedPreferences): SecureDataManager = SecureDataManagerImpl(improvedSharedPreferences)
    @Provides @Singleton fun provideDataManager(app: Application,
                                                preferencesManager: PreferencesManager,
                                                dbManager: DbManager,
                                                apiManager: ApiManager,
                                                analyticsManager: AnalyticsManager,
                                                secureDataManager: SecureDataManager): DataManager =
        DataManagerImpl(app, preferencesManager, dbManager, apiManager, analyticsManager, secureDataManager)


    @Provides @Singleton fun provideAppState(): AppState = AppState()
    @Provides @Singleton fun provideSetup(dataManager: DataManager, appState: AppState): Setup = Setup.getInstance(dataManager, appState)

    @Provides @Singleton fun provideTimeHelper(): TimeHelper = TimeHelperImpl()

    @Provides
    @Singleton
    fun provideNotificationFactory(app: Application):NotificationFactory {
        val factory = NotificationFactory(app)
        factory.initSyncNotificationChannel()
        return factory
    }
}

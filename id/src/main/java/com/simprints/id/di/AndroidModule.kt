package com.simprints.id.di

import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import javax.inject.Singleton

/**
 * Created by fabiotuzza on 17/01/2018.
 */
@Module
internal class AndroidModule(val app: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Provides @Singleton fun provideFabric(app: Application): Fabric =
        Fabric.Builder(app).kits(Crashlytics()).debuggable(BuildConfig.DEBUG).build()

    @Provides @Singleton fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(app).apply {
            setAnalyticsCollectionEnabled(true)
            setMinimumSessionDuration(0)
        }
}

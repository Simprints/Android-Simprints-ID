package com.simprints.core

import android.content.Context
import com.lyft.kronos.AndroidClockFactory
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.extentions.deviceId
import com.simprints.core.tools.extentions.packageVersionName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.KronosTimeHelperImpl
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtilsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(ActivityComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideTimeHelper(context: Context): TimeHelper = KronosTimeHelperImpl(
        AndroidClockFactory.createKronosClock(
            context,
            requestTimeoutMs = TimeUnit.SECONDS.toMillis(60),
            minWaitTimeBetweenSyncMs = TimeUnit.MINUTES.toMillis(30),
            cacheExpirationMs = TimeUnit.MINUTES.toMillis(30)
        )
    )

    @Provides
    @Singleton
    fun provideSimNetworkUtils(ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    @Singleton
    fun provideDispatcher(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideJsonHelper(): JsonHelper = JsonHelper

    @DeviceID
    @Provides
    fun provideDeviceId(
        context: Context
    ): String = context.deviceId

    @PackageVersionName
    @Provides
    fun providePackageVersionName(
        context: Context
    ): String = context.packageVersionName

    @LibSimprintsVersionName
    @Provides
    fun provideLibSimprintsVersionName(): String =
        com.simprints.libsimprints.BuildConfig.LIBRARY_PACKAGE_NAME


}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LibSimprintsVersionName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PackageVersionName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeviceID

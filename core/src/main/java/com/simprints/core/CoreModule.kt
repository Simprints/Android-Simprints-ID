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
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtilsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideTimeHelper(@ApplicationContext context: Context): TimeHelper = KronosTimeHelperImpl(
        AndroidClockFactory.createKronosClock(
            context,
            requestTimeoutMs = TimeUnit.SECONDS.toMillis(60),
            minWaitTimeBetweenSyncMs = TimeUnit.MINUTES.toMillis(30),
            cacheExpirationMs = TimeUnit.MINUTES.toMillis(30)
        )
    )

    @Provides
    @Singleton
    fun provideSimNetworkUtils(@ApplicationContext ctx: Context): SimNetworkUtils = SimNetworkUtilsImpl(ctx)

    @Provides
    @Singleton
    fun provideDispatcher(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideJsonHelper(): JsonHelper = JsonHelper

    @Provides
    @Singleton
    fun provideEncodingUtils(): EncodingUtils = EncodingUtilsImpl

    @DeviceID
    @Provides
    fun provideDeviceId(
        @ApplicationContext context: Context
    ): String = context.deviceId

    @PackageVersionName
    @Provides
    fun providePackageVersionName(
        @ApplicationContext context: Context
    ): String = context.packageVersionName

    @LibSimprintsVersionName
    @Provides
    fun provideLibSimprintsVersionName(): String =
        com.simprints.libsimprints.BuildConfig.LIBRARY_PACKAGE_VERSION

    @DispatcherIO
    @Provides
    fun provideDispatcherIo(): CoroutineDispatcher = Dispatchers.IO

    @NonCancellableIO
    @Provides
    fun provideNonCancellableIO(
        @DispatcherIO dispatcherIO: CoroutineDispatcher
    ): CoroutineContext = dispatcherIO + NonCancellable

    @ExternalScope
    @Provides
    fun provideExternalScope(): CoroutineScope = CoroutineScope(SupervisorJob())
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherIO

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NonCancellableIO

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExternalScope

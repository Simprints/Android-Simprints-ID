package com.simprints.feature.dashboard.tools.di

import android.content.Context
import androidx.work.WorkManager
import com.simprints.core.AppScope
import com.simprints.core.AvailableProcessors
import com.simprints.core.CoreModule
import com.simprints.core.DeviceID
import com.simprints.core.DispatcherBG
import com.simprints.core.DispatcherIO
import com.simprints.core.DispatcherMain
import com.simprints.core.ExternalScope
import com.simprints.core.NonCancellableIO
import com.simprints.core.PackageVersionName
import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.Ticker
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.StringTokenizer
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CoreModule::class],
)
object FakeCoreModule {
    const val DEVICE_ID = "deviceId"
    const val PACKAGE_VERSION_NAME = "version"

    @Provides
    @Singleton
    fun provideTimeHelper(): TimeHelper = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideTicker(): Ticker = mockk(relaxed = true)

    @DeviceID
    @Provides
    fun provideDeviceId(): String = DEVICE_ID

    @PackageVersionName
    @Provides
    fun providePackageVersionName(): String = PACKAGE_VERSION_NAME

    @AvailableProcessors
    @Provides
    fun provideAvailableProcessors(): Int = 4

    @DispatcherIO
    @Provides
    fun provideCoroutineDispatcherIo(): CoroutineDispatcher = StandardTestDispatcher()

    @DispatcherBG
    @Provides
    fun provideCoroutineDispatcherBg(): CoroutineDispatcher = StandardTestDispatcher()

    @DispatcherMain
    @Provides
    fun provideCoroutineDispatcherMain(): CoroutineDispatcher = StandardTestDispatcher()

    @NonCancellableIO
    @Provides
    fun provideNonCancellableIO(): CoroutineContext = StandardTestDispatcher()

    @ExternalScope
    @Provides
    fun provideExternalScope(): CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    @SessionCoroutineScope
    @Provides
    fun provideSessionCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    @AppScope
    @Provides
    fun provideAppScope(): CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    @Provides
    @Singleton
    fun provideStringTokenizer(): StringTokenizer = mockk()

    @Provides
    @Singleton
    fun provideEncodingUtils(): EncodingUtils = EncodingUtilsImplForTests

    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = mockk(relaxed = true)
}

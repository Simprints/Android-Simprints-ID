package com.simprints.feature.dashboard.tools.di

import com.simprints.core.CoreModule
import com.simprints.core.DeviceID
import com.simprints.core.DispatcherIO
import com.simprints.core.ExternalScope
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.StringTokenizer
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CoreModule::class]
)
object FakeCoreModule {


    const val DEVICE_ID = "deviceId"
    const val PACKAGE_VERSION_NAME = "version"

    @Provides
    @Singleton
    fun provideTimeHelper(): TimeHelper = mockk()

    @DeviceID
    @Provides
    fun provideDeviceId(): String = DEVICE_ID

    @PackageVersionName
    @Provides
    fun providePackageVersionName(): String = PACKAGE_VERSION_NAME

    @DispatcherIO
    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher = StandardTestDispatcher()

    @ExternalScope
    @Provides
    fun provideExternalScope(): CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    @Provides
    @Singleton
    fun provideStringTokenizer(): StringTokenizer = mockk()

}

package com.simprints.feature.dashboard.tools.di

import com.simprints.feature.dashboard.debug.SecurityStateScheduler
import com.simprints.feature.dashboard.main.sync.DeviceManager
import com.simprints.feature.dashboard.main.sync.EventSyncCache
import com.simprints.feature.dashboard.main.sync.EventSyncManager
import com.simprints.feature.dashboard.settings.about.SignerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

// TODO removed when the interfaces in
//  - com.simprints.feature.dashboard.main.sync
//  - com.simprints.feature.dashboard.settings.about
//  - com.simprints.feature.dashboard.debug
//  are removed.
@Module
@InstallIn(SingletonComponent::class)
object FakeModule {
    @Provides
    @Singleton
    fun provideDeviceManager(): DeviceManager = mockk()

    @Provides
    @Singleton
    fun provideEventSyncCache(): EventSyncCache = mockk()

    @Provides
    @Singleton
    fun provideEventSyncManager(): EventSyncManager = mockk()

    @Provides
    @Singleton
    fun provideSignerManager(): SignerManager = mockk()

    @Provides
    @Singleton
    fun provideSecurityStateScheduler(): SecurityStateScheduler = mockk()
}

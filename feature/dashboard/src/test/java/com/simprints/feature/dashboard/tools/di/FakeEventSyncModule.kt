package com.simprints.feature.dashboard.tools.di

import com.simprints.infra.eventsync.DeleteModulesUseCase
import com.simprints.infra.eventsync.DeleteSyncInfoUseCase
import com.simprints.infra.eventsync.DownSyncSubjectUseCase
import com.simprints.infra.eventsync.EventSyncModule
import com.simprints.infra.eventsync.EventSyncWorkerTagRepository
import com.simprints.infra.eventsync.ResetDownSyncInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [EventSyncModule::class],
)
object FakeEventSyncModule {
    @Provides
    @Singleton
    fun provideDownSyncSubjectUseCase(): DownSyncSubjectUseCase = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideDeleteModulesUseCase(): DeleteModulesUseCase = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideDeleteSyncInfoUseCase(): DeleteSyncInfoUseCase = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideResetDownSyncInfoUseCase(): ResetDownSyncInfoUseCase = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideEventSyncWorkerTagRepository(): EventSyncWorkerTagRepository = mockk(relaxed = true)
}

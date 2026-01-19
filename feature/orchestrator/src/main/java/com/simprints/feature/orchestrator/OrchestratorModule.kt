package com.simprints.feature.orchestrator

import com.simprints.feature.orchestrator.tools.OrcJsonHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class ExecutorLockTimeoutSec

@Module
@InstallIn(SingletonComponent::class)
internal object OrchestratorModule {
    @Provides
    @Singleton
    @ExecutorLockTimeoutSec
    fun provideExecutorLockTimeout(): Int = 60

    @Provides
    @Singleton
    fun provideOrcJsonHelper(): OrcJsonHelper = OrcJsonHelper
}

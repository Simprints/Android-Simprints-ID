package com.simprints.infra.sync

import com.simprints.infra.sync.config.ProjectConfigurationScheduler
import com.simprints.infra.sync.config.ProjectConfigurationSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    internal abstract fun provideConfigurationScheduler(configurationScheduler: ProjectConfigurationSchedulerImpl): ProjectConfigurationScheduler

}

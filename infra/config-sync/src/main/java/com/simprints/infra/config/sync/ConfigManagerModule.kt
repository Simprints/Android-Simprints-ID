package com.simprints.infra.config.sync

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigManagerModule {

    @Binds
    internal abstract fun provideConfigurationScheduler(configurationScheduler: ProjectConfigurationSchedulerImpl): ProjectConfigurationScheduler

}

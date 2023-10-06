package com.simprints.infra.config.sync

import com.simprints.infra.config.sync.worker.ConfigurationScheduler
import com.simprints.infra.config.sync.worker.ConfigurationSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigManagerModule {

    @Binds
    internal abstract fun provideConfigManager(configManager: ConfigManagerImpl): ConfigManager

    @Binds
    internal abstract fun provideConfigurationScheduler(configurationScheduler: ConfigurationSchedulerImpl): ConfigurationScheduler

}
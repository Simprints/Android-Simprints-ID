package com.simprints.infra.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.simprints.infra.config.domain.ConfigService
import com.simprints.infra.config.domain.ConfigServiceImpl
import com.simprints.infra.config.local.ConfigLocalDataSource
import com.simprints.infra.config.local.ConfigLocalDataSourceImpl
import com.simprints.infra.config.local.migrations.ProjectConfigSharedPrefsMigration
import com.simprints.infra.config.local.migrations.ProjectRealmMigration
import com.simprints.infra.config.local.models.ProtoProject
import com.simprints.infra.config.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.local.serializer.ProjectConfigSerializer
import com.simprints.infra.config.local.serializer.ProjectSerializer
import com.simprints.infra.config.remote.ConfigRemoteDataSource
import com.simprints.infra.config.remote.ConfigRemoteDataSourceImpl
import com.simprints.infra.config.worker.ConfigurationScheduler
import com.simprints.infra.config.worker.ConfigurationSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val PROJECT_DATA_STORE_FILE_NAME = "project_prefs.pb"
private const val PROJECT_CONFIG_DATA_STORE_FILE_NAME = "project_config_prefs.pb"

@Module
@InstallIn(ActivityComponent::class)
abstract class ConfigManagerModule {

    @Binds
    internal abstract fun provideConfigManager(configManager: ConfigManagerImpl): ConfigManager

    @Binds
    internal abstract fun provideConfigurationScheduler(configurationScheduler: ConfigurationSchedulerImpl): ConfigurationScheduler


    @Binds
    internal abstract fun provideConfigRepository(service: ConfigServiceImpl): ConfigService

    @Binds
    internal abstract fun provideConfigRemoteDataSource(remoteDataSource: ConfigRemoteDataSourceImpl): ConfigRemoteDataSource

    @Binds
    internal abstract fun provideConfigLocalDataSource(localDataSource: ConfigLocalDataSourceImpl): ConfigLocalDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    internal fun provideProjectProtoDataStore(
        appContext: Context,
        projectRealmMigration: ProjectRealmMigration
    ): DataStore<ProtoProject> {
        return DataStoreFactory.create(
            serializer = ProjectSerializer,
            produceFile = { appContext.dataStoreFile(PROJECT_DATA_STORE_FILE_NAME) },
            migrations = listOf(projectRealmMigration)
        )
    }

    @Singleton
    @Provides
    internal fun provideConfigurationProtoDataStore(
        appContext: Context,
        projectConfigRealmMigration: ProjectConfigSharedPrefsMigration
    ): DataStore<ProtoProjectConfiguration> {
        return DataStoreFactory.create(
            serializer = ProjectConfigSerializer,
            produceFile = { appContext.dataStoreFile(PROJECT_CONFIG_DATA_STORE_FILE_NAME) },
            migrations = listOf(projectConfigRealmMigration)
        )
    }
}

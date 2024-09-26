package com.simprints.infra.config.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.simprints.infra.config.store.local.ConfigLocalDataSource
import com.simprints.infra.config.store.local.ConfigLocalDataSourceImpl
import com.simprints.infra.config.store.local.migrations.DeviceConfigSharedPrefsMigration
import com.simprints.infra.config.store.local.migrations.ProjectConfigFaceBioSdkMigration
import com.simprints.infra.config.store.local.migrations.ProjectConfigFingerprintBioSdkMigration
import com.simprints.infra.config.store.local.migrations.ProjectConfigQualityThresholdMigration
import com.simprints.infra.config.store.local.migrations.ProjectConfigSharedPrefsMigration
import com.simprints.infra.config.store.local.migrations.ProjectRealmMigration
import com.simprints.infra.config.store.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.serializer.DeviceConfigurationSerializer
import com.simprints.infra.config.store.local.serializer.ProjectConfigurationSerializer
import com.simprints.infra.config.store.local.serializer.ProjectSerializer
import com.simprints.infra.config.store.remote.ConfigRemoteDataSource
import com.simprints.infra.config.store.remote.ConfigRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

private const val PROJECT_DATA_STORE_FILE_NAME = "project_prefs.pb"
private const val PROJECT_CONFIG_DATA_STORE_FILE_NAME = "project_config_prefs.pb"
private const val DEVICE_CONFIG_DATA_STORE_FILE_NAME = "device_config_prefs.pb"

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigStoreModule {


    @Binds
    internal abstract fun provideConfigRepository(service: ConfigRepositoryImpl): ConfigRepository

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
        @ApplicationContext appContext: Context,
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
    internal fun provideProjectConfigurationProtoDataStore(
        @ApplicationContext appContext: Context,
        projectConfigSharedPrefsMigration: ProjectConfigSharedPrefsMigration,
        projectConfigQualityThresholdMigration: ProjectConfigQualityThresholdMigration,
        projectConfigFingerprintBioSdkMigration: ProjectConfigFingerprintBioSdkMigration,
        projectConfigFaceBioSdkMigration: ProjectConfigFaceBioSdkMigration,
    ): DataStore<ProtoProjectConfiguration> {
        return DataStoreFactory.create(
            serializer = ProjectConfigurationSerializer,
            produceFile = { appContext.dataStoreFile(PROJECT_CONFIG_DATA_STORE_FILE_NAME) },
            migrations = listOf(
                projectConfigSharedPrefsMigration,
                projectConfigQualityThresholdMigration,
                projectConfigFingerprintBioSdkMigration,
                projectConfigFaceBioSdkMigration
            )
        )
    }

    @Singleton
    @Provides
    internal fun provideDeviceConfigurationProtoDataStore(
        @ApplicationContext appContext: Context,
        deviceConfigSharedPrefsMigration: DeviceConfigSharedPrefsMigration
    ): DataStore<ProtoDeviceConfiguration> {
        return DataStoreFactory.create(
            serializer = DeviceConfigurationSerializer,
            produceFile = { appContext.dataStoreFile(DEVICE_CONFIG_DATA_STORE_FILE_NAME) },
            migrations = listOf(deviceConfigSharedPrefsMigration)
        )
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AbsolutePath

@Module
@InstallIn(SingletonComponent::class)
object ConfigManagerDependencies {

    @AbsolutePath
    @Provides
    @Singleton
    fun provideAbsolutePath(@ApplicationContext context: Context): String =
        context.filesDir.absolutePath
}

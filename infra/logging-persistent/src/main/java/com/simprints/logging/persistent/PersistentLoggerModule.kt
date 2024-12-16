package com.simprints.logging.persistent

import android.content.Context
import com.simprints.logging.persistent.database.LogEntryDao
import com.simprints.logging.persistent.database.LogEntryDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PersistentLoggerModule {
    @Binds
    internal abstract fun providePersistentLogger(logger: DatabasePersistentLogger): PersistentLogger
}

@Module
@InstallIn(SingletonComponent::class)
internal class PersistentLoggerModuleProviders {
    @Provides
    @Singleton
    fun provideImageMetadataDatabase(
        @ApplicationContext ctx: Context,
    ): LogEntryDatabase = LogEntryDatabase.getDatabase(ctx)

    @Provides
    @Singleton
    fun provideImageMetadataDao(database: LogEntryDatabase): LogEntryDao = database.logDao
}

package com.simprints.infra.recent.user.activity

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.simprints.infra.recent.user.activity.local.RecentUserActivityLocalSource
import com.simprints.infra.recent.user.activity.local.RecentUserActivityLocalSourceImpl
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySerializer
import com.simprints.infra.recent.user.activity.local.RecentUserActivitySharedPrefsMigration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val RECENT_USER_ACTIVITY_DATA_STORE_FILE_NAME = "recent_user_activity_prefs.pb"

@Module
@InstallIn(SingletonComponent::class)
abstract class RecentUserActivityModule {
    @Binds
    internal abstract fun provideManager(impl: RecentUserActivityManagerImpl): RecentUserActivityManager

    @Binds
    internal abstract fun provideConfigLocalDataSource(localDataSource: RecentUserActivityLocalSourceImpl): RecentUserActivityLocalSource
}

@Module
@InstallIn(SingletonComponent::class)
object RecentUserActivityDataStoreModule {
    @Singleton
    @Provides
    internal fun provideProjectProtoDataStore(
        @ApplicationContext appContext: Context,
        projectRealmMigration: RecentUserActivitySharedPrefsMigration,
    ): DataStore<ProtoRecentUserActivity> = DataStoreFactory.create(
        serializer = RecentUserActivitySerializer,
        produceFile = { appContext.dataStoreFile(RECENT_USER_ACTIVITY_DATA_STORE_FILE_NAME) },
        migrations = listOf(projectRealmMigration),
    )
}

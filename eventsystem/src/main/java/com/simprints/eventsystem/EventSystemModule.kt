package com.simprints.eventsystem

import android.content.Context
import androidx.multidex.BuildConfig.VERSION_NAME
import com.simprints.core.tools.extentions.deviceId
import com.simprints.core.tools.extentions.packageVersionName
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.EventRepositoryImpl
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactoryImpl
import com.simprints.eventsystem.event.local.*
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.event.remote.EventRemoteDataSourceImpl
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepositoryImpl
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Module
@InstallIn(ActivityComponent::class)
abstract class EventSystemModule {

    @Binds
    abstract fun bindSessionDataCache(impl: SessionDataCacheImpl): SessionDataCache

    @Binds
    abstract fun bindSessionEventValidatorsFactory(impl: SessionEventValidatorsFactoryImpl): SessionEventValidatorsFactory

    @Binds
    abstract fun bindEventRemoteDataSource(impl: EventRemoteDataSourceImpl): EventRemoteDataSource

    @Binds
    abstract fun bindEventDatabaseFactory(impl: DbEventDatabaseFactoryImpl): EventDatabaseFactory

    @Binds
    abstract fun bindEventLocalDataSource(impl: EventLocalDataSourceImpl): EventLocalDataSource

    @Binds
    abstract fun bindEventUpSyncScopeRepository(impl: EventUpSyncScopeRepositoryImpl): EventUpSyncScopeRepository

    @Binds
    abstract fun bindEventDownSyncScopeRepository(impl: EventDownSyncScopeRepositoryImpl): EventDownSyncScopeRepository

    @AssistedFactory
    interface EventRepositoryFactory {
        fun create(
            @Assisted("deviceId") deviceId: String,
            @Assisted("appVersionName") appVersionName: String,
            @Assisted("libSimprintsVersionName") libSimprintsVersionName: String
        ): EventRepositoryImpl
    }

}

@Module
@InstallIn(ActivityComponent::class)
class EventSystemProvider {

    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideWritableNonCancelableDispatcher(): CoroutineContext =
        provideIODispatcher() + NonCancellable

    @Provides
    @Singleton
    fun provideEventRepository(
        ctx: Context,
        factory: EventSystemModule.EventRepositoryFactory
    ): EventRepository = factory.create(ctx.deviceId, ctx.packageVersionName, VERSION_NAME)

}

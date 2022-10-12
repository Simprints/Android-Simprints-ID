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
    abstract fun provideSessionDataCache(impl: SessionDataCacheImpl): SessionDataCache

    @Binds
    abstract fun provideSessionEventValidatorsFactory(impl: SessionEventValidatorsFactoryImpl): SessionEventValidatorsFactory

    @Binds
    abstract fun provideEventRemoteDataSource(impl: EventRemoteDataSourceImpl): EventRemoteDataSource

    @Binds
    abstract fun provideEventDatabaseFactory(impl: DbEventDatabaseFactoryImpl): EventDatabaseFactory

    @Binds
    abstract fun provideEventLocalDataSource(impl: EventLocalDataSourceImpl): EventLocalDataSource

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

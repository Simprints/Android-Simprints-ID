package com.simprints.infra.network

import android.content.Context
import com.simprints.infra.network.connectivity.ConnectivityTrackerImpl
import com.simprints.infra.network.url.BaseUrlProvider
import com.simprints.infra.network.url.BaseUrlProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingsModule {
    @Binds
    internal abstract fun provideSimNetwork(impl: SimNetworkImpl): SimNetwork

    @Binds
    internal abstract fun provideBaseUrlProvider(impl: BaseUrlProviderImpl): BaseUrlProvider

    @Binds
    internal abstract fun provideConnectivityTracker(impl: ConnectivityTrackerImpl): ConnectivityTracker
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val NETWORK_CACHE_SIZE = 10 * 1024 * 1024L // 10mb

    @Provides
    @Singleton
    fun provideNetworkCache(
        @ApplicationContext context: Context,
    ): Cache = Cache(context.cacheDir, NETWORK_CACHE_SIZE)
}

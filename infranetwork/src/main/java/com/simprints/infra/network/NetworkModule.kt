package com.simprints.infra.network

import com.simprints.infra.network.url.BaseUrlProvider
import com.simprints.infra.network.url.BaseUrlProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    internal abstract fun provideSimNetwork(impl: SimNetworkImpl): SimNetwork

    @Binds
    internal abstract fun provideBaseUrlProvider(impl: BaseUrlProviderImpl): BaseUrlProvider

}

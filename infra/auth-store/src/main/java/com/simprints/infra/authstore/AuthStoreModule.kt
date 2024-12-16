package com.simprints.infra.authstore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthStoreModule {
    @Binds
    internal abstract fun provideAuthStore(authStore: AuthStoreImpl): AuthStore
}

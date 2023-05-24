package com.simprints.infra.login

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginManagerModule {

    @Binds
    internal abstract fun provideLoginManager(loginManager: LoginManagerImpl): LoginManager

}

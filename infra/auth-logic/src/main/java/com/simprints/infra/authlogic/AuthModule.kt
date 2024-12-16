package com.simprints.infra.authlogic

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    internal abstract fun provideAuthenticator(authManager: AuthManagerImpl): AuthManager
}

@Module
@InstallIn(SingletonComponent::class)
object IntegrityModule {
    @Provides
    fun provideIntegrityManager(
        @ApplicationContext context: Context,
    ) = IntegrityManagerFactory.create(context)
}

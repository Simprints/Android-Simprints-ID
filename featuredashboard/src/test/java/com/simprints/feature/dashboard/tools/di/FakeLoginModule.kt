package com.simprints.feature.dashboard.tools.di

import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.LoginManagerModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [LoginManagerModule::class]
)
object FakeLoginModule {

    @Provides
    @Singleton
    fun provideLoginManager(): LoginManager = mockk()
}

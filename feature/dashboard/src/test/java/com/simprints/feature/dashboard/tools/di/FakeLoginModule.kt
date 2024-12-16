package com.simprints.feature.dashboard.tools.di

import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.AuthStoreModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AuthStoreModule::class],
)
object FakeLoginModule {
    @Provides
    @Singleton
    fun provideAuthStore(): AuthStore = mockk()
}

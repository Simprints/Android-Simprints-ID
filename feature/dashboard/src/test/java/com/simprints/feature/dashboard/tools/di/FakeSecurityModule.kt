package com.simprints.feature.dashboard.tools.di

import android.content.SharedPreferences
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.SecurityModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SecurityModule::class],
)
object FakeSecurityModule {
    @Provides
    @Singleton
    fun provideSecurityManager(): SecurityManager = mockk {
        every { buildEncryptedSharedPreferences(any()) } returns mockk<SharedPreferences>(relaxed = true)
    }
}

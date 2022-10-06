package com.simprints.id.testtools.di

import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.ConfigManagerModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [ActivityComponent::class],
    replaces = [ConfigManagerModule::class]
)
object TestConfigManagerModule {

    @Singleton
    @Provides
    fun provideConfigManager(): ConfigManager = mockk(relaxed = true)

}

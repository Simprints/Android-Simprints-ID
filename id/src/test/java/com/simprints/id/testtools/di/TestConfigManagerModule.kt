package com.simprints.id.testtools.di

import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.ConfigManagerModule
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Inject
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [ActivityComponent::class],
    replaces = [ConfigManagerModule::class]
)
object TestConfigManagerModule {

    @Singleton
    @Provides
    fun provideConfigManager(): ConfigManager = mockk()

}

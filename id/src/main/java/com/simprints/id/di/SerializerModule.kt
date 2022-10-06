package com.simprints.id.di

import com.simprints.core.tools.json.JsonHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

// TODO: Remove after hilt migration
@DisableInstallInCheck
@Module
class SerializerModule {
    @Provides
    @Singleton
    fun provideJsonHelper(): JsonHelper = JsonHelper
}

package com.simprints.feature.dashboard

import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.ModuleRepository
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.ModuleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {
    @Binds
    internal abstract fun provideModuleRepository(impl: ModuleRepositoryImpl): ModuleRepository
}

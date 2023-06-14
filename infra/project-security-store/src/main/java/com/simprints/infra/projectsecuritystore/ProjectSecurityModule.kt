package com.simprints.infra.projectsecuritystore

import com.simprints.infra.projectsecuritystore.securitystate.repo.SecurityStateRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProjectSecurityModule {
    @Binds
    internal abstract fun provideSecurityStateRepository(impl: SecurityStateRepositoryImpl): SecurityStateRepository
}
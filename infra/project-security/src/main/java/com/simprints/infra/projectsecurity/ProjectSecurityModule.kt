package com.simprints.infra.projectsecurity

import com.simprints.infra.projectsecurity.securitystate.repo.SecurityStateRepository
import com.simprints.infra.projectsecurity.securitystate.repo.SecurityStateRepositoryImpl
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
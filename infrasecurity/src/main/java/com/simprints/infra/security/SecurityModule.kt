package com.simprints.infra.security

import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProviderImpl
import com.simprints.infra.security.random.RandomGenerator
import com.simprints.infra.security.random.RandomGeneratorImpl
import com.simprints.infra.security.root.RootManager
import com.simprints.infra.security.root.RootManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class SecurityModule {

    @Binds
    internal abstract fun bindSecureLocalDbKeyProvider(impl: SecureLocalDbKeyProviderImpl): SecureLocalDbKeyProvider

    @Binds
    internal abstract fun bindRandomGenerator(impl: RandomGeneratorImpl): RandomGenerator

    @Binds
    internal abstract fun bindRootManager(impl: RootManagerImpl): RootManager

}


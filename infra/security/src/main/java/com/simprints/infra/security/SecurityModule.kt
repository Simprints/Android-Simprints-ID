package com.simprints.infra.security

import android.content.Context
import android.os.Build
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilder
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilderImpl
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesProvider
import com.simprints.infra.security.keyprovider.MasterKeyProvider
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProviderImpl
import com.simprints.infra.security.random.RandomGenerator
import com.simprints.infra.security.random.RandomGeneratorImpl
import com.simprints.infra.security.root.RootManager
import com.simprints.infra.security.root.RootManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    companion object {
        @BuildSdk
        @Provides
        internal fun provideBuildSdk(): Int = Build.VERSION.SDK_INT

        @Provides
        internal fun bindEncryptedSharedPreferences(
            @ApplicationContext context: Context,
        ): EncryptedSharedPreferencesProvider = EncryptedSharedPreferencesProvider(ctx = context)

        @Provides
        internal fun bindMasterKeyProvider(): MasterKeyProvider = MasterKeyProvider()
    }

    @Binds
    internal abstract fun bindSecureLocalDbKeyProvider(impl: SecureLocalDbKeyProviderImpl): SecureLocalDbKeyProvider

    @Binds
    internal abstract fun bindRandomGenerator(impl: RandomGeneratorImpl): RandomGenerator

    @Binds
    internal abstract fun bindRootManager(impl: RootManagerImpl): RootManager

    @Binds
    internal abstract fun bindEncryptedSharedPreferencesBuilder(
        impl: EncryptedSharedPreferencesBuilderImpl,
    ): EncryptedSharedPreferencesBuilder

    @Binds
    internal abstract fun bindSecurityManager(impl: SecurityManagerImpl): SecurityManager
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BuildSdk

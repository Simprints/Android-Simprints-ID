package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.biosdkimpl.SimprintsSdk
import com.simprints.fingerprint.infra.necsdkimpl.NecSdk
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FingerprintBioSdkModule {

    @Binds
    @SimprintsSdk
    @Singleton
    abstract fun provideSimprintsBioSdkWrapper(impl: SimprintsBioSdkWrapper): BioSdkWrapper

    @Binds
    @NecSdk
    @Singleton
    abstract fun provideNecBioSdkWrapper(impl: NECBioSdkWrapper): BioSdkWrapper
}

@Module
@InstallIn(SingletonComponent::class)
internal object FingerprintBioSdkProvidersModule {


    @Provides
    @Singleton
    fun provideBioSdkWrapper(
        @SimprintsSdk simprintsWrapper: BioSdkWrapper,
        @NecSdk necWrapper: BioSdkWrapper,
        configManager: ConfigRepository
    ): BioSdkWrapper = runBlocking {
        // Todo we didn't yet implement the logic to select the SDK based on the configuration
        // so we are just using the first allowed SDK for now
        // See tickets in SIM-81 for more details
        when (configManager.getProjectConfiguration().fingerprint?.allowedSDKs?.first()) {

            FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER -> simprintsWrapper
            FingerprintConfiguration.BioSdk.NEC -> necWrapper
            else -> {
                throw IllegalStateException("Unknown fingerprint configuration")
            }
        }
    }
}

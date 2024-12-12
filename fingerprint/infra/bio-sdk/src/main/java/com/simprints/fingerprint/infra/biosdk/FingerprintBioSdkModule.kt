package com.simprints.fingerprint.infra.biosdk

import com.simprints.fingerprint.infra.biosdkimpl.SimprintsSdk
import com.simprints.fingerprint.infra.necsdkimpl.NecSdk
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

package com.simprints.fingerprint.infra.biosdk

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FingerprintBioSdkModule {

    // TODO Consider replacing this with a provider method that would inject
    //  specific wrappers based on project configuration.

    @Binds
    abstract fun provideBioSdkWrapper(impl: SimprintsBioSdkWrapper): BioSdkWrapper
}

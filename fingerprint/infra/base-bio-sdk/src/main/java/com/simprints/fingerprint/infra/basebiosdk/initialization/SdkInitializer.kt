package com.simprints.fingerprint.infra.basebiosdk.initialization

fun interface SdkInitializer<SdkConfig> {
    /**
     * Initialize the SDK with the given parameters.
     *
     * @param initializationParams The parameters to initialize the SDK with.
     * throws BioSdkInitializationException if the SDK fails to initialize.
     */
    suspend fun initialize(initializationParams: SdkConfig?)
}

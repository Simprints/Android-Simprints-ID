package com.simprints.fingerprint.infra.basebiosdk.initialization

fun interface SdkInitializer<SdkConfig> {
    suspend fun initialize(initializationParams:SdkConfig?)
}

package com.simprints.fingerprint.infra.basebiosdk.initialization

fun interface SdkInitializer<SdkConfig> {
    fun initialize(initializationParams:SdkConfig?)
}

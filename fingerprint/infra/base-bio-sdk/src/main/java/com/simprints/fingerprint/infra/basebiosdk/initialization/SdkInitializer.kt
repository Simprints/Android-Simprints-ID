package com.simprints.fingerprint.infra.basebiosdk.initialization

fun interface SdkInitializer {
    fun initialize(initializationParams: Map<String, Any>)
}

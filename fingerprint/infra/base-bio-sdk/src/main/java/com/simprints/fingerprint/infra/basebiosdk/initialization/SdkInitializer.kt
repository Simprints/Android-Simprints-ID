package com.simprints.fingerprint.infra.basebiosdk.initialization

fun interface SdkInitializer {
    fun initialize(params: Map<String, Any>)
}

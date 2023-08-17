package com.simprints.fingerprint.infra.basebiosdk.initialization

fun interface SdkInitializer {
    fun init(license: String,params: Map<String, Any>)
}

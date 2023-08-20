package com.simprints.fingerprint.controllers.fingerprint.config

import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import javax.inject.Inject

class ConfigurationController @Inject constructor(
 private val  fingerprintBioSdk: FingerprintBioSdk
){

    fun runConfiguration(@Suppress("UNUSED_PARAMETER") taskRequest: ConfigurationTaskRequest): ConfigurationTaskResult {
        //Todo: initialize the fingerprintBioSdk with proper input params when implementing
        // the template and image acquisition features in the bio-sdk-impl module
        fingerprintBioSdk.initialize(emptyMap())
        return ConfigurationTaskResult()
    }
}

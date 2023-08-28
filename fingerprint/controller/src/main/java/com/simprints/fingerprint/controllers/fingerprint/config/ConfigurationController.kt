package com.simprints.fingerprint.controllers.fingerprint.config

import com.simprints.core.ExternalScope
import com.simprints.fingerprint.biosdk.SimprintsBioSdkWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfigurationController @Inject constructor(
    private val simprintsBioSdkWrapper: SimprintsBioSdkWrapper,
    @ExternalScope private val externalScope: CoroutineScope,

    ) {

    fun runConfiguration(@Suppress("UNUSED_PARAMETER") taskRequest: ConfigurationTaskRequest): ConfigurationTaskResult {
        externalScope.launch {
            simprintsBioSdkWrapper.initialize()
        }
        return ConfigurationTaskResult()
    }
}

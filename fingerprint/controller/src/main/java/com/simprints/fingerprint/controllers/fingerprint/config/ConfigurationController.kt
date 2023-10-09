package com.simprints.fingerprint.controllers.fingerprint.config

import javax.inject.Inject

class ConfigurationController @Inject constructor() {

    fun runConfiguration(@Suppress("UNUSED_PARAMETER") taskRequest: ConfigurationTaskRequest): ConfigurationTaskResult {
        return ConfigurationTaskResult()
    }
}

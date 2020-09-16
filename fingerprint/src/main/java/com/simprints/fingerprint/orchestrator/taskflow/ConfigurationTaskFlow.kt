package com.simprints.fingerprint.orchestrator.taskflow

import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationTaskRequest
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationTaskResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.FinalResultBuilder
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintConfigurationRequest
import com.simprints.fingerprint.orchestrator.models.FinalResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask

class ConfigurationTaskFlow(configureRequest: FingerprintConfigurationRequest) : FingerprintTaskFlow(configureRequest) {

    init {
        fingerprintTasks = listOf(
            FingerprintTask.Configuration(CONFIGURATION_TASK_KEY) { createConfigurationTaskRequest() }
        )
    }

    private fun createConfigurationTaskRequest() =
        ConfigurationTaskRequest()

    override fun getFinalOkResult(finalResultBuilder: FinalResultBuilder): FinalResult =
        finalResultBuilder.createConfigurationResult(taskResults[CONFIGURATION_TASK_KEY] as ConfigurationTaskResult)

    companion object {
        private const val CONFIGURATION_TASK_KEY = "configuration"
    }
}

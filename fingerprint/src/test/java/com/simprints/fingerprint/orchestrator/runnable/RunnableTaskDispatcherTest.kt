package com.simprints.fingerprint.orchestrator.runnable

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationController
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationTaskRequest
import com.simprints.fingerprint.controllers.fingerprint.config.ConfigurationTaskResult
import com.simprints.fingerprint.orchestrator.task.FingerprintTask
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class RunnableTaskDispatcherTest {

    private val configurationControllerMock = mockk<ConfigurationController>()
    private val runnableTaskDispatcher = RunnableTaskDispatcher(configurationControllerMock)

    @Test
    fun dispatchConfigurationTask_finishesNormally_savesResultCorrectly() {
        val taskRequest = ConfigurationTaskRequest()
        val taskResult = ConfigurationTaskResult()
        every { configurationControllerMock.runConfiguration(eq(taskRequest)) } returns taskResult

        var result: ConfigurationTaskResult? = null
        runnableTaskDispatcher.dispatch(FingerprintTask.Configuration("config") { taskRequest }) {
            result = it as ConfigurationTaskResult?
        }

        assertThat(result).isEqualTo(taskResult)
    }

    @Test
    fun dispatchConfigurationTask_throws_returnsNull() {
        val taskRequest = ConfigurationTaskRequest()
        every { configurationControllerMock.runConfiguration(eq(taskRequest)) } throws Exception("Oops")

        var result: ConfigurationTaskResult? = null
        runnableTaskDispatcher.dispatch(FingerprintTask.Configuration("config") { taskRequest }) {
            result = it as ConfigurationTaskResult?
        }

        assertThat(result).isNull()
    }
}

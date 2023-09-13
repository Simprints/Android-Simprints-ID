package com.simprints.fingerprint.controllers.fingerprint.config

import com.simprints.fingerprint.biosdk.SimprintsBioSdkWrapper
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfigurationControllerTest {

    @Test
    fun runConfiguration() = runTest {
        val bioSdkWrapper = mockk<SimprintsBioSdkWrapper>{
            coJustRun { initialize() }
        }
        val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
        val configurationController = ConfigurationController(bioSdkWrapper, coroutineScope)
        configurationController.runConfiguration(ConfigurationTaskRequest())
        coVerify { bioSdkWrapper.initialize() }
    }
}

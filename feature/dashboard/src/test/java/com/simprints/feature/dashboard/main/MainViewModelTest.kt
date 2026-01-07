package com.simprints.feature.dashboard.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val configRepository = mockk<ConfigRepository> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { consent } returns mockk {
                every { collectConsent } returns true
            }
        }
    }
    private val securityManager = mockk<SecurityManager>(relaxed = true)

    @Test
    fun `should initialize the live data correctly`() {
        val viewModel = MainViewModel(configRepository, securityManager)

        assertThat(viewModel.consentRequired.value).isEqualTo(true)
    }

    @Test
    fun `should show rooted device detected if device is rooted`() {
        coEvery { securityManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        val viewModel = MainViewModel(configRepository, securityManager)

        assertThat(viewModel.rootedDeviceDetected.value).isNotNull()
    }
}

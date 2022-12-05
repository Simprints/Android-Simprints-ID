package com.simprints.feature.dashboard.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { consent } returns mockk {
                every { collectConsent } returns true
            }
        }
    }

    @Test
    fun `should initialize the live data correctly`() {
        val viewModel = MainViewModel(configManager, testCoroutineRule.testCoroutineDispatcher)

        assertThat(viewModel.consentRequired.value).isEqualTo(true)
    }
}

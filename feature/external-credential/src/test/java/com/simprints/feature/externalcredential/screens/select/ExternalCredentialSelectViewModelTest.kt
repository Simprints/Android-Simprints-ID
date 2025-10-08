package com.simprints.feature.externalcredential.screens.select

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ExternalCredentialSelectViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    private lateinit var viewModel: ExternalCredentialSelectViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = ExternalCredentialSelectViewModel(configManager)
    }

    @Test
    fun `loads allowed external credentials`() = runTest {
        val expected = listOf(
            ExternalCredentialType.NHISCard,
            ExternalCredentialType.GhanaIdCard,
        )
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { multifactorId } returns mockk {
                every { allowedExternalCredentials } returns expected
            }
        }

        val observer = viewModel.externalCredentialTypes.test()
        viewModel.loadExternalCredentials()

        assertThat(observer.value()).isEqualTo(expected)
    }

    @Test
    fun `sets empty list if no allowed credentials`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { multifactorId } returns mockk {
                every { allowedExternalCredentials } returns emptyList()
            }
        }

        val observer = viewModel.externalCredentialTypes.test()
        viewModel.loadExternalCredentials()

        assertThat(observer.value()).isEmpty()
    }
}

package com.simprints.feature.externalcredential.screens.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ExternalCredentialViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager
    private lateinit var viewModel: ExternalCredentialViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = ExternalCredentialViewModel(configManager = configManager)
    }

    @Test
    fun `initial state is EMPTY`() {
        val observer = viewModel.stateLiveData.test()
        assertThat(observer.value()).isEqualTo(ExternalCredentialState.EMPTY)
    }

    @Test
    fun `setSelectedExternalCredentialType updates state`() {
        val observer = viewModel.stateLiveData.test()

        viewModel.setSelectedExternalCredentialType(ExternalCredentialType.GhanaIdCard)

        assertThat(observer.value()?.selectedType).isEqualTo(ExternalCredentialType.GhanaIdCard)
    }

    @Test
    fun `setSelectedExternalCredentialType null resets state`() {
        val observer = viewModel.stateLiveData.test()

        viewModel.setSelectedExternalCredentialType(null)

        assertThat(observer.value()?.selectedType).isNull()
    }

    @Test
    fun `setExternalCredentialValue updates state`() {
        val observer = viewModel.stateLiveData.test()
        val value = "value"
        viewModel.setExternalCredentialValue(value)
        assertThat(observer.value()?.credentialValue).isEqualTo(value)
    }

    @Test
    fun `init sets state only once`() {
        val observer = viewModel.stateLiveData.test()
        val subjectId = "subjectId"
        val flowType = FlowType.IDENTIFY
        val params = createParams(subjectId = subjectId, flowType)
        val paramsSecond = createParams(subjectId = "other", flowType)

        viewModel.init(params)
        val firstState = observer.value()

        viewModel.init(paramsSecond)

        assertThat(observer.value()).isEqualTo(firstState)
        assertThat(observer.value()?.subjectId).isEqualTo(subjectId)
        assertThat(observer.value()?.flowType).isEqualTo(flowType)
    }

    @Test
    fun `finish sends result to finishEvent`() {
        val observer = viewModel.finishEvent.test()
        val mockResult = mockk<ExternalCredentialSearchResult>(relaxed = true)
        viewModel.finish(mockResult)

        assertThat(observer.value()).isNotNull()
        assertThat(observer.value()?.peekContent()).isEqualTo(mockResult)
    }

    @Test
    fun `init block loads allowed external credentials from config`() = runTest {
        val allowedCredentials = listOf(
            ExternalCredentialType.NHISCard,
            ExternalCredentialType.GhanaIdCard,
        )
        setupProjectConfig(allowedCredentials = allowedCredentials)
        val viewModel = ExternalCredentialViewModel(configManager = configManager)
        val observer = viewModel.externalCredentialTypes.test()
        assertThat(observer.value()).isEqualTo(allowedCredentials)
    }

    @Test
    fun `init block sets empty list if no allowed credentials configured`() = runTest {
        setupProjectConfig(allowedCredentials = emptyList())
        val viewModel = ExternalCredentialViewModel(configManager = configManager)
        val observer = viewModel.externalCredentialTypes.test()
        assertThat(observer.value()).isEmpty()
    }

    private fun setupProjectConfig(allowedCredentials: List<ExternalCredentialType>) {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { multifactorId } returns mockk {
                every { allowedExternalCredentials } returns allowedCredentials
            }
        }
    }

    private fun createParams(
        subjectId: String,
        flowType: FlowType,
    ) = ExternalCredentialParams(
        subjectId = subjectId,
        flowType = flowType,
        ageGroup = null,
        probeReferenceId = null,
        faceSamples = emptyList(),
        fingerprintSamples = emptyList(),
    )
}

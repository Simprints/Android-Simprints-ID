package com.simprints.feature.externalcredential.screens.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var configManager: ConfigManager
    private lateinit var viewModel: ExternalCredentialViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = ExternalCredentialViewModel(configManager = configManager, timeHelper = timeHelper, eventRepository = eventRepository)
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
    fun `finish sends result to finishEvent`() = runTest {
        val mockResult = mockk<ExternalCredentialSearchResult>(relaxed = true) {
            every { scannedCredential } returns null
        }
        viewModel.finish(mockResult)
        val observer = viewModel.finishEvent
            .test()
            .value()
            .getContentIfNotHandled()

        assertThat(observer).isNotNull()
        assertThat(observer).isEqualTo(mockResult)
    }

    @Test
    fun `finish handles non-null scannedCredential in result`() = runTest {
        val subjectId = "subjectId"
        val flowType = FlowType.IDENTIFY
        val params = createParams(subjectId, flowType)
        val credentialSearchResult = mockk<ExternalCredentialSearchResult>(relaxed = true) {
            every { scannedCredential } returns createScannedCredential()
        }
        viewModel.init(params)
        viewModel.finish(credentialSearchResult)

        val observer = viewModel.finishEvent
            .test()
            .value()
            .getContentIfNotHandled()

        assertThat(observer).isEqualTo(credentialSearchResult)
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(ofType<ExternalCredentialCaptureValueEvent>()) }
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(ofType<ExternalCredentialCaptureEvent>()) }
    }

    @Test
    fun `init block loads allowed external credentials from config`() = runTest {
        val allowedCredentials = listOf(
            ExternalCredentialType.NHISCard,
            ExternalCredentialType.GhanaIdCard,
        )
        val viewModel = setupViewModel(allowedCredentials = allowedCredentials)
        val observer = viewModel.externalCredentialTypes.test()
        assertThat(observer.value()).isEqualTo(allowedCredentials)
    }

    @Test
    fun `init block sets empty list if no allowed credentials configured`() = runTest {
        val viewModel = setupViewModel(allowedCredentials = emptyList())
        val observer = viewModel.externalCredentialTypes.test()
        assertThat(observer.value()).isEmpty()
    }

    private fun setupViewModel(allowedCredentials: List<ExternalCredentialType>): ExternalCredentialViewModel {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { multifactorId } returns mockk {
                every { allowedExternalCredentials } returns allowedCredentials
            }
        }
        return ExternalCredentialViewModel(configManager = configManager, timeHelper = timeHelper, eventRepository = eventRepository)
    }

    private fun createScannedCredential(
        credential: String = "credential",
        credentialType: ExternalCredentialType = ExternalCredentialType.NHISCard,
        documentImagePath: String? = "documentImagePath",
        zoomedCredentialImagePath: String? = "zoomedCredentialImagePath",
        credentialBoundingBox: BoundingBox? = BoundingBox(0, 0, 100, 100),
    ) = ScannedCredential(
        credential = credential.asTokenizableEncrypted(),
        credentialType = credentialType,
        documentImagePath = documentImagePath,
        zoomedCredentialImagePath = zoomedCredentialImagePath,
        credentialBoundingBox = credentialBoundingBox,
    )

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

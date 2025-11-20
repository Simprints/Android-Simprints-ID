package com.simprints.feature.externalcredential.screens.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.usecase.ExternalCredentialEventTrackerUseCase
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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
    lateinit var eventsTracker: ExternalCredentialEventTrackerUseCase

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var configManager: ConfigManager
    private lateinit var viewModel: ExternalCredentialViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = ExternalCredentialViewModel(
            configManager = configManager,
            timeHelper = timeHelper,
            eventsTracker = eventsTracker,
        )
        every { timeHelper.now() } returns Timestamp(1L)
    }

    @Test
    fun `initial state is EMPTY`() {
        val observer = viewModel.stateLiveData.test()
        assertThat(observer.value()).isEqualTo(ExternalCredentialState.EMPTY)
    }

    @Test
    fun `setSelectedExternalCredentialType updates state`() {
        val observer = viewModel.stateLiveData.test()

        viewModel.selectionStarted()
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
        viewModel.selectionStarted()
        viewModel.setSelectedExternalCredentialType(ExternalCredentialType.QRCode) // init capture timer
        viewModel.finish(credentialSearchResult)

        val observer = viewModel.finishEvent
            .test()
            .value()
            .getContentIfNotHandled()

        assertThat(observer).isEqualTo(credentialSearchResult)
    }

    @Test
    fun `finish saves success flow events`() = runTest {
        val mockResult = mockk<ExternalCredentialSearchResult>(relaxed = true) {
            every { scannedCredential } returns mockk(relaxed = true)
        }
        coEvery { eventsTracker.saveSelectionEvent(any(), any(), any()) } returns "selectionId"

        viewModel.selectionStarted()
        viewModel.init(createParams(subjectId = "subjectId", FlowType.IDENTIFY))
        viewModel.setSelectedExternalCredentialType(ExternalCredentialType.QRCode) // init capture timer
        viewModel.finish(mockResult)

        coVerify { eventsTracker.saveSelectionEvent(any(), any(), any()) }
        coVerify { eventsTracker.saveCaptureEvents(any(), any(), any(), any()) }
    }

    @Test
    fun `finish saves skip event`() = runTest {
        val mockResult = mockk<ExternalCredentialSearchResult>(relaxed = true) {
            every { scannedCredential } returns null
        }
        viewModel.selectionStarted()
        viewModel.skipOptionSelected(ExternalCredentialSelectionEvent.SkipReason.OTHER)
        viewModel.skipOtherReasonChanged("other")
        viewModel.finish(mockResult)

        coVerify { eventsTracker.saveSkippedEvent(any(), any(), any()) }
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
        return ExternalCredentialViewModel(
            configManager = configManager,
            timeHelper = timeHelper,
            eventsTracker = eventsTracker,
        )
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
        scanStartTime = Timestamp(1L),
        scanEndTime = Timestamp(2L),
        scannedValue = credential.asTokenizableRaw(),
    )

    private fun createParams(
        subjectId: String,
        flowType: FlowType,
    ) = ExternalCredentialParams(
        subjectId = subjectId,
        flowType = flowType,
        ageGroup = null,
        probeReferenceId = null,
        samples = emptyMap(),
    )
}

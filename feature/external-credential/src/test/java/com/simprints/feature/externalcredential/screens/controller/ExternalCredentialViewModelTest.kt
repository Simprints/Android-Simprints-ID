package com.simprints.feature.externalcredential.screens.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.feature.externalcredential.usecase.ExternalCredentialEventTrackerUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
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
    lateinit var eventsTracker: ExternalCredentialEventTrackerUseCase

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var configRepository: ConfigRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ExternalCredentialViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        savedStateHandle = SavedStateHandle()
        viewModel = ExternalCredentialViewModel(
            configRepository = configRepository,
            timeHelper = timeHelper,
            eventsTracker = eventsTracker,
            savedStateHandle = savedStateHandle,
        )
        every { timeHelper.now() } returns Timestamp(1L)
    }

    @Test
    fun `selectionStarted persists selectionStartTime to savedStateHandle`() {
        val expected = Timestamp(11L)
        every { timeHelper.now() } returns expected

        viewModel.selectionStarted()

        assertThat(savedStateHandle.get<Timestamp>(ExternalCredentialViewModel.KEY_SELECTION_START_TIME))
            .isEqualTo(expected)
    }

    @Test
    fun `setSelectedExternalCredentialType persists selection event id and capture start time`() = runTest {
        val selectionStartTime = Timestamp(10L)
        val selectionEndTime = Timestamp(20L)
        val captureStartTime = Timestamp(30L)
        coEvery { eventsTracker.saveSelectionEvent(any(), any(), any()) } returns "selection-id"
        every { timeHelper.now() } returnsMany listOf(selectionStartTime, selectionEndTime, captureStartTime)

        viewModel.selectionStarted()
        viewModel.setSelectedExternalCredentialType(ExternalCredentialType.QRCode)

        assertThat(savedStateHandle.get<String>(ExternalCredentialViewModel.KEY_SELECTION_EVENT_ID))
            .isEqualTo("selection-id")
        assertThat(savedStateHandle.get<Timestamp>(ExternalCredentialViewModel.KEY_CAPTURE_START_TIME))
            .isEqualTo(captureStartTime)
    }

    @Test
    fun `finish complete uses restored selection event state from savedStateHandle`() = runTest {
        val restoredCaptureStartTime = Timestamp(123L)
        val restoredSelectionEventId = "restored-selection-id"
        val restoredStateHandle = SavedStateHandle(
            mapOf(
                ExternalCredentialViewModel.KEY_CAPTURE_START_TIME to restoredCaptureStartTime,
                ExternalCredentialViewModel.KEY_SELECTION_EVENT_ID to restoredSelectionEventId,
            ),
        )
        val restoredViewModel = ExternalCredentialViewModel(
            configRepository = configRepository,
            timeHelper = timeHelper,
            eventsTracker = eventsTracker,
            savedStateHandle = restoredStateHandle,
        )
        val result = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { scannedCredentialResult } returns mockk(relaxed = true)
        }

        restoredViewModel.init(createParams(subjectId = "subjectId", flowType = FlowType.IDENTIFY))
        restoredViewModel.finish(result)

        coVerify {
            eventsTracker.saveCaptureEvents(
                credentialSearchResult = result,
                subjectId = "subjectId",
                startTime = restoredCaptureStartTime,
                selectionEventId = restoredSelectionEventId,
            )
        }
    }

    @Test
    fun `finish skipped uses restored selection start time from savedStateHandle`() = runTest {
        val restoredSelectionStartTime = Timestamp(456L)
        val restoredStateHandle = SavedStateHandle(
            mapOf(ExternalCredentialViewModel.KEY_SELECTION_START_TIME to restoredSelectionStartTime),
        )
        val restoredViewModel = ExternalCredentialViewModel(
            configRepository = configRepository,
            timeHelper = timeHelper,
            eventsTracker = eventsTracker,
            savedStateHandle = restoredStateHandle,
        )
        val result = mockk<ExternalCredentialSearchResult.Skipped>(relaxed = true)

        restoredViewModel.finish(result)

        coVerify {
            eventsTracker.saveSkippedEvent(
                startTime = restoredSelectionStartTime,
                skipReason = result.skipReason,
                skipOther = null,
            )
        }
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
        val mockResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { scannedCredentialResult } returns mockk()
        }
        viewModel.init(createParams(subjectId = "subjectId", FlowType.IDENTIFY))
        viewModel.selectionStarted()
        viewModel.setSelectedExternalCredentialType(ExternalCredentialType.QRCode)
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
        val credentialSearchResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { scannedCredentialResult } returns createScannedCredential()
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
        val mockResult = mockk<ExternalCredentialSearchResult.Complete>(relaxed = true) {
            every { scannedCredentialResult } returns mockk(relaxed = true)
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
        val mockResult = mockk<ExternalCredentialSearchResult.Skipped>(relaxed = true)
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
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { multifactorId } returns mockk {
                every { allowedExternalCredentials } returns allowedCredentials
            }
        }
        return ExternalCredentialViewModel(
            configRepository = configRepository,
            timeHelper = timeHelper,
            eventsTracker = eventsTracker,
            savedStateHandle = SavedStateHandle(),
        )
    }

    private fun createScannedCredential(
        documentImagePath: String? = "documentImagePath",
        zoomedCredentialImagePath: String? = "zoomedCredentialImagePath",
        credentialBoundingBox: BoundingBox? = BoundingBox(0, 0, 100, 100),
    ) = ScannedCredentialResult(
        document = mockk(),
        documentImagePath = documentImagePath,
        zoomedCredentialImagePath = zoomedCredentialImagePath,
        credentialBoundingBox = credentialBoundingBox,
        scanStartTime = Timestamp(1L),
        scanEndTime = Timestamp(2L),
    )

    private fun createParams(
        subjectId: String,
        flowType: FlowType,
    ) = ExternalCredentialParams(
        subjectId = subjectId,
        flowType = flowType,
        ageGroup = null,
        probeReferences = emptyList(),
    )
}

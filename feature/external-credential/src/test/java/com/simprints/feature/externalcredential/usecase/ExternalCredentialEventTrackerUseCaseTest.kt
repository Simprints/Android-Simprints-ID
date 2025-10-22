package com.simprints.feature.externalcredential.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CalculateLevenshteinDistanceUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent.ExternalCredentialConfirmationResult
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent.SkipReason
import com.simprints.infra.events.session.SessionEventRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ExternalCredentialEventTrackerUseCaseTest {
    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    @MockK
    private lateinit var calculateDistance: CalculateLevenshteinDistanceUseCase

    private lateinit var useCase: ExternalCredentialEventTrackerUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ExternalCredentialEventTrackerUseCase(
            timeHelper = timeHelper,
            authStore = authStore,
            configManager = configManager,
            tokenizationProcessor = tokenizationProcessor,
            eventRepository = eventRepository,
            calculateDistance = calculateDistance,
        )

        every { timeHelper.now() } returns END_TIME

        coEvery { authStore.signedInProjectId } returns ""
        coEvery { configManager.getProject(any()) } returns mockk()
        coEvery {
            tokenizationProcessor.decrypt(any(), TokenKeyType.ExternalCredential, any())
        } returns RAW_SCANNED_VALUE.asTokenizableRaw()

        coEvery { calculateDistance(any(), any()) } returns DEFAULT_DISTANCE
    }

    @Test
    fun `saveCaptureEvents should save external credential capture events`() = runTest {
        val scannedCredential = makeScannedCredential(ExternalCredentialType.QRCode)
        useCase.saveCaptureEvents(START_TIME, SUBJECT_ID, scannedCredential, SELECTION_ID)

        val valueEventSlot = slot<ExternalCredentialCaptureValueEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(valueEventSlot)) }
        with(valueEventSlot.captured) {
            assertThat(payload.credential.id).isEqualTo(SCAN_ID)
            assertThat(payload.credential.subjectId).isEqualTo(SUBJECT_ID)
        }

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        with(captureEventSlot.captured) {
            assertThat(payload.id).isEqualTo(SCAN_ID)
            assertThat(payload.createdAt).isEqualTo(START_TIME)
            assertThat(payload.endedAt).isEqualTo(END_TIME)
            assertThat(payload.autoCaptureStartTime).isEqualTo(SCAN_START_TIME)
            assertThat(payload.autoCaptureEndTime).isEqualTo(SCAN_END_TIME)
            assertThat(payload.ocrErrorCount).isEqualTo(DEFAULT_DISTANCE)
            assertThat(payload.capturedTextLength).isEqualTo(RAW_SCANNED_VALUE.length)
        }
    }

    @Test
    fun `saveCaptureEvents should correctly calculate length for NHISCard`() = runTest {
        val scannedCredential = makeScannedCredential(ExternalCredentialType.NHISCard)
        useCase.saveCaptureEvents(START_TIME, SUBJECT_ID, scannedCredential, SELECTION_ID)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        assertThat(captureEventSlot.captured.payload.credentialTextLength).isEqualTo(8)
    }

    @Test
    fun `saveCaptureEvents should correctly calculate length for GhanaIdCard`() = runTest {
        val scannedCredential = makeScannedCredential(ExternalCredentialType.GhanaIdCard)
        useCase.saveCaptureEvents(START_TIME, SUBJECT_ID, scannedCredential, SELECTION_ID)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        assertThat(captureEventSlot.captured.payload.credentialTextLength).isEqualTo(15)
    }

    @Test
    fun `saveCaptureEvents should correctly calculate length for QRCode`() = runTest {
        val scannedCredential = makeScannedCredential(ExternalCredentialType.QRCode)
        useCase.saveCaptureEvents(START_TIME, SUBJECT_ID, scannedCredential, SELECTION_ID)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        assertThat(captureEventSlot.captured.payload.credentialTextLength).isEqualTo(6)
    }

    @Test
    fun `saveSelectionEvent should save correct event`() = runTest {
        useCase.saveSelectionEvent(START_TIME, END_TIME, ExternalCredentialType.QRCode)

        val captureEventSlot = slot<ExternalCredentialSelectionEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        with(captureEventSlot.captured) {
            assertThat(payload.createdAt).isEqualTo(START_TIME)
            assertThat(payload.endedAt).isEqualTo(END_TIME)
            assertThat(payload.credentialType).isEqualTo(ExternalCredentialType.QRCode)
            assertThat(payload.skipReason).isNull()
            assertThat(payload.skipOther).isNull()
        }
    }

    @Test
    fun `saveSkippedEvent should save correct event`() = runTest {
        useCase.saveSkippedEvent(START_TIME, SkipReason.OTHER, "other")

        val captureEventSlot = slot<ExternalCredentialSelectionEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        with(captureEventSlot.captured) {
            assertThat(payload.createdAt).isEqualTo(START_TIME)
            assertThat(payload.endedAt).isEqualTo(END_TIME)
            assertThat(payload.credentialType).isNull()
            assertThat(payload.skipReason).isEqualTo(SkipReason.OTHER)
            assertThat(payload.skipOther).isEqualTo("other")
        }
    }

    @Test
    fun `saveConfirmation should save correct event`() = runTest {
        useCase.saveConfirmation(START_TIME, ExternalCredentialConfirmationResult.CONTINUE)

        val captureEventSlot = slot<ExternalCredentialConfirmationEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        with(captureEventSlot.captured) {
            assertThat(payload.createdAt).isEqualTo(START_TIME)
            assertThat(payload.endedAt).isEqualTo(END_TIME)
            assertThat(payload.result).isEqualTo(ExternalCredentialConfirmationResult.CONTINUE)
        }
    }

    private fun makeScannedCredential(type: ExternalCredentialType) = ScannedCredential(
        credentialScanId = "test-scan-id",
        credential = RAW_SCANNED_VALUE.asTokenizableEncrypted(),
        credentialType = type,
        documentImagePath = null,
        zoomedCredentialImagePath = null,
        credentialBoundingBox = null,
        scanStartTime = SCAN_START_TIME,
        scanEndTime = SCAN_END_TIME,
        scannedValue = RAW_SCANNED_VALUE.asTokenizableRaw(),
    )

    companion object Companion {
        private val START_TIME = Timestamp(0L)
        private val SCAN_START_TIME = Timestamp(3L)
        private val SCAN_END_TIME = Timestamp(4L)
        private val END_TIME = Timestamp(6L)
        private const val SCAN_ID = "test-scan-id"
        private const val SUBJECT_ID = "test-subject-id"
        private const val RAW_SCANNED_VALUE = "scanned-value"
        private const val DEFAULT_DISTANCE = 7
        private const val SELECTION_ID = "selection_id"
    }
}

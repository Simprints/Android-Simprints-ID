package com.simprints.feature.externalcredential.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.ExternalCredentialMapper
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CalculateLevenshteinDistanceUseCase
import com.simprints.feature.externalcredential.screens.search.model.MfidDocument
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent.ExternalCredentialConfirmationResult
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent.SkipReason
import com.simprints.infra.events.event.domain.models.FingerComparisonStrategy
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent
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
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    @MockK
    private lateinit var calculateDistance: CalculateLevenshteinDistanceUseCase

    @MockK
    private lateinit var externalCredentialMapper: ExternalCredentialMapper

    private lateinit var useCase: ExternalCredentialEventTrackerUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ExternalCredentialEventTrackerUseCase(
            timeHelper = timeHelper,
            configRepository = configRepository,
            eventRepository = eventRepository,
            calculateDistance = calculateDistance,
            externalCredentialMapper = externalCredentialMapper,
        )

        every { timeHelper.now() } returns END_TIME

        coEvery { configRepository.getProject() } returns mockk()
        coEvery {
            externalCredentialMapper.mapExternalCredential(any(), any(), any())
        } returns ExternalCredential(
            id = SCAN_ID,
            value = ENCRYPTED_CREDENTIAL,
            subjectId = SUBJECT_ID,
            type = ExternalCredentialType.QRCode,
        )

        coEvery { calculateDistance(any(), any()) } returns DEFAULT_DISTANCE
    }

    @Test
    fun `buildCaptureAttempt maps scanned credential result via mapper`() = runTest {
        val scannedResult = makeScannedCredentialResult(ExternalCredentialType.QRCode)

        useCase.buildCaptureAttempt(scannedResult, SUBJECT_ID, START_TIME, SELECTION_ID)

        coVerify(exactly = 1) {
            externalCredentialMapper.mapExternalCredential(
                scannedCredentialResult = scannedResult,
                subjectId = SUBJECT_ID,
            )
        }
    }

    @Test
    fun `buildCaptureAttempt returns attempt without persisting anything`() = runTest {
        val scannedResult = makeScannedCredentialResult(ExternalCredentialType.QRCode)

        val attempt = useCase.buildCaptureAttempt(scannedResult, SUBJECT_ID, START_TIME, SELECTION_ID)

        assertThat(attempt.scannedCredentialResult).isEqualTo(scannedResult)
        assertThat(attempt.startTime).isEqualTo(START_TIME)
        assertThat(attempt.endTime).isEqualTo(END_TIME)
        assertThat(attempt.selectionEventId).isEqualTo(SELECTION_ID)
        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `hasConfirmedValueChanged returns false when confirmed value matches scanned value`() {
        val attempt = makeAttempt(ExternalCredentialType.QRCode, scannedValue = RAW_SCANNED_VALUE)

        val result = useCase.hasConfirmedValueChanged(attempt, RAW_SCANNED_VALUE.asTokenizableRaw())

        assertThat(result).isFalse()
    }

    @Test
    fun `hasConfirmedValueChanged returns true when confirmed value differs from scanned value`() {
        val attempt = makeAttempt(ExternalCredentialType.QRCode, scannedValue = RAW_SCANNED_VALUE)

        val result = useCase.hasConfirmedValueChanged(attempt, "edited".asTokenizableRaw())

        assertThat(result).isTrue()
    }

    @Test
    fun `persistCaptureAttempt should save external credential capture value event`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.QRCode)
        useCase.persistCaptureAttempt(attempt)

        val valueEventSlot = slot<ExternalCredentialCaptureValueEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(valueEventSlot)) }
        with(valueEventSlot.captured) {
            assertThat(payload.createdAt).isEqualTo(attempt.startTime)
            assertThat(payload.credential).isEqualTo(attempt.externalCredential)
        }
    }

    @Test
    fun `persistCaptureAttempt should save capture event unmodified with zero ocr error count`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.QRCode, scannedValue = RAW_SCANNED_VALUE)
        useCase.persistCaptureAttempt(attempt)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        with(captureEventSlot.captured) {
            assertThat(payload.createdAt).isEqualTo(attempt.startTime)
            assertThat(payload.endedAt).isEqualTo(attempt.endTime)
            assertThat(payload.autoCaptureStartTime).isEqualTo(SCAN_START_TIME)
            assertThat(payload.autoCaptureEndTime).isEqualTo(SCAN_END_TIME)
            assertThat(payload.ocrErrorCount).isEqualTo(0)
            assertThat(payload.capturedTextLength).isEqualTo(RAW_SCANNED_VALUE.length)
            assertThat(payload.selectionId).isEqualTo(SELECTION_ID)
        }
        coVerify(exactly = 0) { calculateDistance(any(), any()) }
    }

    @Test
    fun `persistCaptureAttemptWithConfirmedValue recalculates ocr error count and captured text length`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.QRCode, scannedValue = RAW_SCANNED_VALUE)
        val confirmedValue = "confirmed value".asTokenizableRaw()

        useCase.persistCaptureAttemptWithConfirmedValue(attempt, confirmedValue)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        coVerify(exactly = 1) { calculateDistance(RAW_SCANNED_VALUE, confirmedValue.value) }
        with(captureEventSlot.captured) {
            assertThat(payload.ocrErrorCount).isEqualTo(DEFAULT_DISTANCE)
            assertThat(payload.capturedTextLength).isEqualTo(confirmedValue.value.length)
        }
    }

    @Test
    fun `persistCaptureAttemptWithConfirmedValue still saves the raw scanned value in the value event`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.QRCode, scannedValue = RAW_SCANNED_VALUE)
        val confirmedValue = "confirmed value".asTokenizableRaw()

        useCase.persistCaptureAttemptWithConfirmedValue(attempt, confirmedValue)

        val valueEventSlot = slot<ExternalCredentialCaptureValueEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(valueEventSlot)) }
        assertThat(valueEventSlot.captured.payload.credential).isEqualTo(attempt.externalCredential)
    }

    @Test
    fun `persistCaptureAttempt should correctly calculate expected length for NHISCard`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.NHISCard)
        useCase.persistCaptureAttempt(attempt)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        assertThat(captureEventSlot.captured.payload.credentialTextLength).isEqualTo(8)
    }

    @Test
    fun `persistCaptureAttempt should correctly calculate expected length for GhanaIdCard`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.GhanaIdCard)
        useCase.persistCaptureAttempt(attempt)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        assertThat(captureEventSlot.captured.payload.credentialTextLength).isEqualTo(15)
    }

    @Test
    fun `persistCaptureAttempt should correctly calculate expected length for FaydaCard`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.FaydaCard)
        useCase.persistCaptureAttempt(attempt)

        val captureEventSlot = slot<ExternalCredentialCaptureEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(captureEventSlot)) }
        assertThat(captureEventSlot.captured.payload.credentialTextLength).isEqualTo(16)
    }

    @Test
    fun `persistCaptureAttempt should correctly calculate expected length for QRCode`() = runTest {
        val attempt = makeAttempt(ExternalCredentialType.QRCode)
        useCase.persistCaptureAttempt(attempt)

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

    @Test
    fun `saveMatchEvent should save match event for face SDK`() = runTest {
        val match = makeCredentialMatch(
            faceSdk = FACE_SDK,
            fingerprintSdk = null,
        )

        useCase.saveMatchEvent(START_TIME, match)

        verifyMatchEvent(
            expectedMatcher = FACE_SDK.name,
            expectedFingerStrategy = null,
        )
    }

    @Test
    fun `saveMatchEvent should save match event for fingerprint SDK`() = runTest {
        val fingerprintConfig = mockk<FingerprintConfiguration.FingerprintSdkConfiguration> {
            every { comparisonStrategyForVerification } returns
                FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER
        }

        val projectConfig = mockk<ProjectConfiguration> {
            every { fingerprint } returns mockk {
                every { getSdkConfiguration(FINGERPRINT_SDK) } returns fingerprintConfig
            }
        }

        coEvery { configRepository.getProjectConfiguration() } returns projectConfig

        val match = makeCredentialMatch(
            faceSdk = null,
            fingerprintSdk = FINGERPRINT_SDK,
        )

        useCase.saveMatchEvent(START_TIME, match)

        verifyMatchEvent(
            expectedMatcher = FINGERPRINT_SDK.name,
            expectedFingerStrategy = FingerComparisonStrategy.SAME_FINGER,
        )
    }

    private fun makeDocument(
        type: ExternalCredentialType,
        value: String,
    ): MfidDocument = when (type) {
        ExternalCredentialType.NHISCard -> MfidDocument.GhanaNhisCard(credential = value.asTokenizableRaw())
        ExternalCredentialType.GhanaIdCard -> MfidDocument.GhanaIdCard(credential = value.asTokenizableRaw())
        ExternalCredentialType.QRCode -> MfidDocument.GhanaQrCode(credential = value.asTokenizableRaw())
        ExternalCredentialType.FaydaCard -> MfidDocument.FaydaCard(credential = value.asTokenizableRaw())
    }

    private fun makeScannedCredentialResult(
        type: ExternalCredentialType,
        value: String = RAW_SCANNED_VALUE,
    ) = ScannedCredentialResult(
        credentialScanId = SCAN_ID,
        document = makeDocument(type, value),
        documentImagePath = null,
        zoomedCredentialImagePath = null,
        credentialBoundingBox = null,
        scanStartTime = SCAN_START_TIME,
        scanEndTime = SCAN_END_TIME,
    )

    private fun makeAttempt(
        type: ExternalCredentialType,
        scannedValue: String = RAW_SCANNED_VALUE,
    ) = ExternalCredentialCaptureAttempt(
        scannedCredentialResult = makeScannedCredentialResult(type, scannedValue),
        externalCredential = ExternalCredential(
            id = SCAN_ID,
            value = ENCRYPTED_CREDENTIAL,
            subjectId = SUBJECT_ID,
            type = type,
        ),
        startTime = START_TIME,
        endTime = END_TIME,
        selectionEventId = SELECTION_ID,
    )

    private fun makeCredentialMatch(
        faceSdk: ModalitySdkType?,
        fingerprintSdk: ModalitySdkType?,
    ): CredentialMatch {
        val matchResult = mockk<ComparisonResult> {
            every { subjectId } returns SUBJECT_ID
            every { comparisonScore } returns CONFIDENCE
        }

        val sdk = faceSdk ?: fingerprintSdk!!
        return CredentialMatch(
            credential = ENCRYPTED_CREDENTIAL,
            comparisonResult = matchResult,
            probeReferenceId = PROBE_REFERENCE_ID,
            verificationThreshold = 0.5f,
            bioSdk = sdk,
            matcherName = sdk.name,
        )
    }

    private fun verifyMatchEvent(
        expectedMatcher: String,
        expectedFingerStrategy: FingerComparisonStrategy?,
    ) {
        val slot = slot<OneToOneMatchEvent>()
        coVerify(exactly = 1) { eventRepository.addOrUpdateEvent(capture(slot)) }

        with(slot.captured.payload as OneToOneMatchEvent.OneToOneMatchPayload.OneToOneMatchPayloadV4) {
            assertThat(createdAt).isEqualTo(START_TIME)
            assertThat(endedAt).isEqualTo(END_TIME)
            assertThat(candidateId).isEqualTo(SUBJECT_ID)
            assertThat(matcher).isEqualTo(expectedMatcher)
            assertThat(result?.score).isEqualTo(CONFIDENCE)
            assertThat(fingerComparisonStrategy).isEqualTo(expectedFingerStrategy)
            assertThat(probeBiometricReferenceId).isEqualTo(PROBE_REFERENCE_ID)
        }
    }

    companion object Companion {
        private val START_TIME = Timestamp(0L)
        private val SCAN_START_TIME = Timestamp(3L)
        private val SCAN_END_TIME = Timestamp(4L)
        private val END_TIME = Timestamp(6L)
        private const val SCAN_ID = "test-scan-id"
        private const val SUBJECT_ID = "test-subject-id"
        private const val RAW_SCANNED_VALUE = "scanned"
        private val ENCRYPTED_CREDENTIAL = "encrypted_credential".asTokenizableEncrypted()
        private const val DEFAULT_DISTANCE = 7
        private const val SELECTION_ID = "selection_id"
        private const val CONFIDENCE = 0.9f
        private const val PROBE_REFERENCE_ID = "probe-ref-id"
        private val FACE_SDK = ModalitySdkType.RANK_ONE
        private val FINGERPRINT_SDK = ModalitySdkType.SECUGEN_SIM_MATCHER
    }
}

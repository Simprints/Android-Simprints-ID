package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.SETUP_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.tools.TimeHelperImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowEnrolImplTest {

    companion object {
        const val NUMBER_STEPS_FACE_OR_FINGER = 3
        const val NUMBER_STEPS_FACE_AND_FINGER = 4

        const val NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT = 2
        const val NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT = 3
    }

    private lateinit var modalityFlowEnrol: ModalityFlowEnrolImpl
    private val timeHelper = TimeHelperImpl()
    @MockK lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @MockK lateinit var faceStepProcessor: FaceStepProcessor
    @MockK lateinit var coreStepProcessor: CoreStepProcessor
    @MockK lateinit var fingerprintStepMock: Step
    @MockK lateinit var faceStepMock: Step
    @MockK lateinit var consentStepMock: Step
    @MockK lateinit var setupStepMock: Step
    @MockK lateinit var sessionRepository: SessionRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { consentStepMock.activityName } returns CONSENT_ACTIVITY_NAME
        every { setupStepMock.activityName } returns SETUP_ACTIVITY_NAME

        every { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        every { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentStepMock
        every { coreStepProcessor.buildStepSetup(any()) } returns setupStepMock
    }

    @Test
    fun enrolForFace_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)

        verify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprint_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
        verify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprintWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceFingerprint_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFingerprintFace_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFaceFingerprintWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFingerprintFaceWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
        }
    }

    private fun buildModalityFlowEnrol(consentRequired: Boolean) {
        modalityFlowEnrol = ModalityFlowEnrolImpl(fingerprintStepProcessor, faceStepProcessor,
            coreStepProcessor, timeHelper, sessionRepository, consentRequired, locationRequired = true, isEnrolmentPlus = false, matchGroup = GROUP.GLOBAL)
    }
}

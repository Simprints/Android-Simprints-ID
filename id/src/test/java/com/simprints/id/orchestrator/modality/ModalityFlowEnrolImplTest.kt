package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.verifyNever
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowEnrolImplTest {

    companion object {
        const val NUMBER_STEPS_FACE_OR_FINGER = 2
        const val NUMBER_STEPS_FACE_AND_FINGER = 3
        const val NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT = 1
        const val NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT = 2
    }

    private lateinit var modalityFlowEnrol: ModalityFlowEnrolImpl
    @Mock lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @Mock lateinit var faceStepProcessor: FaceStepProcessor
    @Mock lateinit var coreStepProcessor: CoreStepProcessor
    @Mock lateinit var fingerprintStepMock: Step
    @Mock lateinit var faceStepMock: Step
    @Mock lateinit var coreStepMock: Step
    @Mock lateinit var sessionEventsManager: SessionEventsManager

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(fingerprintStepMock) { activityName } thenReturn FINGERPRINT_ACTIVITY_NAME
        whenever(faceStepMock) { activityName } thenReturn FACE_ACTIVITY_NAME
        whenever(coreStepMock) { activityName } thenReturn CONSENT_ACTIVITY_NAME

        whenever(fingerprintStepProcessor) { buildStepToCapture() } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildCaptureStep() } thenReturn faceStepMock
        whenever(coreStepProcessor) { buildStepConsent(anyNotNull()) } thenReturn coreStepMock
    }

    @Test
    fun enrolForFace_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
        verifyNever(fingerprintStepProcessor) { buildStepToCapture() }
        verifyOnce(faceStepProcessor) { buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprint_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepToCapture() }
        verifyNever(faceStepProcessor) { buildCaptureStep() }
    }

    @Test
    fun enrolForFaceWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
        verifyNever(fingerprintStepProcessor) { buildStepToCapture() }
        verifyOnce(faceStepProcessor) { buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprintWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
        verifyOnce(fingerprintStepProcessor) { buildStepToCapture() }
        verifyNever(faceStepProcessor) { buildCaptureStep() }
    }

    @Test
    fun enrolForFaceFingerprint_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepToCapture() }
        verifyOnce(faceStepProcessor) { buildCaptureStep() }
        assertThat(modalityFlowEnrol.steps[0].activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
        assertThat(modalityFlowEnrol.steps[1].activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun enrolForFingerprintFace_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepToCapture() }
        assertThat(modalityFlowEnrol.steps[0].activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
        assertThat(modalityFlowEnrol.steps[1].activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }

    @Test
    fun enrolForFaceFingerprintWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        verifyOnce(fingerprintStepProcessor) { buildStepToCapture() }
        verifyOnce(faceStepProcessor) { buildCaptureStep() }
        assertThat(modalityFlowEnrol.steps[0].activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun enrolForFingerprintFaceWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false)
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        verifyOnce(fingerprintStepProcessor) { buildStepToCapture() }
        assertThat(modalityFlowEnrol.steps[0].activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }

    private fun buildModalityFlowEnrol(consentRequired: Boolean) {
        modalityFlowEnrol = ModalityFlowEnrolImpl(fingerprintStepProcessor, faceStepProcessor,
            coreStepProcessor, sessionEventsManager, consentRequired)
    }
}

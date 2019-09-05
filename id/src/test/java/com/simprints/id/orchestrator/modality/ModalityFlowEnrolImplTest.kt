package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CORE_ACTIVITY_NAME
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
    }

    private lateinit var modalityFlowEnrol: ModalityFlowEnrolImpl
    @Mock lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @Mock lateinit var faceStepProcessor: FaceStepProcessor
    @Mock lateinit var coreStepProcessor: CoreStepProcessor
    @Mock lateinit var fingerprintStepMock: Step
    @Mock lateinit var faceStepMock: Step
    @Mock lateinit var coreStepMock: Step

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(fingerprintStepMock) { activityName } thenReturn FINGERPRINT_ACTIVITY_NAME
        whenever(faceStepMock) { activityName } thenReturn FACE_ACTIVITY_NAME
        whenever(coreStepMock) { activityName } thenReturn CORE_ACTIVITY_NAME

        whenever(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn faceStepMock
        whenever(coreStepProcessor) { buildStepEnrolOrIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn coreStepMock

        modalityFlowEnrol = ModalityFlowEnrolImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor)
    }

    @Test
    fun enrolForFace_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
        verifyNever(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun enrolForFingerprint_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyNever(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun enrolForFaceFingerprint_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowEnrol.steps[0].activityName).isEqualTo(CORE_ACTIVITY_NAME)
        assertThat(modalityFlowEnrol.steps[1].activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun enrolForFingerprintFace_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowEnrol.steps[0].activityName).isEqualTo(CORE_ACTIVITY_NAME)
        assertThat(modalityFlowEnrol.steps[1].activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }
}

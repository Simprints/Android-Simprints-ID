package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.steps.Step
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

    private lateinit var modalityFlowEnrol: ModalityFlowEnrolImpl
    @Mock lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @Mock lateinit var faceStepProcessor: FaceStepProcessor
    @Mock lateinit var fingerprintStepMock: Step
    @Mock lateinit var faceStepMock: Step

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(fingerprintStepMock) { activityName } thenReturn FINGERPRINT_ACTIVITY_NAME
        whenever(faceStepMock) { activityName } thenReturn FACE_ACTIVITY_NAME

        whenever(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn faceStepMock

        modalityFlowEnrol = ModalityFlowEnrolImpl(fingerprintStepProcessor, faceStepProcessor)
    }

    @Test
    fun enrolForFace_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(1)
        verifyNever(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun enrolForFingerprint_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(1)
        verifyOnce(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyNever(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun enrolForFaceFingerprint_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowEnrol.steps).hasSize(2)
        verifyOnce(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowEnrol.steps.first().activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun enrolForFingerprintFace_shouldCreateTheRightSteps() {
        modalityFlowEnrol.startFlow(enrolAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowEnrol.steps).hasSize(2)
        verifyOnce(fingerprintStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepEnrol(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowEnrol.steps.first().activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }
}

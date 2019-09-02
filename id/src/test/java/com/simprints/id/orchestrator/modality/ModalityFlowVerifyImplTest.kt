package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.verifyAppRequest
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

class ModalityFlowVerifyImplTest {

    private lateinit var modalityFlowVerify: ModalityFlowVerifyImpl
    @Mock lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @Mock lateinit var faceStepProcessor: FaceStepProcessor
    @Mock lateinit var coreStepProcessor: CoreStepProcessor
    @Mock lateinit var fingerprintStepMock: Step
    @Mock lateinit var faceStepMock: Step

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(fingerprintStepMock) { activityName } thenReturn FINGERPRINT_ACTIVITY_NAME
        whenever(faceStepMock) { activityName } thenReturn FACE_ACTIVITY_NAME

        whenever(fingerprintStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn faceStepMock

        modalityFlowVerify = ModalityFlowVerifyImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor)
    }

    @Test
    fun verifyForFace_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FACE))

        assertThat(modalityFlowVerify.steps).hasSize(1)
        verifyNever(fingerprintStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun verifyForFingerprint_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FINGER))

        assertThat(modalityFlowVerify.steps).hasSize(1)
        verifyOnce(fingerprintStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyNever(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun verifyForFaceFingerprint_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowVerify.steps).hasSize(2)
        verifyOnce(fingerprintStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowVerify.steps.first().activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun verifyForFingerprintFace_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowVerify.steps).hasSize(2)
        verifyOnce(fingerprintStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowVerify.steps.first().activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }
}

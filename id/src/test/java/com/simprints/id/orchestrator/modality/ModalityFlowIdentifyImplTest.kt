package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
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

class ModalityFlowIdentifyImplTest {

    private lateinit var modalityFlowIdentify: ModalityFlowIdentifyImpl
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

        whenever(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn faceStepMock

        modalityFlowIdentify = ModalityFlowIdentifyImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor)
    }

    @Test
    fun identifyForFace_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE))

        assertThat(modalityFlowIdentify.steps).hasSize(1)
        verifyNever(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun identifyForFingerprint_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER))

        assertThat(modalityFlowIdentify.steps).hasSize(1)
        verifyOnce(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyNever(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun identifyForFaceFingerprint_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowIdentify.steps).hasSize(2)
        verifyOnce(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowIdentify.steps.first().activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun identifyForFingerprintFace_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowIdentify.steps).hasSize(2)
        verifyOnce(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowIdentify.steps.first().activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }
}

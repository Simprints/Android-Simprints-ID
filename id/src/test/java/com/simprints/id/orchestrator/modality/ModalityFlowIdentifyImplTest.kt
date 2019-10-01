package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_AND_FINGER
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_OR_FINGER
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

class ModalityFlowIdentifyImplTest {

    private lateinit var modalityFlowIdentify: ModalityFlowIdentifyImpl
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
        whenever(coreStepMock) { activityName } thenReturn CONSENT_ACTIVITY_NAME

        whenever(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn faceStepMock
        whenever(coreStepProcessor) { buildStepConsent(anyNotNull()) } thenReturn coreStepMock

        modalityFlowIdentify = ModalityFlowIdentifyImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor)
    }

    @Test
    fun identifyForFace_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE))

        assertThat(modalityFlowIdentify.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
        verifyNever(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun identifyForFingerprint_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER))

        assertThat(modalityFlowIdentify.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyNever(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun identifyForFaceFingerprint_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowIdentify.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowIdentify.steps[0].activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
        assertThat(modalityFlowIdentify.steps[1].activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun identifyForFingerprintFace_shouldCreateTheRightSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowIdentify.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verifyOnce(fingerprintStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepIdentify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowIdentify.steps[0].activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
        assertThat(modalityFlowIdentify.steps[1].activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }
}

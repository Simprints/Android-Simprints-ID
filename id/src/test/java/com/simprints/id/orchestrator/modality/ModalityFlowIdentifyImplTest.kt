package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.PreferencesManager
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
    @Mock lateinit var preferencesManager: PreferencesManager
    @Mock lateinit var fingerprintStepMock: Step
    @Mock lateinit var faceStepMock: Step
    @Mock lateinit var coreStepMock: Step

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(fingerprintStepMock) { activityName } thenReturn FINGERPRINT_ACTIVITY_NAME
        whenever(faceStepMock) { activityName } thenReturn FACE_ACTIVITY_NAME
        whenever(coreStepMock) { activityName } thenReturn CONSENT_ACTIVITY_NAME

        whenever(fingerprintStepProcessor) { buildStepToCapture(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildCaptureStep() } thenReturn faceStepMock
        whenever(coreStepProcessor) { buildStepConsent(anyNotNull()) } thenReturn coreStepMock

        modalityFlowIdentify = ModalityFlowIdentifyImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor, preferencesManager)
    }

    @Test
    fun identifyForFace_shouldStartWithCaptureSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE))

        with(modalityFlowIdentify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
            verifyStepWasAdded(first(), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprint_shouldStartWithCaptureSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER))

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
            verifyStepWasAdded(first(), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprint_shouldStartWithCaptureSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE, FINGER))

        with(modalityFlowIdentify.steps) {
            println(this)
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
            verifyStepWasAdded(first(), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintFace_shouldStartWithCaptureSteps() {
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER, FACE))

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
            verifyStepWasAdded(first(), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FACE_ACTIVITY_NAME)
        }
    }

    private fun verifyStepWasAdded(step: Step, activityName: String) =
        assertThat(step.activityName).isEqualTo(activityName)
}

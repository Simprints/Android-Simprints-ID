package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.VERIFY_ACTIVITY_NAME
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

    companion object {
        const val NUMBER_STEPS_FACE_OR_FINGER_VERIFY = 3
        const val NUMBER_STEPS_FACE_AND_FINGER_VERIFY = 4
    }

    private lateinit var modalityFlowVerify: ModalityFlowVerifyImpl
    @Mock lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @Mock lateinit var faceStepProcessor: FaceStepProcessor
    @Mock lateinit var coreStepProcessor: CoreStepProcessor
    @Mock lateinit var preferencesManager: PreferencesManager
    @Mock lateinit var fingerprintStepMock: Step
    @Mock lateinit var faceStepMock: Step
    @Mock lateinit var verifyCoreStepMock: Step
    @Mock lateinit var consentCoreStepMock: Step

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(fingerprintStepMock) { activityName } thenReturn FINGERPRINT_ACTIVITY_NAME
        whenever(faceStepMock) { activityName } thenReturn FACE_ACTIVITY_NAME
        whenever(verifyCoreStepMock) { activityName } thenReturn VERIFY_ACTIVITY_NAME
        whenever(consentCoreStepMock) { activityName } thenReturn CONSENT_ACTIVITY_NAME

        whenever(fingerprintStepProcessor) { buildStepToMatch(anyNotNull(), anyNotNull()) } thenReturn fingerprintStepMock
        whenever(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) } thenReturn faceStepMock
        whenever(coreStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull()) } thenReturn verifyCoreStepMock
        whenever(coreStepProcessor) { buildStepConsent(anyNotNull()) } thenReturn consentCoreStepMock

        modalityFlowVerify = ModalityFlowVerifyImpl(fingerprintStepProcessor, faceStepProcessor, coreStepProcessor, preferencesManager)
    }

    @Test
    fun verifyForFace_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FACE))

        assertThat(modalityFlowVerify.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY)
        verifyNever(fingerprintStepProcessor) { buildStepToMatch(anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun verifyForFingerprint_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FINGER))

        assertThat(modalityFlowVerify.steps).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY)
        verifyOnce(fingerprintStepProcessor) { buildStepToMatch(anyNotNull(), anyNotNull()) }
        verifyNever(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
    }

    @Test
    fun verifyForFaceFingerprint_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FACE, FINGER))

        assertThat(modalityFlowVerify.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY)
        verifyOnce(fingerprintStepProcessor) { buildStepToMatch(anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowVerify.steps[0].activityName).isEqualTo(VERIFY_ACTIVITY_NAME)
        assertThat(modalityFlowVerify.steps[1].activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
        assertThat(modalityFlowVerify.steps[2].activityName).isEqualTo(FACE_ACTIVITY_NAME)
    }

    @Test
    fun verifyForFingerprintFace_shouldCreateTheRightSteps() {
        modalityFlowVerify.startFlow(verifyAppRequest, listOf(FINGER, FACE))

        assertThat(modalityFlowVerify.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY)
        verifyOnce(fingerprintStepProcessor) { buildStepToMatch(anyNotNull(), anyNotNull()) }
        verifyOnce(faceStepProcessor) { buildStepVerify(anyNotNull(), anyNotNull(), anyNotNull()) }
        assertThat(modalityFlowVerify.steps[0].activityName).isEqualTo(VERIFY_ACTIVITY_NAME)
        assertThat(modalityFlowVerify.steps[1].activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
        assertThat(modalityFlowVerify.steps[2].activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
    }
}

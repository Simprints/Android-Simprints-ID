package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.FETCH_GUID_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.SETUP_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.verifyAppRequest
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowVerifyImplTest {

    companion object {
        const val NUMBER_STEPS_FACE_OR_FINGER_VERIFY = 4
        const val NUMBER_STEPS_FACE_AND_FINGER_VERIFY = 5
        const val NUMBER_STEPS_FACE_OR_FINGER_VERIFY_WITHOUT_CONSENT = 3
        const val NUMBER_STEPS_FACE_AND_FINGER_VERIFY_WITHOUT_CONSENT = 4
    }

    private lateinit var modalityFlowVerify: ModalityFlowVerifyImpl
    private val timeHelper: TimeHelper = TimeHelperImpl()
    @MockK private lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @MockK lateinit var faceStepProcessor: FaceStepProcessor
    @MockK lateinit var coreStepProcessor: CoreStepProcessor
    @MockK lateinit var sessionRepository: SessionRepository
    @MockK lateinit var fingerprintStepMock: Step
    @MockK lateinit var faceStepMock: Step
    @MockK lateinit var verifyCoreStepMock: Step
    @MockK lateinit var consentCoreStepMock: Step
    @MockK lateinit var setupCoreStepMock: Step

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { verifyCoreStepMock.activityName } returns FETCH_GUID_ACTIVITY_NAME
        every { consentCoreStepMock.activityName } returns CONSENT_ACTIVITY_NAME
        every { setupCoreStepMock.activityName } returns SETUP_ACTIVITY_NAME

        every { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        every { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { coreStepProcessor.buildFetchGuidStep(any(), any()) } returns verifyCoreStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentCoreStepMock
        every { coreStepProcessor.buildStepSetup(any(), any()) } returns setupCoreStepMock
    }

    @Test
    fun identifyForFace_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(true, listOf(FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprint_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(true, listOf(FINGER))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprint_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(true, listOf(FACE, FINGER))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintFace_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(true, listOf(FINGER, FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(false, listOf(FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(false, listOf(FINGER))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprintWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(false, listOf(FACE, FINGER))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintFaceWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowVerify(false, listOf(FINGER, FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), SETUP_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
        }
    }

    private fun verifyStepWasAdded(step: Step, activityName: String) =
        assertThat(step.activityName).isEqualTo(activityName)

    private fun buildModalityFlowVerify(consentRequired: Boolean, modalities: List<Modality>) {
        modalityFlowVerify = ModalityFlowVerifyImpl(fingerprintStepProcessor, faceStepProcessor,
            coreStepProcessor, timeHelper, sessionRepository, consentRequired, false, modalities)
    }
}

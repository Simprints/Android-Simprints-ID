package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_AND_FINGER
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_OR_FINGER
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.SETUP_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowIdentifyImplTest {

    private lateinit var modalityFlowIdentify: ModalityFlowIdentifyImpl
    private val timeHelper: TimeHelper = TimeHelperImpl()
    @MockK lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @MockK lateinit var faceStepProcessor: FaceStepProcessor
    @MockK lateinit var coreStepProcessor: CoreStepProcessor
    @MockK lateinit var sessionRepository: SessionRepository
    @MockK lateinit var fingerprintStepMock: Step
    @MockK lateinit var faceStepMock: Step
    @MockK lateinit var consentStepMock: Step
    @MockK lateinit var setupStepMock: Step

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { consentStepMock.activityName } returns CONSENT_ACTIVITY_NAME
        every { setupStepMock.activityName } returns SETUP_ACTIVITY_NAME

        every { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        every { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { fingerprintStepProcessor.buildConfigurationStep() } returns fingerprintStepMock
        every { faceStepProcessor.buildConfigurationStep(any(), any()) } returns faceStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentStepMock
        every { coreStepProcessor.buildStepSetup(any(), any()) } returns setupStepMock
    }

    @Test
    fun identifyForFace_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(true, listOf(FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprint_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(true, listOf(FINGER))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprint_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(true, listOf(FACE, FINGER))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
            verifyStepWasAdded(get(3), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(5), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintFace_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(true, listOf(FINGER, FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
            verifyStepWasAdded(get(3), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(5), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false, listOf(FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(2), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false, listOf(FINGER))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(2), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprintWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false, listOf(FACE, FINGER))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintFaceWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false, listOf(FINGER, FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FACE_ACTIVITY_NAME)
        }
    }

    private fun verifyStepWasAdded(step: Step, activityName: String) =
        assertThat(step.activityName).isEqualTo(activityName)

    private fun buildModalityFlowIdentify(consentRequired: Boolean, modalities: List<Modality>) {
        modalityFlowIdentify = ModalityFlowIdentifyImpl(fingerprintStepProcessor, faceStepProcessor,
            coreStepProcessor, GROUP.GLOBAL, timeHelper, sessionRepository, consentRequired, true,
            modalities,"projectId", "deviceId")
    }
}

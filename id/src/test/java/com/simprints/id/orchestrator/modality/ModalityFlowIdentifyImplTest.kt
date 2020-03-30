package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.GROUP
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
    @MockK lateinit var coreStepMock: Step

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { coreStepMock.activityName } returns CONSENT_ACTIVITY_NAME

        every { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        every { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns coreStepMock
    }

    @Test
    fun identifyForFace_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(true)
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE))

        with(modalityFlowIdentify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER)
            verifyStepWasAdded(first(), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprint_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(true)
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
        buildModalityFlowIdentify(true)
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
        buildModalityFlowIdentify(true)
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER, FACE))

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
            verifyStepWasAdded(first(), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false)
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE))

        with(modalityFlowIdentify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(first(), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false)
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER))

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(first(), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprintWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false)
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FACE, FINGER))

        with(modalityFlowIdentify.steps) {
            println(this)
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintFaceWithoutConsent_shouldStartWithCaptureSteps() {
        buildModalityFlowIdentify(false)
        modalityFlowIdentify.startFlow(identifyAppRequest, listOf(FINGER, FACE))

        with(modalityFlowIdentify.steps) {
            println(this)
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FACE_ACTIVITY_NAME)
        }
    }

    private fun verifyStepWasAdded(step: Step, activityName: String) =
        assertThat(step.activityName).isEqualTo(activityName)

    private fun buildModalityFlowIdentify(consentRequired: Boolean) {
        modalityFlowIdentify = ModalityFlowIdentifyImpl(fingerprintStepProcessor, faceStepProcessor,
            coreStepProcessor, GROUP.GLOBAL, timeHelper, sessionRepository, consentRequired)
    }
}

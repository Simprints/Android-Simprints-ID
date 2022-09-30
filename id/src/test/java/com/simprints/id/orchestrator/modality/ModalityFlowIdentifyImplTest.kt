package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_AND_FINGER
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FACE_WITHOUT_CONSENT
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FINGER
import com.simprints.id.orchestrator.modality.ModalityFlowEnrolImplTest.Companion.NUMBER_STEPS_FINGER_WITHOUT_CONSENT
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.SETUP_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.*
import com.simprints.infra.config.domain.models.IdentificationConfiguration
import com.simprints.infra.login.LoginManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowIdentifyImplTest {

    private lateinit var modalityFlowIdentify: ModalityFlowIdentify

    @MockK
    lateinit var fingerprintStepProcessor: FingerprintStepProcessor

    @MockK
    lateinit var faceStepProcessor: FaceStepProcessor

    @MockK
    lateinit var coreStepProcessor: CoreStepProcessor

    @MockK
    lateinit var fingerprintStepMock: Step

    @MockK
    lateinit var faceStepMock: Step

    @MockK
    lateinit var consentStepMock: Step

    @MockK
    lateinit var setupStepMock: Step

    private val generalConfiguration = mockk<GeneralConfiguration>()
    private val consentConfiguration = mockk<ConsentConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { consent } returns consentConfiguration
            every { identification } returns mockk {
                every { poolType } returns IdentificationConfiguration.PoolType.PROJECT
            }
        }
    }
    private val loginManager = mockk<LoginManager> {
        every { signedInProjectId } returns ModalityFlowEnrolImplTest.PROJECT_ID
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { consentStepMock.activityName } returns CONSENT_ACTIVITY_NAME
        every { setupStepMock.activityName } returns SETUP_ACTIVITY_NAME

        coEvery { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        coEvery { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { fingerprintStepProcessor.buildConfigurationStep() } returns fingerprintStepMock
        every { faceStepProcessor.buildConfigurationStep(any(), any()) } returns faceStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentStepMock
        every { coreStepProcessor.buildStepSetup(any(), any()) } returns setupStepMock
    }

    @Test
    fun identifyForFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FINGERPRINT))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FINGER)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FACE, FINGERPRINT))
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
    fun identifyForFingerprintFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FINGERPRINT, FACE))
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
    fun identifyForFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_WITHOUT_CONSENT)
            verifyStepWasAdded(get(2), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FINGERPRINT))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)

            assertThat(this).hasSize(NUMBER_STEPS_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(2), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFaceFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FACE, FINGERPRINT))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        with(modalityFlowIdentify.steps) {
            println(this)
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun identifyForFingerprintFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FINGERPRINT, FACE))
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

    private fun buildModalityFlowIdentify(collectConsent: Boolean, modalities: List<Modality>) {
        every { generalConfiguration.collectLocation } returns true
        every { generalConfiguration.modalities } returns modalities
        every { consentConfiguration.collectConsent } returns collectConsent

        modalityFlowIdentify = ModalityFlowIdentify(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            loginManager,
            "deviceId"
        )
    }
}

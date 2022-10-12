package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.FETCH_GUID_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.verifyAppRequest
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FACE
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FINGERPRINT
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

class ModalityFlowVerifyImplTest {

    companion object {
        const val NUMBER_STEPS_FACE_OR_FINGER_VERIFY = 4
        const val NUMBER_STEPS_FINGER_VERIFY = 4
        const val NUMBER_STEPS_FACE_AND_FINGER_VERIFY = 6
        const val NUMBER_STEPS_FACE_OR_FINGER_VERIFY_WITHOUT_CONSENT = 3
        const val NUMBER_STEPS_FINGER_VERIFY_WITHOUT_CONSENT = 3
        const val NUMBER_STEPS_FACE_AND_FINGER_VERIFY_WITHOUT_CONSENT = 5
    }

    private lateinit var modalityFlowVerify: ModalityFlowVerify

    @MockK
    private lateinit var fingerprintStepProcessor: FingerprintStepProcessor

    @MockK
    lateinit var faceStepProcessor: FaceStepProcessor

    @MockK
    lateinit var coreStepProcessor: CoreStepProcessor

    @MockK
    lateinit var fingerprintStepMock: Step

    @MockK
    lateinit var faceStepMock: Step

    @MockK
    lateinit var verifyCoreStepMock: Step

    @MockK
    lateinit var consentCoreStepMock: Step

    private val generalConfiguration = mockk<GeneralConfiguration>()
    private val consentConfiguration = mockk<ConsentConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { consent } returns consentConfiguration
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
        every { verifyCoreStepMock.activityName } returns FETCH_GUID_ACTIVITY_NAME
        every { consentCoreStepMock.activityName } returns CONSENT_ACTIVITY_NAME

        coEvery { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        coEvery { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { fingerprintStepProcessor.buildConfigurationStep() } returns fingerprintStepMock
        every { faceStepProcessor.buildConfigurationStep(any(), any()) } returns faceStepMock
        every { coreStepProcessor.buildFetchGuidStep(any(), any()) } returns verifyCoreStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentCoreStepMock
    }

    @Test
    fun verifyForFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY)
            verifyStepWasAdded(get(0), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun verifyForFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FINGER_VERIFY)
            verifyStepWasAdded(get(0), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun verifyForFaceFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FACE, FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY)
            verifyStepWasAdded(get(0), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(5), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun verifyForFingerprintFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FINGERPRINT, FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY)
            verifyStepWasAdded(get(0), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), CONSENT_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(5), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun verifyForFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_OR_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun verifyForFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun verifyForFaceFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FACE, FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun verifyForFingerprintFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FINGERPRINT, FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        with(modalityFlowVerify.steps) {
            assertThat(this).hasSize(NUMBER_STEPS_FACE_AND_FINGER_VERIFY_WITHOUT_CONSENT)
            verifyStepWasAdded(get(0), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(1), FACE_ACTIVITY_NAME)
            verifyStepWasAdded(get(2), FETCH_GUID_ACTIVITY_NAME)
            verifyStepWasAdded(get(3), FINGERPRINT_ACTIVITY_NAME)
            verifyStepWasAdded(get(4), FACE_ACTIVITY_NAME)
        }
    }

    private fun verifyStepWasAdded(step: Step, activityName: String) =
        assertThat(step.activityName).isEqualTo(activityName)

    private fun buildModalityFlowVerify(collectConsent: Boolean, modalities: List<Modality>) {
        every { generalConfiguration.collectLocation } returns true
        every { generalConfiguration.modalities } returns modalities
        every { consentConfiguration.collectConsent } returns collectConsent

        modalityFlowVerify = ModalityFlowVerify(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            loginManager,
            "deviceId"
        )
    }
}

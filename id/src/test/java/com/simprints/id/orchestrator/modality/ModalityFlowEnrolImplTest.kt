package com.simprints.id.orchestrator.modality

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceRequestCode
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FACE
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality.FINGERPRINT
import com.simprints.infra.config.domain.models.IdentificationConfiguration
import com.simprints.infra.login.LoginManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowEnrolImplTest {

    companion object {
        const val NUMBER_STEPS_FACE = 3
        const val NUMBER_STEPS_FINGER = 3
        const val NUMBER_STEPS_FACE_AND_FINGER = 5

        const val NUMBER_STEPS_FACE_WITHOUT_CONSENT = 2
        const val NUMBER_STEPS_FINGER_WITHOUT_CONSENT = 2
        const val NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT = 4
        const val PROJECT_ID = "projectId"
    }

    private lateinit var modalityFlowEnrol: ModalityFlowEnrol

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
        every { signedInProjectId } returns PROJECT_ID
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { consentStepMock.activityName } returns CONSENT_ACTIVITY_NAME

        coEvery { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        coEvery { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { fingerprintStepProcessor.buildConfigurationStep() } returns fingerprintStepMock
        every { faceStepProcessor.buildConfigurationStep(any(), any()) } returns faceStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentStepMock
    }

    @Test
    fun enrolForFace_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE)

        coVerify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprint_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FINGER)
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_WITHOUT_CONSENT)
        coVerify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprintWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FINGER_WITHOUT_CONSENT)
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceFingerprint_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FACE, FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
            assertThat(get(3).activityName).isEqualTo(FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFingerprintFace_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FINGERPRINT, FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
            assertThat(get(3).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFaceFingerprintWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FACE, FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFingerprintFaceWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FINGERPRINT, FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolmentPlusForFinger_shouldAddMatchStepAfterCapture() = runTest {
        val fingerprintCaptureResponse = mockk<FingerprintCaptureResponse>()
        buildModalityFlowEnrol(
            collectConsent = false,
            modalities = listOf(FINGERPRINT),
            isEnrolmentPlus = true
        )
        val appRequest = buildAppEnrolRequest()
        every {
            fingerprintStepProcessor.processResult(
                any(),
                any(),
                any()
            )
        } returns fingerprintCaptureResponse

        modalityFlowEnrol.handleIntentResult(
            appRequest,
            FingerprintRequestCode.CAPTURE.value,
            Activity.RESULT_OK,
            mockk()
        )

        verify(exactly = 1) { fingerprintStepProcessor.buildStepToMatch(any(), any()) }
    }

    @Test
    fun enrolmentPlusForFace_shouldAddMatchStepAfterCapture() = runTest {
        val faceCaptureResponse = mockk<FaceCaptureResponse>()
        buildModalityFlowEnrol(
            collectConsent = false,
            modalities = listOf(FACE),
            isEnrolmentPlus = true
        )
        val appRequest = buildAppEnrolRequest()
        every { faceStepProcessor.processResult(any(), any(), any()) } returns faceCaptureResponse

        modalityFlowEnrol.handleIntentResult(
            appRequest,
            FaceRequestCode.CAPTURE.value,
            Activity.RESULT_OK,
            mockk()
        )

        verify(exactly = 1) { faceStepProcessor.buildStepMatch(any(), any()) }
    }

    private fun buildAppEnrolRequest() =
        AppEnrolRequest(PROJECT_ID, "userId", "moduleId", "metadata")

    private fun buildModalityFlowEnrol(
        collectConsent: Boolean,
        modalities: List<Modality>,
        isEnrolmentPlus: Boolean = false
    ) {
        every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns isEnrolmentPlus
        every { generalConfiguration.collectLocation } returns true
        every { generalConfiguration.modalities } returns modalities
        every { consentConfiguration.collectConsent } returns collectConsent

        modalityFlowEnrol = ModalityFlowEnrol(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            loginManager,
            "deviceId"
        )
    }
}

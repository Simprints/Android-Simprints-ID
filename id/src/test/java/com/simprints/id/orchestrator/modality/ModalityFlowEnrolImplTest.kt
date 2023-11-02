package com.simprints.id.orchestrator.modality

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.enrolAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.SETUP_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceRequestCode
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintRequestCode
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality.FACE
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality.FINGERPRINT
import com.simprints.infra.config.store.models.IdentificationConfiguration
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.CAPTURE_ACTIVITY_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.CAPTURE_ACTIVITY_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowEnrolImplTest {

    companion object {
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
    lateinit var setupStepMock: Step

    @MockK
    lateinit var fingerprintStepMock: Step

    @MockK
    lateinit var faceStepMock: Step

    @MockK
    lateinit var consentStepMock: Step

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var generalConfiguration: GeneralConfiguration

    @MockK
    lateinit var consentConfiguration: ConsentConfiguration

    @MockK
    lateinit var authStore: AuthStore

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { consent } returns consentConfiguration
            every { identification } returns mockk {
                every { poolType } returns IdentificationConfiguration.PoolType.PROJECT
            }
        }
        every { authStore.signedInProjectId } returns PROJECT_ID

        every { setupStepMock.activityName } returns SETUP_ACTIVITY_NAME
        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { consentStepMock.activityName } returns CONSENT_ACTIVITY_NAME

        coEvery { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        coEvery { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { faceStepProcessor.buildConfigurationStep(any(), any()) } returns faceStepMock
        every { coreStepProcessor.buildStepSetup() } returns setupStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentStepMock
    }

    @Test
    fun enrolForFace_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprint_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprintWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceFingerprint_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FACE, FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        assertThat(modalityFlowEnrol.steps.map { it.activityName }).isEqualTo(
            listOf(
                SETUP_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
                CONSENT_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
                FINGERPRINT_ACTIVITY_NAME,
            )
        )
    }

    @Test
    fun enrolForFingerprintFace_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(true, listOf(FINGERPRINT, FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        assertThat(modalityFlowEnrol.steps.map { it.activityName }).isEqualTo(
            listOf(
                SETUP_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
                CONSENT_ACTIVITY_NAME,
                FINGERPRINT_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
            )
        )
    }

    @Test
    fun enrolForFaceFingerprintWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FACE, FINGERPRINT))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        coVerify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        assertThat(modalityFlowEnrol.steps.map { it.activityName }).isEqualTo(
            listOf(
                SETUP_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
                FINGERPRINT_ACTIVITY_NAME,
            )
        )
    }

    @Test
    fun enrolForFingerprintFaceWithoutConsent_shouldCreateTheRightSteps() = runTest {
        buildModalityFlowEnrol(false, listOf(FINGERPRINT, FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        verify(exactly = 1) { coreStepProcessor.buildStepSetup() }
        coVerify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        assertThat(modalityFlowEnrol.steps.map { it.activityName }).isEqualTo(
            listOf(
                SETUP_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
                FINGERPRINT_ACTIVITY_NAME,
                FACE_ACTIVITY_NAME,
            )
        )
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

        verify(exactly = 1) { fingerprintStepProcessor.buildStepToMatch(any(), any(), any()) }
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

        verify(exactly = 1) { faceStepProcessor.buildStepMatch(any(), any(), any()) }
    }

    private fun buildAppEnrolRequest() =
        AppEnrolRequest(
            projectId = PROJECT_ID,
            userId = "userId".asTokenizableRaw(),
            moduleId = "moduleId".asTokenizableRaw(),
            metadata = "metadata"
        )

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
            authStore,
            "deviceId"
        )
    }
}

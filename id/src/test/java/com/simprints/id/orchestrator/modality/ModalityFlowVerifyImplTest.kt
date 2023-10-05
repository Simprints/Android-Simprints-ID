package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.FETCH_GUID_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.SETUP_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessor
import com.simprints.id.orchestrator.verifyAppRequest
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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowVerifyImplTest {

    private lateinit var modalityFlowVerify: ModalityFlowVerify

    @MockK
    private lateinit var fingerprintStepProcessor: FingerprintStepProcessor

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
    lateinit var verifyCoreStepMock: Step

    @MockK
    lateinit var consentCoreStepMock: Step

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
        every { authStore.signedInProjectId } returns ModalityFlowEnrolImplTest.PROJECT_ID

        every { setupStepMock.activityName } returns SETUP_ACTIVITY_NAME
        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { verifyCoreStepMock.activityName } returns FETCH_GUID_ACTIVITY_NAME
        every { consentCoreStepMock.activityName } returns CONSENT_ACTIVITY_NAME

        coEvery { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        coEvery { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { fingerprintStepProcessor.buildConfigurationStep() } returns fingerprintStepMock
        every { faceStepProcessor.buildConfigurationStep(any(), any()) } returns faceStepMock
        every { coreStepProcessor.buildFetchGuidStep(any(), any()) } returns verifyCoreStepMock
        every { coreStepProcessor.buildStepSetup() } returns setupStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentCoreStepMock
    }

    @Test
    fun verifyForFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    @Test
    fun verifyForFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun verifyForFaceFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FACE, FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun verifyForFingerprintFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(true, listOf(FINGERPRINT, FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    @Test
    fun verifyForFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    @Test
    fun verifyForFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun verifyForFaceFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FACE, FINGERPRINT))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun verifyForFingerprintFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowVerify(false, listOf(FINGERPRINT, FACE))
        modalityFlowVerify.startFlow(verifyAppRequest)

        assertThat(modalityFlowVerify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FETCH_GUID_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    private fun buildModalityFlowVerify(collectConsent: Boolean, modalities: List<Modality>) {
        every { generalConfiguration.collectLocation } returns true
        every { generalConfiguration.modalities } returns modalities
        every { consentConfiguration.collectConsent } returns collectConsent

        modalityFlowVerify = ModalityFlowVerify(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            authStore,
            "deviceId"
        )
    }
}

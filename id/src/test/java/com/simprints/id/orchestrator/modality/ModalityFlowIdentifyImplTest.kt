package com.simprints.id.orchestrator.modality

import com.google.common.truth.Truth.assertThat
import com.simprints.id.orchestrator.identifyAppRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.CONSENT_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.core.CoreStepProcessorImpl.Companion.SETUP_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.face.FaceStepProcessor
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
        every { authStore.signedInProjectId } returns ModalityFlowEnrolImplTest.PROJECT_ID

        every { setupStepMock.activityName } returns SETUP_ACTIVITY_NAME
        every { fingerprintStepMock.activityName } returns FINGERPRINT_ACTIVITY_NAME
        every { faceStepMock.activityName } returns FACE_ACTIVITY_NAME
        every { consentStepMock.activityName } returns CONSENT_ACTIVITY_NAME

        coEvery { fingerprintStepProcessor.buildStepToCapture() } returns fingerprintStepMock
        coEvery { faceStepProcessor.buildCaptureStep() } returns faceStepMock
        every { fingerprintStepProcessor.buildConfigurationStep() } returns fingerprintStepMock
        every { faceStepProcessor.buildConfigurationStep(any(), any()) } returns faceStepMock
        every { coreStepProcessor.buildStepSetup() } returns setupStepMock
        every { coreStepProcessor.buildStepConsent(any()) } returns consentStepMock
    }

    @Test
    fun identifyForFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    @Test
    fun identifyForFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FINGERPRINT))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun identifyForFaceFingerprint_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FACE, FINGERPRINT))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun identifyForFingerprintFace_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(true, listOf(FINGERPRINT, FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            CONSENT_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    @Test
    fun identifyForFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    @Test
    fun identifyForFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FINGERPRINT))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun identifyForFaceFingerprintWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FACE, FINGERPRINT))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
        ))
    }

    @Test
    fun identifyForFingerprintFaceWithoutConsent_shouldStartWithCaptureSteps() = runTest {
        buildModalityFlowIdentify(false, listOf(FINGERPRINT, FACE))
        modalityFlowIdentify.startFlow(identifyAppRequest)

        assertThat(modalityFlowIdentify.steps.map { it.activityName }).isEqualTo(listOf(
            SETUP_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
            FINGERPRINT_ACTIVITY_NAME,
            FACE_ACTIVITY_NAME,
        ))
    }

    private fun buildModalityFlowIdentify(collectConsent: Boolean, modalities: List<Modality>) {
        every { generalConfiguration.collectLocation } returns true
        every { generalConfiguration.modalities } returns modalities
        every { consentConfiguration.collectConsent } returns collectConsent

        modalityFlowIdentify = ModalityFlowIdentify(
            fingerprintStepProcessor,
            faceStepProcessor,
            coreStepProcessor,
            configManager,
            authStore,
            "deviceId"
        )
    }
}

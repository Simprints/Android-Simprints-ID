package com.simprints.id.orchestrator.modality

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.*
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
import com.simprints.id.tools.TimeHelperImpl
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FACE_ACTIVITY_NAME
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl.Companion.ACTIVITY_CLASS_NAME as FINGERPRINT_ACTIVITY_NAME

class ModalityFlowEnrolImplTest {

    companion object {
        const val NUMBER_STEPS_FACE = 4
        const val NUMBER_STEPS_FINGER = 3  // TODO : Change back to 4 once fingerprint implements configuration request
        const val NUMBER_STEPS_FACE_AND_FINGER = 5 // TODO : Change back to 6 once fingerprint implements configuration request

        const val NUMBER_STEPS_FACE_WITHOUT_CONSENT = 3
        const val NUMBER_STEPS_FINGER_WITHOUT_CONSENT = 2 // TODO : Change back to 3 once fingerprint implements configuration request
        const val NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT = 4 // TODO : Change back to 5 once fingerprint implements configuration request
        const val PROJECT_ID = "projectId"
    }

    private lateinit var modalityFlowEnrol: ModalityFlowEnrolImpl
    private val timeHelper = TimeHelperImpl()
    @MockK lateinit var fingerprintStepProcessor: FingerprintStepProcessor
    @MockK lateinit var faceStepProcessor: FaceStepProcessor
    @MockK lateinit var coreStepProcessor: CoreStepProcessor
    @MockK lateinit var fingerprintStepMock: Step
    @MockK lateinit var faceStepMock: Step
    @MockK lateinit var consentStepMock: Step
    @MockK lateinit var setupStepMock: Step
    @MockK lateinit var sessionRepository: SessionRepository

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
    fun enrolForFace_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true, listOf(FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE)

        verify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprint_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true, listOf(FINGER))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FINGER)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false, listOf(FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_WITHOUT_CONSENT)
        verify(exactly = 0) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFingerprintWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false, listOf(FINGER))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FINGER_WITHOUT_CONSENT)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 0) { faceStepProcessor.buildCaptureStep() }
    }

    @Test
    fun enrolForFaceFingerprint_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true, listOf(FACE, FINGER))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
//            assertThat(get(1).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME) // TODO : Uncomment once fingerprint implements configuration request
            assertThat(get(1).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
            assertThat(get(3).activityName).isEqualTo(FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFingerprintFace_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(true, listOf(FINGER, FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        with(modalityFlowEnrol.steps) {
//            assertThat(get(0).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME) // TODO : Uncomment once fingerprint implements configuration request
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(CONSENT_ACTIVITY_NAME)
            assertThat(get(3).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFaceFingerprintWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false, listOf(FACE, FINGER))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }
        verify(exactly = 1) { faceStepProcessor.buildCaptureStep() }

        with(modalityFlowEnrol.steps) {
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
//            assertThat(get(1).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME) // TODO : Uncomment once fingerprint implements configuration request
            assertThat(get(1).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(FACE_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolForFingerprintFaceWithoutConsent_shouldCreateTheRightSteps() {
        buildModalityFlowEnrol(false, listOf(FINGER, FACE))
        modalityFlowEnrol.startFlow(enrolAppRequest)

        assertThat(modalityFlowEnrol.steps).hasSize(NUMBER_STEPS_FACE_AND_FINGER_WITHOUT_CONSENT)
        verify(exactly = 1) { fingerprintStepProcessor.buildStepToCapture() }

        with(modalityFlowEnrol.steps) {
//            assertThat(get(0).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME) // TODO : Uncomment once fingerprint implements configuration request
            assertThat(get(0).activityName).isEqualTo(SETUP_ACTIVITY_NAME)
            assertThat(get(1).activityName).isEqualTo(FACE_ACTIVITY_NAME)
            assertThat(get(2).activityName).isEqualTo(FINGERPRINT_ACTIVITY_NAME)
        }
    }

    @Test
    fun enrolmentPlusForFinger_shouldAddMatchStepAfterCapture() = runBlockingTest {
        val fingerprintCaptureResponse = mockk<FingerprintCaptureResponse>()
        buildModalityFlowEnrol(consentRequired = false, modalities = listOf(FINGER), isEnrolmentPlus = true)
        val appRequest = buildAppEnrolRequest()
        every { fingerprintStepProcessor.processResult(any(), any(), any()) } returns fingerprintCaptureResponse

        modalityFlowEnrol.handleIntentResult(appRequest, FingerprintRequestCode.CAPTURE.value, Activity.RESULT_OK, mockk())

        verify(exactly = 1) { fingerprintStepProcessor.buildStepToMatch(any(), any()) }
    }

    @Test
    fun enrolmentPlusForFace_shouldAddMatchStepAfterCapture() = runBlockingTest {
        val faceCaptureResponse = mockk<FaceCaptureResponse>()
        buildModalityFlowEnrol(consentRequired = false, modalities = listOf(FACE), isEnrolmentPlus = true)
        val appRequest = buildAppEnrolRequest()
        every { faceStepProcessor.processResult(any(), any(), any()) } returns faceCaptureResponse

        modalityFlowEnrol.handleIntentResult(appRequest, FaceRequestCode.CAPTURE.value, Activity.RESULT_OK, mockk())

        verify(exactly = 1) { faceStepProcessor.buildStepMatch(any(), any()) }
    }

    private fun buildAppEnrolRequest() =
        AppEnrolRequest(PROJECT_ID, "userId", "moduleId", "metadata")

    private fun buildModalityFlowEnrol(consentRequired: Boolean,
                                       modalities: List<Modality>,
                                       isEnrolmentPlus: Boolean = false) {
        modalityFlowEnrol = ModalityFlowEnrolImpl(fingerprintStepProcessor, faceStepProcessor,
            coreStepProcessor, timeHelper, sessionRepository, consentRequired, locationRequired = true,
            modalities = modalities, projectId = PROJECT_ID, deviceId = "deviceId",
            isEnrolmentPlus = isEnrolmentPlus, matchGroup = GROUP.GLOBAL)
    }
}
